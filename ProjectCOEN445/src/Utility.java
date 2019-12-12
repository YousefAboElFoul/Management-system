import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class Utility {
    // Database parameters
    public static final String url = "jdbc:postgresql://ec2-54-235-92-244.compute-1.amazonaws.com:5432/d70m64dg1qc8fu?sslmode=require";
    public static final String user = "gkcmczoxettaer";
    public static final String password = "8d4b50fd5a522fd0256536f4f6993a61fad200dde8d58372c1200b5e63cfe694";
    public static final String[] Codes = {"1110","1111","2222","3330","3331","4444","5550","5551","6660","6661","7770","7771","8880","8881","9999","0000"};
    private final static Logger LOGGER = Logger.getLogger(Utility.class.getName());

    /* This for parsing messages through udp */
    @SuppressWarnings("uncheked")
    public static <T> T parsingMessage(String in, String from, int option) throws IOException, SQLException {
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
        if (query != null && option == 2) {
            if (mCode == Message.REQUEST_CODE || mCode == Message.ADD_CODE || mCode == Message.ROOM_CHANGE_CODE)
                insertMessage(query, mCode, messageReceived, from);
        }

        return (messageReceived != null) ? messageReceived : (T) "Error occurred in the request message";

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
            conn.close();
        }
        catch (SQLException | ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /* This for parsing the list of participants */
    private static ArrayList<String> getParticipantsStrings (String s) {
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
        if (Arrays.asList(Codes).contains(ui[0])) {
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
                if (ui[2].equals(RoomsUtility.ROOM_ONE) || ui[2].equals(RoomsUtility.ROOM_TWO))
                    return new RoomChangeMessage(ui[1], ui[2]).printRoomChangeMessage();
                return "Invalid Message";
            case Message.STOP_CONNECTION:
                return "0000";
            default:
                LOGGER.warning("You have entered a wrong code try again please");
                return "Invalid Message";
        }
    }

    /* Processing on the server side */
    public static String processingServer (String o, String server, String person) throws IOException, ParseException, SQLException {
        Object obj = null;

        try {
            obj = parsingMessage(o, person, 2);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (obj instanceof RequestMessage) {
            // Retrieve current object message
            RequestMessage currReq = (RequestMessage) obj;

            String reqnum_ = currReq.getRQ_NUMBER();
            String date_ = currReq.getRQ_DATE();
            String time_ = currReq.getRQ_TIME();

            // Create a temporary invitation ready to be used/sent in case reservation is successful
            InviteMessage newInvite = new InviteMessage(server, date_, time_, currReq.getRQ_TOPIC(), person);

            String currMT = newInvite.getMT_NUMBER();

            // Check is the room is available&reserved
            String[] reservation = RoomsUtility.reserveRoom(date_, time_, currMT);
            boolean isReserved = Boolean.valueOf(reservation[0]);

            if (isReserved) {

                String roomnum_ = reservation[1];

                // the mysql insert statement
                String queryI = "INSERT INTO InviteMessage(MEETINGNUMBER, DATEINSERTED, MEETINGTIME, TOPIC, REQUESTER, REQUESTNUMBER)"
                        + " VALUES (?, ?, ?, ?, ?, ?)";
                insertMessage(queryI, Message.INVITE_CODE, newInvite, reqnum_);

                String queryB = "INSERT INTO Bookings(CLIENTNAME, ROOMNUMBER, MEETINGNUMBER, DATEINSERTED, START_TIME)"
                        + " VALUES (" + fmtStrDB(person) + "," + fmtStrDB(roomnum_) + ","
                        + fmtStrDB(currMT) + "," + fmtStrDB(date_) + "," + fmtStrDB(time_) + ")";

                executedDB(queryB);

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
                ResponseMessage newResponse = new ResponseMessage(currReq.getRQ_NUMBER());

                // the mysql insert statement
                String query = "INSERT INTO ResponseMessage(REQUESTNUMBER)"
                        + " VALUES (?)";
                insertMessage(query, Message.RESPONSE_CODE, newResponse, null);

                String msgToSend = newResponse.printRespMessage();

                // send a message to the requester if reservation was not successful return Response Message
                sendUdpPacket(msgToSend, getPortByClientName(person), UdpServer.getSocket(), person);

            }

        }
        else if (obj instanceof AcceptMessage) {
            // Retrieve the current object message
            AcceptMessage currAcpt = (AcceptMessage) obj;

            String currMT = currAcpt.getMT_NUMBER();

            String q1 = "SELECT WHOREJECTED"
                    + " FROM RejectMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND WHOREJECTED = " + fmtStrDB(person);
            String q2 = "SELECT *"
                    + " FROM RoomReservation"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT);
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 PreparedStatement pstmt2 = conn.prepareStatement(q2);
                 ResultSet res = pstmt.executeQuery();
                 ResultSet res2 = pstmt2.executeQuery()) {

                String[] queries = new String[2];

                if (!res.next() && res2.next()) {
                    //Insert into the DB whoaccepted
                    String queryA = "INSERT INTO AcceptMessage(MEETINGNUMBER, WHOACCEPTED)"
                            + " VALUES (?, ?)";
                    insertMessage(queryA, Message.ACCEPT_CODE, currAcpt, person);

                    queries[0] = "INSERT INTO ParticipantsConfirmed (MEETINGNUMBER, WHO)"
                            + " VALUES (" + fmtStrDB(currMT) + "," + fmtStrDB(person) + ")";
                    queries[1] = "INSERT INTO Bookings(CLIENTNAME, ROOMNUMBER, MEETINGNUMBER, DATEINSERTED, START_TIME)"
                            + " VALUES (" + fmtStrDB(person) + "," + fmtStrDB(res2.getString(2)) + ","
                            + fmtStrDB(currMT) + "," + fmtStrDB(res2.getString(3)) + ","
                            + fmtStrDB(res2.getString(4)) + ")";
                } else { }

                executeMultipleQ(conn, queries);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof RejectMessage) {
            // Retrieve the current object message
            RejectMessage currRej = (RejectMessage) obj;

            String currMT = currRej.getMT_NUMBER();

            String q0 = "SELECT whoaccepted"
                    + " FROM AcceptMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND WHOACCEPTED = " + fmtStrDB(person);
            String q1 = "SELECT *"
                    + " FROM RoomReservation"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT);

            try (Connection conn = connect();
                 PreparedStatement pstmt0 = conn.prepareStatement(q0);
                 PreparedStatement pstmt1 = conn.prepareStatement(q1);
                 ResultSet res0 = pstmt0.executeQuery();
                 ResultSet res1 = pstmt1.executeQuery()) {

                String q2 = null;

                if (!res0.next() && res1.next()) {
                    //Insert into the DB whorejected
                    String queryR = "INSERT INTO RejectMessage(MEETINGNUMBER, WHOREJECTED)"
                            + " VALUES (?, ?)";
                    insertMessage(queryR, Message.REJECT_CODE, currRej, person);

                    q2 = "INSERT INTO ParticipantsConfirmed (MEETINGNUMBER, WHO, CONFIRMED)"
                            + " VALUES (" + fmtStrDB(currMT) + ", " + fmtStrDB(person) + ", FALSE)";
                } else { }
                if ( q2 != null) {
                    conn.close();
                    executedDB(q2);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof CancelMessageII) {
            // Retrieve the current object message
            CancelMessageII currCancel = (CancelMessageII) obj;

            String currMT = currCancel.getMT_NUMBER();

            String q1 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND CONFIRMED = TRUE";
            String q2 = "SELECT COUNT(DISTINCT MEETINGNUMBER)"
                    + " FROM ConfirmMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT);
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 PreparedStatement pstmt2 = conn.prepareStatement(q2);
                 ResultSet res = pstmt.executeQuery();
                 ResultSet res2 = pstmt2.executeQuery()) {

                String[] cleanUpQueries = new String[3];

                String lOfConfPart = "";
                if (res2.next()) {
                    if (res2.getInt(1) == 1 && res.next()) {

                        lOfConfPart += res.getString(1);
                        while (res.next()) {
                            lOfConfPart += "," + res.getString(1);
                        }
                        String[] result = lOfConfPart.split(",");

                        sendUdpPacketToLOP(result, UdpServer.getSocket(), currCancel.printCancelIIMessage());

                        //Insert into the DB who canceled
                        String queryA = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)"
                                + " VALUES (?, ?)";
                        insertMessage(queryA, Message.CANCEL_2_CODE, currCancel, person);

                        cleanUpQueries[0] = "UPDATE ParticipantsConfirmed SET CONFIRMED = FALSE WHERE MEETINGNUMBER =" + fmtStrDB(currMT);
                        cleanUpQueries[1] = "DELETE FROM RoomReservation WHERE MEETINGNUMBER =" + fmtStrDB(currMT);
                        cleanUpQueries[2] = "DELETE FROM Bookings WHERE MEETINGNUMBER = " + fmtStrDB(currMT);

                    } else { }

                    executeMultipleQ(conn, cleanUpQueries);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof WithdrawMessageII) {
            // Retrieve the current object message
            WithdrawMessageII currWithdraw = (WithdrawMessageII) obj;

            String currMT = currWithdraw.getMT_NUMBER();

            // Prepare the withdraw message to be sent
            String newWithdrawS = new WithdrawMessageI(currMT, person).printWithdrawIMessage();

            // get the scheduled meeting
            String q0="SELECT MEETINGNUMBER"
                    + " FROM ScheduledMessage "
                    + " WHERE MEETINGNUMBER=" + fmtStrDB(currMT);
            // get the meeting details
            String q1="SELECT I.DATEINSERTED, I.MEETINGTIME, I.TOPIC, REQUESTER, MINIMUM"
                    + " FROM InviteMessage I"
                    + " INNER JOIN REQUESTMESSAGE R ON I.REQUESTNUMBER = R.REQUESTNUMBER"
                    + " WHERE MEETINGNUMBER=" + fmtStrDB(currMT);
            // get all the confirmed participants for that meeting
            String q2 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND CONFIRMED = TRUE";
            // get those who never accepted the invitation
            String q3 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND CONFIRMED = FALSE";

            try(Connection conn = connect();
                PreparedStatement pstmt0 = conn.prepareStatement(q0);
                PreparedStatement pstmt1 = conn.prepareStatement(q1);
                PreparedStatement pstmt2 = conn.prepareStatement(q2);
                PreparedStatement pstmt3 = conn.prepareStatement(q3);
                ResultSet res0 = pstmt0.executeQuery();
                ResultSet res1 = pstmt1.executeQuery();
                ResultSet res2 = pstmt2.executeQuery();
                ResultSet res3 = pstmt3.executeQuery()) {

                String[] queries = new String[2];

                String lOfConfPart = "";
                int minimum_ = -1;
                String date_ = null;
                String time_ = null;
                String topic_ = null;
                String requester_ = null;

                // Do this block if he is not the requester
                if (res0.next() && res1.next() && res2.next()) {
                    int numOfConfPart = 0;

                    lOfConfPart += res2.getString(1);

                    while (res2.next()) {
                        lOfConfPart += "," + res2.getString(1);
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    numOfConfPart = lOfConfPart.split(",").length;
                    date_ = dateFormat.format(res1.getDate(1));
                    time_ = timeFormat.format(res1.getTime(2));
                    topic_ = res1.getString(3);
                    requester_ = res1.getString(4);
                    minimum_ = res1.getInt(5);


                    Date currDT = new Date();
                    Date mtDT = new Date(date_ + " " + time_);


                    if(!person.equals(requester_) && lOfConfPart.contains(person) && (currDT.compareTo(mtDT) < 0)) {
                        //Insert into the DB whowithdrew
                        String queryW = "INSERT INTO WithdrawMessage(MEETINGNUMBER, WHOWITHDRAWED)"
                                + " VALUES (?, ?)";
                        insertMessage(queryW, Message.WITHDRAW_2_CODE, currWithdraw, person);

                        queries[0] = "DELETE FROM ParticipantsConfirmed"
                                + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT) + " AND WHO = " + fmtStrDB(person);
                        queries[1] = "DELETE FROM Bookings"
                                + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT) + " AND CLIENTNAME = " + fmtStrDB(person);

                        executeMultipleQ(conn, queries);

                        sendUdpPacket(newWithdrawS, getPortByClientName(requester_), UdpServer.getSocket(), requester_);

                        if (minimum_ != -1) {
                            if (numOfConfPart - 1 < minimum_) {

                                String lOfNotConfPart = "";
                                if (res3.next()) {
                                    lOfNotConfPart += res2.getString(1);

                                    while (res3.next()) {
                                        lOfNotConfPart += "," + res2.getString(1);
                                    }
                                }

                                // Retrieve the invite message
                                InviteMessage newInvite = new InviteMessage(currMT, date_, time_, topic_, requester_);

                                String msgToSend = newInvite.printInvMessage();

                                // send Message to list of Unconfirmed Participants
                                sendUdpPacketToLOP(lOfNotConfPart.split(","), UdpServer.getSocket(), msgToSend);

                                TIMER(msgToSend, UdpServer.getSocket(), 2);
                            }
                        }
                    }
                } else { }
                conn.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        else if (obj instanceof AddMessage) {
            // Retrieve the current object message
            AddMessage currAdd = (AddMessage) obj;

            String currMT = currAdd.getMT_NUMBER();

            // Prepare the added message to be sent
            String newAddedS = new AddedMessage(currMT, person).printAddedMessage();

            // get the meeting details
            String q1="SELECT I.DATEINSERTED, I.MEETINGTIME, REQUESTER"
                    + " FROM InviteMessage I"
                    + " INNER JOIN REQUESTMESSAGE R ON I.REQUESTNUMBER = R.REQUESTNUMBER"
                    + " WHERE MEETINGNUMBER =" + fmtStrDB(currMT);
            // check if he first rejected that meeting
            String q2 = "SELECT WHOREJECTED"
                    + " FROM RejectMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND WHOREJECTED = " + fmtStrDB(person);
            // check if the meeting is part of scheduled table
            String q3 = "SELECT MEETINGNUMBER, ROOMNUMBER"
                    + " FROM ScheduledMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT);
            // check if the meeting was canceled
            String q4 = "SELECT MEETINGNUMBER"
                    + " FROM CancelMessage"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT);

            try(Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(q1);
                PreparedStatement pstmt2 = conn.prepareStatement(q2);
                PreparedStatement pstmt3 = conn.prepareStatement(q3);
                PreparedStatement pstmt4 = conn.prepareStatement(q4);
                ResultSet res = pstmt.executeQuery();
                ResultSet res2 = pstmt2.executeQuery();
                ResultSet res3 = pstmt3.executeQuery();
                ResultSet res4 = pstmt4.executeQuery()) {

                String[] queries = new String[3];

                String date_ = null;
                String time_ = null;
                String roomnum_ = null;
                String requester_ = null;
                String currMsg = null;

                if (res.next() && res2.next()) {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    date_ = dateFormat.format(res.getDate(1));
                    time_ = timeFormat.format(res.getTime(2));
                    requester_ = res.getString(3);

                    if(!person.equals(requester_)) {

                        if (res3.next()) {
                            roomnum_ = res3.getString(2);

                            queries[0] = "INSERT INTO ParticipantsConfirmed (MEETINGNUMBER, WHO)"
                                    + " VALUES (" + fmtStrDB(currMT) + "," + fmtStrDB(person) + ")"
                                    + " ON CONFLICT(MEETINGNUMBER, WHO) DO UPDATE SET "
                                    + " CONFIRMED = TRUE";

                            queries[1] = "UPDATE AddMessage SET WASADDED = TRUE"
                                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT) + " AND WHO = " + fmtStrDB(person);

                            queries[2] = "INSERT INTO Bookings(CLIENTNAME, ROOMNUMBER, MEETINGNUMBER, DATEINSERTED, START_TIME)"
                                    + " VALUES (" + fmtStrDB(person) + "," + fmtStrDB(roomnum_) + ","
                                    + fmtStrDB(currMT) + "," + fmtStrDB(date_) + "," + fmtStrDB(time_) + ")";

                            // To the organizer
                            sendUdpPacket(newAddedS, getPortByClientName(requester_), UdpServer.getSocket(), requester_);

                            // Retrieve the confirm message
                            ConfirmMessage ConfirmMsg = new ConfirmMessage(currMT, roomnum_);

                            // Confirm message
                            currMsg = ConfirmMsg.printConfMessage();

                        } else if (res4.next()) {
                            // Retrieve the cancel message
                            CancelMessageI CancelMsg = new CancelMessageI(currMT);

                            // Cancel Message
                            currMsg = CancelMsg.printCancelIMessage();
                        }

                        // To the participant
                        if (currMsg != null)
                            sendUdpPacket(currMsg, getPortByClientName(person), UdpServer.getSocket(), person);
                    }
                } else { }

                executeMultipleQ(conn, queries);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        else if (obj instanceof RoomChangeMessage) {
            // Retrieve the current object message
            RoomChangeMessage currRoomChange = (RoomChangeMessage) obj;

            String currMT = currRoomChange.getMT_NUMBER();

            // get the reservation details
            String q0="SELECT R.ROOMNUMBER, R.DATEINSERTED, R.START_TIME, REQUESTER"
                    + " FROM RoomReservation R"
                    + " INNER JOIN InviteMessage I ON I.MEETINGNUMBER = R.MEETINGNUMBER"
                    + " WHERE I.MEETINGNUMBER =" + fmtStrDB(currMT);
            // check if there exist another meeting reserved in another held on the same date&time
            String q1 = "SELECT R1.MEETINGNUMBER, R2.MEETINGNUMBER"
                    + " FROM RoomReservation R1, RoomReservation R2"
                    + " WHERE R1.ROOMNUMBER < R2.ROOMNUMBER"
                    + " AND R1.DATEINSERTED = R2.DATEINSERTED"
                    + " AND R1.START_TIME = R2.START_TIME";
            // get all the confirmed participants for that meeting
            String q2 = "SELECT WHO"
                    + " FROM ParticipantsConfirmed"
                    + " WHERE MEETINGNUMBER = " + fmtStrDB(currMT)
                    + " AND CONFIRMED = TRUE";

            try(Connection conn = connect();
                PreparedStatement pstmt0 = conn.prepareStatement(q0);
                PreparedStatement pstmt1 = conn.prepareStatement(q1);
                PreparedStatement pstmt2 = conn.prepareStatement(q2);
                ResultSet res0 = pstmt0.executeQuery();
                ResultSet res1 = pstmt1.executeQuery();
                ResultSet res2 = pstmt2.executeQuery()) {

                String currMsg = null;
                String lOfConfPart = "";
                String roomnumber_ = null;
                String date_ = null;
                String time_ = null;
                String requester_ = null;
                String newmeetingnum_ = "MAINT-" + currMT.split("-")[1];

                String[] queries = new String[2];

                if (res0.next() && res2.next()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    roomnumber_ = res0.getString(1);
                    date_ = dateFormat.format(res0.getDate(2));
                    time_ = timeFormat.format(res0.getTime(3));
                    requester_ = res0.getString(4);

                    lOfConfPart += res2.getString(1);

                    while (res2.next()) {
                        lOfConfPart += "," + res2.getString(1);
                    }
                }

                // Do this block if the room numbers are actually different
                if (!roomnumber_.equals(currRoomChange.getNEW_ROOM_NUMBER())) {

                    // Do this block if the newroom is free
                    if (!res1.next()) {
                        // Retrieve the roomchange message
                        currMsg = currRoomChange.printRoomChangeMessage();

                        queries[0] = "UPDATE RoomReservation SET ROOMNUMBER = "
                                + fmtStrDB(currRoomChange.getNEW_ROOM_NUMBER()) + "WHERE MEETINGNUMBER = " + fmtStrDB(currMT);

                    } else {
                        // Cancel Message
                        CancelMessageI CancelMsg = new CancelMessageI(currMT);

                        // Retrieve the cancel message
                        currMsg = CancelMsg.printCancelIMessage();

                        queries[0] = "DELETE FROM Bookings WHERE MEETINGNUMBER = " + fmtStrDB(currMT);
                    }

                    queries[1] = "INSERT INTO RoomReservation (ROOMNUMBER, DATEINSERTED, START_TIME, MEETINGNUMBER)"
                            + " VALUES (" + fmtStrDB(roomnumber_) + ", " + fmtStrDB(date_)
                            + ", " + fmtStrDB(time_) + ", " + fmtStrDB(newmeetingnum_) + ")"
                            + " ON CONFLICT(ROOMNUMBER, DATEINSERTED, START_TIME) DO UPDATE SET "
                            + "MEETINGNUMBER = " + fmtStrDB(newmeetingnum_) + ";";

                    // To the organizer
                    sendUdpPacket(currMsg, getPortByClientName(requester_), UdpServer.getSocket(), requester_);
                    // To list of Unconfirmed Participants
                    sendUdpPacketToLOP(lOfConfPart.split(","), UdpServer.getSocket(), currMsg);

                    executeMultipleQ(conn, queries);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        else { }
        return null;
    }

    /* Processing on the client side */
    public static String processingClient (String o, Integer serverPort, String server, String me) throws IOException, ParseException {
        Object obj = null;
        try {
            obj = parsingMessage(o, null, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (obj instanceof InviteMessage) {
            InviteMessage currInv = (InviteMessage) obj;

            String q1 = "SELECT CLIENTNAME"
                    + " FROM Bookings B"
                    + " INNER JOIN InviteMessage I"
                    + " ON B.DATEINSERTED = I.DATEINSERTED AND B.START_TIME = I.MEETINGTIME"
                    + " WHERE CLIENTNAME = " + fmtStrDB(me) + " AND I.MEETINGNUMBER = " + fmtStrDB(currInv.getMT_NUMBER());

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(q1);
                 ResultSet res = pstmt.executeQuery()) {

                String currMsg = null;
                if (res.next()) {
                    // Prepare the reject message
                    RejectMessage newRej = new RejectMessage(currInv.getMT_NUMBER());

                    currMsg = newRej.printRMessage();
                } else {
                    // Prepare the accept message
                    AcceptMessage newAcpt = new AcceptMessage(currInv.getMT_NUMBER());

                    currMsg = newAcpt.printAMessage();
                }

                sendUdpPacket(currMsg, serverPort, UdpClient.getSocket(), server);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } else { }
        return null;
    }

    /**
     * formats strings for DB
     * @param s
     * @return
     */
    public static String fmtStrDB (String s) {
        if(s == "")
            return null;
        else
            return "'" + s + "'";
    }

    /**
     * updates the count for every request/meeting number and save its state
     * @param myIp
     * @param ct
     * @param increment
     * @return
     */
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

            conn.close();
            executedDB(q2);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ct;
    }

    /**
     * executes an sql statement
     * @param query
     * @throws SQLException
     */
    public static void executedDB(String query) throws SQLException {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.execute();
        conn.close();
    }

    /**
     * executes multiple statements in one shot
     * @param conn
     * @param queries
     * @throws SQLException
     */
    public static void executeMultipleQ(Connection conn, String[] queries) throws SQLException {
        Statement sts = conn.createStatement();
        for (String s : queries) {
            if (s != null)
                sts.addBatch(s);
        }
        sts.executeBatch();

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

        String q2 = "SELECT Req.REQUESTNUMBER , ROOMNUMBER , REQUESTER, MINIMUM  from INVITEMESSAGE I"
                + " INNER JOIN RoomReservation R ON I.MEETINGNUMBER = R.MEETINGNUMBER"
                + " INNER JOIN RequestMessage Req ON I.REQUESTNUMBER = Req.REQUESTNUMBER"
                + " WHERE I.MEETINGNUMBER = " + fmtStrDB(msgArgs[1]);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             PreparedStatement pstmt2 = conn.prepareStatement(q2);
             ResultSet res = pstmt.executeQuery();
             ResultSet res2 = pstmt2.executeQuery()) {

            String[] queries = new String[4];

            String lOfConfPart = "";
            String Requestnumberquery = null;
            String Minimumparticipants = null;
            String Requester = null;
            String Roomnumber = null;

            int numOfConfPart = 0;

            if (res.next()) {

                lOfConfPart += res.getString(1);

                while (res.next()) {
                    lOfConfPart += "," + res.getString(1);
                }

                numOfConfPart = lOfConfPart.split(",").length;
            }

            if (res2.next()) {

                Requestnumberquery = res2.getString(1);
                Roomnumber =res2.getString(2);
                Requester =res2.getString(3);
                Minimumparticipants = res2.getString(4);


                // logic for the forming and sending the confirmation to list of participants
                // and scheduled to requester

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
                else {
                    // LIST_OF_CONFIRMED_PARTICIPANTS
                    ArrayList<String> ListofConfParticipants = new ArrayList<String>(Arrays.asList(lOfConfPart));

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
                    queries[2] = "DELETE FROM RoomReservation WHERE MEETINGNUMBER =" + fmtStrDB(msgArgs[1]);
                    queries[3] = "DELETE FROM Bookings WHERE MEETINGNUMBER = " + fmtStrDB(msgArgs[1]);


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
            }

            executeMultipleQ(conn, queries);

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
        logMessages(msg);

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
        logMessages(msg);

        for (int i = 0; i < lOP.length; i++) {
            if(!lOP[i].equals("")) {
                InetAddress ipLOP = InetAddress.getByName(lOP[i].trim());
                byte[] buf_s = msg.getBytes();
                DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, getPortByClientName(lOP[i].trim()));
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
     * logs messages into a text file
     * @param s
     * @throws IOException
     */
    public static void logMessages(String s) throws IOException {
        UIDisplay.textArea.append("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-\n"
                + s + "\n");

        PrintWriter writer = new PrintWriter(new FileWriter("my_log.txt", true));
        writer.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
        writer.println(s);
        writer.close();
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
