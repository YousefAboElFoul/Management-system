import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class Utility {
    // Database parameters
    public static final String url = "jdbc:postgresql://ec2-54-235-92-244.compute-1.amazonaws.com:5432/d70m64dg1qc8fu?sslmode=require";
    public static final String user = "gkcmczoxettaer";
    public static final String password = "8d4b50fd5a522fd0256536f4f6993a61fad200dde8d58372c1200b5e63cfe694";

    private final static Logger LOGGER = Logger.getLogger(Utility.class.getName());

    /* This for parsing messages through udp */
    @SuppressWarnings("uncheked")
    public static <T> T parsingMessage(String in, String from) throws IOException, SQLException {
        // parsing
        String[] txt = in.replaceAll(" ","").replaceAll(".+\\{","").split("\\||\\}");

        // message to be returned
        T messageReceived = null;
        String query = null;
        int mCode = Integer.valueOf(txt[0]);

        try {
            switch (mCode) {
                case Message.REQUEST_CODE:
                    ArrayList<String> req_list = getParticipantsStrings(txt[5]);
                    messageReceived = (T) new RequestMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), req_list, txt[6]);

                    // the mysql insert statement
                    query = "INSERT INTO RequestMessage(REQUESTNUMBER, DATEINSERTED, CURRENTTIME, MINIMUM, LISTOFPARTICIPANTS, TOPIC)"
                            + " VALUES (?, ?, ?, ?, ?, ?)";
                    break;
                case Message.RESPONSE_CODE:
                    messageReceived = (T) new ResponseMessage(txt[1]);
                    break;
                case Message.INVITE_CODE:
                    messageReceived = (T) new InviteMessage(txt[1], txt[2], txt[3], txt[4], txt[5]);
                    break;
                case Message.ACCEPT_CODE:
                    messageReceived = (T) new AcceptMessage(txt[1]);
                    break;
                case Message.REJECT_CODE:
                    messageReceived = (T) new RejectMessage(txt[1]);
                    break;
                case Message.CONFIRM_CODE:
                    messageReceived = (T) new ConfirmMessage(txt[1], txt[2]);
                    break;
                case Message.SCHEDULED_CODE:
                    ArrayList<String> conf_list = getParticipantsStrings(txt[4]);
                    messageReceived = (T) new ScheduledMessage(txt[1], txt[2], txt[3], conf_list);
                    break;
                case Message.CANCEL_1_CODE:
                    messageReceived = (T) new CancelMessageI(txt[1]);
                    break;
                case Message.NOT_SCHEDULED_CODE:
                    ArrayList<String> nscheq_list = getParticipantsStrings(txt[5]);
                    messageReceived = (T) new NotScheduledMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), nscheq_list, txt[6]);
                    break;
                case Message.CANCEL_2_CODE:
                    messageReceived = (T) new CancelMessageII(txt[1]);
                    break;
                case Message.WITHDRAW_1_CODE:
                    messageReceived = (T) new WithdrawMessageI(txt[1], txt[2]);
                    break;
                case Message.WITHDRAW_2_CODE:
                    messageReceived = (T) new WithdrawMessageII(txt[1]);
                    break;
                case Message.ADD_CODE:
                    messageReceived = (T) new AddMessage(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO AddMessage(MEETINGNUMBER, WHO)"
                            + " VALUES (?, ?)";
                    break;
                case Message.ADDED_CODE:
                    messageReceived = (T) new AddedMessage(txt[1], txt[2]);

                    // the mysql update statement
                    query = "UPDATE AddMessage SET WASADDED = TRUE"
                            + " WHERE MEETINGNUMBER = ? AND WHO = ?";
                    break;
                case Message.ROOM_CHANGE_CODE:
                    messageReceived = (T) new RoomChangeMessage(txt[1], txt[2]);

                    // the mysql insert statement
                    query = "INSERT INTO RoomChangeMessage(MEETINGNUMBER, NEWROOM)"
                            + " VALUES (?, ?)";
                    break;
                default:
                    messageReceived = null;
                    break;
            }
        } catch (Exception e) {
            e.getMessage();
            return (T) "Error occurred in the request message";
        }

        // For db-insertion
        if (query != null) {
            if (mCode == Message.ADDED_CODE)
                updateMessage(query, mCode, messageReceived, from);
            else if (mCode != Message.ADDED_CODE || mCode != Message.INVITE_CODE
                    || mCode != Message.RESPONSE_CODE || mCode != Message.CANCEL_2_CODE
                    || mCode != Message.WITHDRAW_2_CODE)
                insertMessage(query, mCode, messageReceived, from);
        }

        return (messageReceived != null) ? (T) messageReceived : (T) "Error occurred in the request message";

    }

    /* This for connecting to db */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /* This for inserting messages into db */
    private static void insertMessage(String query, int mCode, Object message, String from)  {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            //Do parse our sql value
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

            switch (mCode) {
                case Message.REQUEST_CODE:
                    preparedStmt.setString(1, ((RequestMessage) message).getRQ_NUMBER());
                    preparedStmt.setDate(2, new java.sql.Date(format.parse(((RequestMessage) message).getRQ_DATE()).getTime()));
                    preparedStmt.setTime(3, Time.valueOf(LocalTime.parse(((RequestMessage) message).getRQ_TIME())));
                    preparedStmt.setInt(4, ((RequestMessage) message).getMIN_NUMBER_OF_PARTICIPANTS());
                    preparedStmt.setString(5, ((RequestMessage) message).getLIST_OF_PARTICIPANTS().toString().replace("[[", "").replace("]]", ""));
                    preparedStmt.setString(6, ((RequestMessage) message).getRQ_TOPIC());
                    break;
                case Message.RESPONSE_CODE:
                    preparedStmt.setString(1, ((ResponseMessage) message).getRQ_NUMBER());
                    break;
                case Message.INVITE_CODE:
                    preparedStmt.setString(1, ((InviteMessage) message).getMT_NUMBER());
                    preparedStmt.setDate(2, new java.sql.Date(format.parse(((InviteMessage) message).getIV_DATE()).getTime()));
                    preparedStmt.setTime(3, Time.valueOf(LocalTime.parse(((InviteMessage) message).getIV_TIME())));
                    preparedStmt.setString(4, ((InviteMessage) message).getIV_TOPIC());
                    preparedStmt.setString(5, ((InviteMessage) message).getIV_REQUESTER());
                    preparedStmt.setString(6, from);
                    break;
                case Message.ACCEPT_CODE:
                    preparedStmt.setString(1, ((AcceptMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.REJECT_CODE:
                    preparedStmt.setString(1, ((RejectMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.CONFIRM_CODE:
                    preparedStmt.setString(1, ((ConfirmMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, ((ConfirmMessage) message).getROOM_NUMBER());
                    break;
                case Message.SCHEDULED_CODE:
                    preparedStmt.setString(1, ((ScheduledMessage) message).getRQ_NUMBER());
                    preparedStmt.setString(2, ((ScheduledMessage) message).getMT_NUMBER());
                    preparedStmt.setString(3, ((ScheduledMessage) message).getROOM_NUMBER());
                    preparedStmt.setString(4, ((ScheduledMessage) message).getLIST_OF_CONFIRMED_PARTICIPANTS().toString());
                    break;
                case Message.CANCEL_1_CODE:
                    preparedStmt.setString(1, ((CancelMessageI) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.NOT_SCHEDULED_CODE:
                    preparedStmt.setString(1, ((NotScheduledMessage) message).getRQ_NUMBER());
                    preparedStmt.setDate(2, new java.sql.Date(format.parse(((NotScheduledMessage) message).getNSD_DATE()).getTime()));
                    preparedStmt.setTime(3, Time.valueOf(LocalTime.parse(((NotScheduledMessage) message).getNSD_TIME())));
                    preparedStmt.setInt(4, ((NotScheduledMessage) message).getMIN_NUMBER_OF_PARTICIPANTS());
                    preparedStmt.setString(5, ((NotScheduledMessage) message).getLIST_OF_CONFIRMED_PARTICIPANTS().toString());
                    preparedStmt.setString(6, ((NotScheduledMessage) message).getNSD_TOPIC());
                    break;
                case Message.CANCEL_2_CODE:
                    preparedStmt.setString(1, ((CancelMessageII) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.WITHDRAW_2_CODE:
                    preparedStmt.setString(1, ((WithdrawMessageII) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.ADD_CODE:
                    preparedStmt.setString(1, ((AddMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, from);
                    break;
                case Message.ROOM_CHANGE_CODE:
                    preparedStmt.setString(1, ((RoomChangeMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, ((RoomChangeMessage) message).getNEW_ROOM_NUMBER());
                    break;
                default:
                    break;
            }

            // execute the preparedstatement
            preparedStmt.execute();
        }
        catch (SQLException | ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /* This for updating messages into db */
    private static void updateMessage(String query, int mCode, Object message, String from)  {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            //Do parse our sql values
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

            switch (mCode) {
                case Message.REQUEST_CODE:
                    break;
                case Message.RESPONSE_CODE:
                    break;
                case Message.INVITE_CODE:
                    break;
                case Message.ACCEPT_CODE:
                    break;
                case Message.REJECT_CODE:
                    break;
                case Message.CONFIRM_CODE:
                    break;
                case Message.SCHEDULED_CODE:
                    break;
                case Message.CANCEL_1_CODE:
                    break;
                case Message.NOT_SCHEDULED_CODE:
                    break;
                case Message.CANCEL_2_CODE:
                    break;
                case Message.WITHDRAW_2_CODE:
                    break;
                case Message.ADD_CODE:
                    break;
                case Message.ADDED_CODE:
                    preparedStmt.setString(1, ((AddedMessage) message).getMT_NUMBER());
                    preparedStmt.setString(2, ((AddedMessage) message).getIP_ADDRESS());
                    break;
                case Message.ROOM_CHANGE_CODE:
                    break;
                default:
                    break;
            }

            // execute the preparedstatement
            preparedStmt.executeUpdate();
        }
        catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /* This for parsing the list of participants */
    private static ArrayList<String> getParticipantsStrings (String s){
        ArrayList<String> mylist = new ArrayList<String>();
        String[] lop = s.split(",");
        for (int i = 0; i < lop.length; i++) {
            mylist.add(lop[i]);
        }
        return mylist;
    }

    /* This for getting the User Input */
    public static String getUserInput (String userInput, String ip) throws Exception {
        String[] ui = userInput.split(" ");
        int arg_1 = 0;
        try {
            arg_1 = Integer.parseInt(ui[0]);
        } catch (NumberFormatException e) {
            e.getMessage();
        }
        if (arg_1 != 0) {
            return switchMessages(ip, ui, arg_1);
        } else
            return "Invalid Message";
    }

    /* Switch between messages */
    private static String switchMessages (String ip, String[] ui ,int arg_1) throws UnknownHostException, SQLException {
        switch (arg_1) {
            case Message.REQUEST_CODE:
                ArrayList<String> req_list = getParticipantsStrings(ui[4]);
                return new RequestMessage(ip, ui[1], ui[2], Integer.valueOf(ui[3]), req_list, ui[5]).printReqMessage();
            case Message.RESPONSE_CODE:
                return new ResponseMessage(ui[1]).printRespMessage();
            case Message.INVITE_CODE:
                return new InviteMessage(ip, ui[1], ui[2], ui[3], ui[4]).printInvMessage();
            case Message.ACCEPT_CODE:
                return new AcceptMessage(ui[1]).printAMessage();
            case Message.REJECT_CODE:
                return new RejectMessage(ui[1]).printRMessage();
            case Message.CONFIRM_CODE:
                return new ConfirmMessage(ui[1], ui[2]).printConfMessage();
            case Message.SCHEDULED_CODE:
                ArrayList<String> conf_list = getParticipantsStrings(ui[4]);
                return new ScheduledMessage(ui[1], ui[2], ui[3], conf_list).printSchedMessage();
            case Message.CANCEL_1_CODE:
                return new CancelMessageI(ui[1]).printCancelIMessage();
            case Message.NOT_SCHEDULED_CODE:
                ArrayList<String> nscheq_list = getParticipantsStrings(ui[5]);
                return new NotScheduledMessage(ui[1], ui[2], ui[3], Integer.valueOf(ui[4]), nscheq_list, ui[6]).printNotSchedMessage();
            case Message.CANCEL_2_CODE:
                return new CancelMessageII(ui[1]).printCancelIIMessage();
            case Message.WITHDRAW_1_CODE:
                return new WithdrawMessageI(ui[1], ui[2]).printWithdrawIMessage();
            case Message.WITHDRAW_2_CODE:
                return new WithdrawMessageII(ui[1]).printWithdrawIIMessage();
            case Message.ADD_CODE:
                return new AddMessage(ui[1]).printAddMessage();
            case Message.ADDED_CODE:
                return new AddedMessage(ui[1], ui[2]).printAddedMessage();
            case Message.ROOM_CHANGE_CODE:
                return new RoomChangeMessage(ui[1], ui[2]).printRoomChangeMessage();
            case Message.STOP_CONNECTION:
                return new String("0000");
            default:
                LOGGER.warning("You have entered a wrong code try again please");
                return "Invalid Message";
        }
    }

    //TODO complete the logic
    public static String processingServer (String o, String server, String requester) throws IOException, ParseException, SQLException {
        Object obj = null;

        try {
            obj = parsingMessage(o, requester);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (obj instanceof RequestMessage) {
            // Create a temporary invitation ready to be used/sent in case reservation is successful
            InviteMessage newInvite = new InviteMessage(server, ((RequestMessage) obj).getRQ_DATE(),
                    ((RequestMessage) obj).getRQ_TIME(), ((RequestMessage) obj).getRQ_TOPIC(), requester);

            // Check is the room is available&reserved
            boolean isReserved = RoomsUtility.reserveRoom(((RequestMessage) obj).getRQ_DATE(),
                    ((RequestMessage) obj).getRQ_TIME(), newInvite.getMT_NUMBER());

            if (isReserved) {
                // the mysql insert statement
                String query = "INSERT INTO InviteMessage(MEETINGNUMBER, DATEINSERTED, MEETINGTIME, TOPIC, REQUESTER,REQUESTNUMBER)"
                        + " VALUES (?, ?, ?, ?, ?,?)";
                insertMessage(query, Message.INVITE_CODE, newInvite, ((RequestMessage) obj).getRQ_NUMBER());

                String list = ((RequestMessage)obj).getLIST_OF_PARTICIPANTS().toString();
                String[] lOP = list.replace("[", "").replace("]", "").split(",");

                String msgToSend = newInvite.printInvMessage();
                // send Message to list of Confirmed Participants
                sendUdpPacketToLOP(lOP, UdpServer.getSocket(), msgToSend);

                TIMER(msgToSend, UdpServer.getSocket(), 1);
            }
            else {
                // reset the invite count to the previous value
                int cache = messageCount(server, newInvite.getCURR_MT_NUM(), false);

                // Create a response message
                ResponseMessage newResponse = new ResponseMessage(((RequestMessage) obj).getRQ_NUMBER());

                // the mysql insert statement
                String query = "INSERT INTO ResponseMessage(REQUESTNUMBER)"
                        + " VALUES (?)";
                insertMessage(query, Message.RESPONSE_CODE, newResponse, null);

                String msgToSend = newResponse.printRespMessage();

                // send a message to the requester if reservation was not successful return Response Message
                sendUdpPacket(msgToSend, getPortByClientName(requester), UdpServer.getSocket(), requester);

            }

        }
        else if (obj instanceof AcceptMessage) {

            //Prepare the accept message
            AcceptMessage newAccept = new AcceptMessage(((AcceptMessage) obj).getMT_NUMBER());

            String q1 = "SELECT whorejected"
                    + " FROM RejectMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newAccept.getMT_NUMBER());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 ResultSet res = pstmt.executeQuery()) {
                String q2 = null;

                if (!res.next()) {
                    //Insert into the DB whoaccepted
                    String queryA = "INSERT INTO AcceptMessage(MEETINGNUMBER, WHOACCEPTED)"
                            + " VALUES (?, ?)";
                    insertMessage(queryA, Message.ACCEPT_CODE, newAccept, requester);

                    q2 = "INSERT INTO ParticipantsConfirmed (MEETINGNUMBER, WHO)"
                            + " VALUES (" + fmtStrDB(newAccept.getMT_NUMBER()) + "," + fmtStrDB(requester) + ")";
                } else { }
                if ( q2 != null)
                {
                    PreparedStatement querystatement = conn.prepareStatement(q2);
                    querystatement.execute();
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof RejectMessage) {

            //Prepare the reject message
            RejectMessage newReject = new RejectMessage(((RejectMessage) obj).getMT_NUMBER());

            String q1 = "SELECT whoaccepted"
                    + " FROM AcceptMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newReject.getMT_NUMBER());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 ResultSet res = pstmt.executeQuery()) {
                String q2 = null;

                if (!res.next()) {
                    //Insert into the DB whorejected
                    String queryR = "INSERT INTO RejectMessage(MEETINGNUMBER, WHOREJECTED)"
                            + " VALUES (?, ?)";
                    insertMessage(queryR, Message.REJECT_CODE, newReject, requester);

                    q2 = "INSERT INTO ParticipantsConfirmed (MEETINGNUMBER, WHO, CONFIRMED)"
                            + " VALUES (" + fmtStrDB(newReject.getMT_NUMBER()) + "," + fmtStrDB(requester) + false + ")";
                } else { }
                if ( q2 != null) {
                    PreparedStatement querystatement = conn.prepareStatement(q2);
                    querystatement.execute();
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof CancelMessageII) {
            // Prepare the cancel message
            CancelMessageII newCancel = new CancelMessageII(((CancelMessageII) obj).getMT_NUMBER());

            String q1 = "SELECT whoaccepted"
                    + " FROM AcceptMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newCancel.getMT_NUMBER());
            String q2 = "SELECT COUNT(DISTINCT MEETINGNUMBER)"
                    + " FROM ConfirmMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newCancel.getMT_NUMBER());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 PreparedStatement pstmt2 = conn.prepareStatement(q2);
                 ResultSet res = pstmt.executeQuery();
                 ResultSet res2 = pstmt2.executeQuery()) {

                String[] cleanUpQueries = new String[2];

                String lOfConfPart = "";
                if (res2.next()) {
                    if (res2.getInt(1) == 1 && res.next()) {

                        lOfConfPart += res.getString(1);
                        while (res.next()) {
                            lOfConfPart += "," + res.getString(1);
                        }
                        String[] result = lOfConfPart.split(",");

                        sendUdpPacketToLOP(result, UdpServer.getSocket(), newCancel.printCancelIIMessage());

                        //Insert into the DB who caneled
                        String queryA = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)"
                                + " VALUES (?, ?)";
                        insertMessage(queryA, Message.CANCEL_2_CODE, newCancel, requester);

                        cleanUpQueries[0] = "UPDATE ParticipantsConfirmed SET confirmed = false WHERE meetingnumber =" + fmtStrDB(newCancel.getMT_NUMBER());
                        cleanUpQueries[1] = "DELETE FROM ROOMRESERVATION WHERE MEETINGNUMBER =" + fmtStrDB(newCancel.getMT_NUMBER());

                    } else {
                    }
                    if (cleanUpQueries[0] != null) {
                        Statement sts = conn.createStatement();
                        for (String s : cleanUpQueries) {
                            sts.addBatch(s);
                        }
                        sts.executeBatch();

                        // TODO - change local agenda
                        conn.close();

                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof WithdrawMessageII) {
            //Prepare the reject message
            WithdrawMessageII newWithdrawC = new WithdrawMessageII(((WithdrawMessageII) obj).getMT_NUMBER());
            String newWithdrawS = new WithdrawMessageI(newWithdrawC.getMT_NUMBER(), requester).printWithdrawIMessage();

            // get the requester and the minimum number of participants
            String q1="SELECT DATEINSERTED, MEETINGTIME, TOPIC, REQUESTER, MINIMUM"
                    + " FROM InviteMessage"
                    + " INNER JOIN REQUESTMESSAGE ON INVITEMESSAGE.REQUESTNUMBER = REQUESTMESSAGE.REQUESTNUMBER"
                    + " WHERE MEETINGNUMBER=" + fmtStrDB(newWithdrawC.getMT_NUMBER());
            // get all the confirmed participants for that meeting
            String q2 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newWithdrawC.getMT_NUMBER())
                    + " AND CONFIRMED = TRUE";
            // get those who never accepted the invitation
            String q3 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(newWithdrawC.getMT_NUMBER())
                    + " AND CONFIRMED = FALSE";
            try(Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(q1);
                PreparedStatement pstmt2 = conn.prepareStatement(q2);
                PreparedStatement pstmt3 = conn.prepareStatement(q3);
                ResultSet res = pstmt.executeQuery();
                ResultSet res2 = pstmt2.executeQuery();
                ResultSet res3 = pstmt3.executeQuery()){

                String[] queries = new String[2];

                String lOfConfPart = "";
                int minimum_ = -1;
                String date_ = null;
                String time_ = null;
                String topic_ = null;
                String requester_ = null;

                // Do this block if he is not the requester
                if (res.next() && res2.next()) {
                    int numOfConfPart = 0;

                    lOfConfPart += res2.getString(1);

                    while (res2.next()) {
                        lOfConfPart += "," + res2.getString(1);
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    numOfConfPart = lOfConfPart.split(",").length;
                    date_ = dateFormat.format(res.getDate(1));
                    time_ = timeFormat.format(res.getTime(2));
                    topic_ = res.getString(3);
                    requester_ = res.getString(4);
                    minimum_ = res.getInt(5);

                    if(!requester.equals(requester_) && lOfConfPart.contains(requester)) {
                        //Insert into the DB whowithdrew
                        String queryW = "INSERT INTO WithdrawMessage(MEETINGNUMBER, WHOWITHDRAWED)"
                                + " VALUES (?, ?)";
                        insertMessage(queryW, Message.WITHDRAW_2_CODE, newWithdrawC, requester);

                        queries[0] = "DELETE FROM ParticipantsConfirmed"
                                + " WHERE MEETINGNUMBER = " + fmtStrDB(newWithdrawC.getMT_NUMBER())
                                + " AND WHO = " + fmtStrDB(requester);
                        queries[1] = "DELETE FROM Bookings"
                                + " WHERE MEETINGNUMBER = " + fmtStrDB(newWithdrawC.getMT_NUMBER())
                                + " AND WHO = " + fmtStrDB(requester);

                        Statement sts = conn.createStatement();
                        for (String s : queries) {
                            sts.addBatch(s);
                        }
                        sts.executeBatch();

                        sendUdpPacket(newWithdrawS, getPortByClientName(requester_), UdpServer.getSocket(), requester_);

                        if (numOfConfPart - 1 < minimum_) {

                            String lOfNotConfPart = "";
                            if (res3.next()) {
                                lOfNotConfPart += res2.getString(1);

                                while (res3.next()) {
                                    lOfNotConfPart += "," + res2.getString(1);
                                }
                            }

                            // Retrieve the invite message
                            InviteMessage newInvite = new InviteMessage(newWithdrawC.getMT_NUMBER(), date_, time_, topic_, requester_);

                            String msgToSend = newInvite.printInvMessage();

                            // send Message to list of Confirmed Participants
                            sendUdpPacketToLOP(lOfNotConfPart.split(","), UdpServer.getSocket(), msgToSend);

                            TIMER(msgToSend, UdpServer.getSocket(), 2);
                        }
                    }
                } else { }
                conn.close();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else if (obj instanceof AddMessage) {
            //Add people who were invited
            // TODO - check if the room reservation still exist
            // TODO - check if the he is part of the list of participants in the request message
            // TODO - add him to the participants confirmed
            // TODO - send a message to the requester (added) and update the local agenda
        } else if (obj instanceof RoomChangeMessage) {
            //maintance
        } else {
        }
        return null;
    }

    // TODO for client processing
    public static String processingClient (Object o, String server, String requester) throws IOException, ParseException {
        Object obj = null;
        try {
            obj = parsingMessage(o.toString(),null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (obj instanceof ResponseMessage) {
        } else if (obj instanceof InviteMessage) {
            // TODO - check the local agenda and reject if he is already booked,
            // TODO - else show it to accept on behalf
        } else if (obj instanceof ConfirmMessage) {
        } else if (obj instanceof ScheduledMessage) {
        } else if (obj instanceof CancelMessageI) {
        } else if (obj instanceof NotScheduledMessage) {
        } else if (obj instanceof WithdrawMessageI) {
        } else if (obj instanceof AddedMessage) {
        } else { }
        return null;
    }

    /* String format for DB */
    public static String fmtStrDB (String s) {
        if(s == "")
            return null;
        else
            return "\'" + s + "\'";
    }

    /* Updates the count for every request/meeting number and save its state */
    public static int messageCount(String myIp, int ct, boolean increment) {
        String q1 = "SELECT MCOUNT"
                + " FROM MessageCount"
                + " WHERE WHO = " + fmtStrDB(myIp);
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             ResultSet res = pstmt.executeQuery()) {
            String q2 = null;

            if (!res.next()) {
                q2 = "INSERT INTO MessageCount (WHO)"
                        + " VALUES (" + fmtStrDB(myIp) + ")";
            } else {
                String result = res.getString(1);
                ct = (increment) ? Integer.parseInt(result) + 1 : Integer.parseInt(result) - 1;
                q2 = "UPDATE MessageCount"
                        + " SET MCOUNT = " + fmtStrDB(String.valueOf(ct))
                        + " WHERE WHO = " + fmtStrDB(myIp);
            }
            conn.prepareStatement(q2).execute();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ct;
    }

    public static void executedDB(String query) throws SQLException {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.execute();
        conn.close();
    }

    /**
     * This is to send a packet to the list of participants
     *
     * @param buf_s
     * @param port
     * @param ds
     * @param s
     * @throws IOException
     */
    public static void sendUdpPacketStringPort(byte[] buf_s, String port, DatagramSocket ds, String s) throws IOException {
        InetAddress ipLOP = InetAddress.getByName(s.trim());
        DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, Integer.valueOf(port));
        ds.send(DpSend);
    }

    /**
     * This for when we want to send invite messages
     *
     * @param msgToSend
     * @param ds
     */
    public static void followUpLogicForInviteMessage(String msgToSend, DatagramSocket ds, int option) {
        String[] msgArgs = msgToSend.replaceAll(" ", "").replaceAll(".+\\{", "").split("\\||\\}");

        String q1 = "SELECT WHO"
                + " FROM ParticipantsConfirmed"
                + " WHERE MEETINGNUMBER = " + fmtStrDB(msgArgs[1]) + " AND CONFIRMED = TRUE";

        String q2 = "SELECT REQUESTMESSAGE.REQUESTNUMBER , ROOMNUMBER , REQUESTER, MINIMUM  from INVITEMESSAGE"
                + " INNER JOIN ROOMRESERVATION ON INVITEMESSAGE.MEETINGNUMBER = ROOMRESERVATION.MEETINGNUMBER"
                + " INNER JOIN REQUESTMESSAGE ON INVITEMESSAGE.REQUESTNUMBER = REQUESTMESSAGE.REQUESTNUMBER"
                + " WHERE INVITEMESSAGE.MEETINGNUMBER = " + fmtStrDB(msgArgs[1]);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             PreparedStatement pstmt2 = conn.prepareStatement(q2);
             ResultSet res = pstmt.executeQuery();
             ResultSet res2 = pstmt2.executeQuery()) {

            String[] queries = new String[2];

            String lOfConfPart = "";
            String Requestnumberquery = null;
            String Minimumparticipants = null;
            String Requester = null;
            String Roomnumber = null;

            if (res.next() && option == 1) {
                int numOfConfPart = 0;

                lOfConfPart += res.getString(1);

                while (res.next()) {
                    lOfConfPart += "," + res.getString(1);
                }

                numOfConfPart = lOfConfPart.split(",").length;

                if (res2.next()) {

                    Requestnumberquery = res2.getString(1);
//                    System.out.println(res2.getString(1) + "1");
                    Roomnumber =res2.getString(2);
//                    System.out.println("Roomnumber is :" + Roomnumber);
                    Requester =res2.getString(3);
//                    System.out.println("Requester is :" + Requester);
                    Minimumparticipants = res2.getString(4);
//                    System.out.println(res2.getString(4) + "4");


                    // logic for the forming and sending the confirmation to list of participants
                    // and scheduled to requester
                    System.out.println("Result is :" + numOfConfPart + " Minimum is:" + Minimumparticipants);

                    if (numOfConfPart >= Integer.valueOf(Minimumparticipants)) {

                        // Queries
                        queries[0] = "INSERT INTO ScheduledMessage(REQUESTNUMBER, MEETINGNUMBER, ROOMNUMBER, LISTOFCONFIRMEDPARTICIPANTS)"
                                + " VALUES (" + fmtStrDB(Requestnumberquery) + "," + fmtStrDB(msgArgs[1]) + ", " + fmtStrDB(Roomnumber) + "," + fmtStrDB(lOfConfPart)
                                + ")";
                        queries[1] = "INSERT INTO ConfirmMessage(MEETINGNUMBER, ROOMNUMBER)" + " VALUES (" + fmtStrDB(msgArgs[1])
                                + ", " + fmtStrDB(Roomnumber) + ")";

                        // LIST_OF_CONFIRMED_PARTICIPANTS
                        ArrayList<String> ListofConfParticipants = new ArrayList<String>(Arrays.asList(lOfConfPart));
                        ScheduledMessage ScheduledMsg = new ScheduledMessage(Requestnumberquery, msgArgs[1], Roomnumber, ListofConfParticipants);
                        // String MT_NUMBER, String ROOM_NUMBER
                        ConfirmMessage ConfirmMsg = new ConfirmMessage(msgArgs[1], Roomnumber);

                        // Scheduled Message
                        String currMsg = ScheduledMsg.printSchedMessage();

                        // Confirm message
                        String currMsg2 = ConfirmMsg.printConfMessage();
                        String[]arrayoflOfConfPart =lOfConfPart.split(",");

                        //Send Message to list of Confirmed Participants
                        sendUdpPacketToLOP(arrayoflOfConfPart, ds, currMsg2);

                        // send a message to the requester
                        sendUdpPacket(currMsg, getPortByClientName(Requester), ds, Requester);
                    }
                }
            } else {
                // LIST_OF_CONFIRMED_PARTICIPANTS
                ArrayList<String> ListofConfParticipants = new ArrayList<String>(Arrays.asList(lOfConfPart));

                while (res2.next()) {
                    Requestnumberquery = res2.getString(1);
                    Requester = res2.getString(3);
                    Minimumparticipants = res2.getString(4);
                }

                if (option == 1) {
                    if (lOfConfPart == "") {
                        queries[0] = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, TOPIC)"
                                + " VALUES (" + fmtStrDB(Requestnumberquery) + "," + fmtStrDB(msgArgs[2])
                                + "," + fmtStrDB(msgArgs[3]) + "," + Minimumparticipants
                                + "," + fmtStrDB(msgArgs[4]) + ")";
                    } else {
                        queries[0] = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, LISTOFCONFIRMEDPARTICIPANTS, TOPIC)"
                                + " VALUES (" + fmtStrDB(Requestnumberquery) + "," + fmtStrDB(msgArgs[2])
                                + "," + fmtStrDB(msgArgs[3]) + "," + Minimumparticipants
                                + "," + fmtStrDB(lOfConfPart) + "," + fmtStrDB(msgArgs[4]) + ")";
                    }

                    NotScheduledMessage NotScheduledMsg = new NotScheduledMessage(Requestnumberquery, msgArgs[2], msgArgs[3],
                            Integer.valueOf(Minimumparticipants), ListofConfParticipants, msgArgs[4]);

                    // NotScheduled message
                    String currMsg = NotScheduledMsg.printNotSchedMessage();

                    // send a message to the requester
                    sendUdpPacket(currMsg, getPortByClientName(Requester), ds,Requester);
                }

                queries[1] = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)" + " VALUES (" + fmtStrDB(msgArgs[1])
                        + "," + fmtStrDB(InetAddress.getLocalHost().getHostName()) + ")";

                CancelMessageI CancelMsg = new CancelMessageI(msgArgs[1]);

                // Cancel Message
                String currMsg2 = CancelMsg.printCancelIMessage();
                String[] arrayoflOfConfPart = lOfConfPart.split(",");

                // send Message to list of Confirmed Participants
                sendUdpPacketToLOP(arrayoflOfConfPart, ds, currMsg2);

                // send a message to the requester
                if (option == 2)
                    sendUdpPacket(currMsg2, getPortByClientName(Requester), ds,Requester);
            }
            Statement sts = conn.createStatement();
            for (String s : queries) {
                if (s != null)
                    sts.addBatch(s);
            }
            sts.executeBatch();

            conn.close();

            // TODO - make sure the appointment booking is done accordingly

        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * sends any message intended to the requester/organizer/server
     * @param msg
     * @param port
     * @param ds
     * @param s
     * @throws IOException
     */
    public static void sendUdpPacket(String msg, Integer port, DatagramSocket ds, String s) throws IOException {
        byte[] buf = msg.getBytes();
        InetAddress iP = InetAddress.getByName(s.trim());
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, iP, port);
        ds.send(DpSend);
    }

    /**
     * sends any message intended to the list of participants
     *
     * @param lOP
     * @param ds
     * @param msg
     * @throws IOException
     * @throws SQLException
     */
    public static void sendUdpPacketToLOP(String[] lOP, DatagramSocket ds, String msg) throws IOException, SQLException {
        for (int i = 0; i < lOP.length; i++) {
            if(!lOP[i].equals("")) {
                InetAddress ipLOP = InetAddress.getByName(lOP[i].trim());
                byte[] buf_s = msg.getBytes();
                DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, getPortByClientName(lOP[i]));
                ds.send(DpSend);
            }
        }
    }

    /**
     * return the client name of the IP Address
     *
     * @param s
     * @return
     * @throws SQLException
     */
    public static String getClientNameFromDB(String s) throws SQLException {
        String query = "SELECT CLIENTNAME from REGISTRATION  WHERE IPADDRESS= " + fmtStrDB(s.replace("/", ""));
        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet res = pstmt.executeQuery();
            conn.close();
            while (res.next())
                return res.getString(1);
        } catch (SQLException e) {
            System.out.println(e);
        }

        return null;
    }

    /**
     * gets the port by IP Address
     *
     * @param s
     * @return
     * @throws SQLException
     */
    public static Integer getPortByIP(String s) throws SQLException {
        String query = "SELECT LISTENINGPORT FROM REGISTRATION WHERE IPADDRESS = " + fmtStrDB(s);
        return getPort(query);
    }

    /**
     * gets the port by Client name
     *
     * @param s
     * @return
     * @throws SQLException
     */
    public static Integer getPortByClientName(String s) throws SQLException {
        String query = "SELECT LISTENINGPORT FROM REGISTRATION WHERE CLIENTNAME = " + fmtStrDB(s);
        return getPort(query);
    }

    /**
     * gets port from database depending on the query
     * @param query
     * @return
     */
    public static Integer getPort(String query) {
        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet res = pstmt.executeQuery();
            conn.close();

            while (res.next())
                return Integer.valueOf(res.getString(1));
        } catch (SQLException e) {
            System.out.println(e);
        }

        return null;
    }

    /**
     * starts the timer after sending an invite message to the list of participants
     *
     * @param msgToSend
     * @param ds
     */
    public static void TIMER(String msgToSend, DatagramSocket ds, int option) {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(20000);
                    followUpLogicForInviteMessage(msgToSend, ds, option);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        });
        timer.start();
    }

    /**
     * Datasbase Connection
     */
    public static void establishDBConnection() {
        System.out.println("Trying to Establish Database Connection.....");
        Connection conn3 = null;

        try {
            // Connect method
            String dbURL3 = url;
            Properties parameters = new Properties();
            parameters.put("user", user);
            parameters.put("password", password);

            conn3 = DriverManager.getConnection(dbURL3, parameters);
            if (conn3 != null) {
                System.out.println("Connected to database");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn3 != null && !conn3.isClosed()) {
                    conn3.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
