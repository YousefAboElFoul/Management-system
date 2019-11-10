import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
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
//                    String rq_num[] = txt[1].split("-");
                    messageReceived = (T) new RequestMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), req_list, txt[6]);

                    // the mysql insert statement
                    query = "INSERT INTO RequestMessage(REQUESTNUMBER, DATEINSERTED, CURRENTTIME, MINIMUM, LISTOFPARTICIPANTS, TOPIC)"
                            + " VALUES (?, ?, ?, ?, ?, ?)";
                    break;
                case Message.RESPONSE_CODE:
                    messageReceived = (T) new ResponseMessage(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO ResponseMessage(REQUESTNUMBER)"
                            + " VALUES (?)";
                    break;
                case Message.INVITE_CODE:
//                    String mt_num[] = txt[1].split("-");
                    messageReceived = (T) new InviteMessage(txt[1], txt[2], txt[3], txt[4], txt[5]);

                    // the mysql insert statement
                    query = "INSERT INTO InviteMessage(MEETINGNUMBER, DATEINSERTED, TIME, TOPIC, REQUESTER)"
                            + " VALUES (?, ?, ?, ?, ?)";
                    break;
                case Message.ACCEPT_CODE:
                    messageReceived = (T) new AcceptMessage(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO AcceptMessage(MEETINGNUMBER, WHOACCEPTED)"
                            + " VALUES (?, ?)";
                    break;
                case Message.REJECT_CODE:
                    messageReceived = (T) new RejectMessage(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO RejectMessage(MEETINGNUMBER, WHOREJECTED)"
                            + " VALUES (?, ?)";
                    break;
                case Message.CONFIRM_CODE:
                    messageReceived = (T) new ConfirmMessage(txt[1], txt[2]);

                    // the mysql insert statement
                    query = "INSERT INTO ConfirmMessage(MEETINGNUMBER, ROOMNUMBER)"
                            + " VALUES (?, ?)";
                    break;
                case Message.SCHEDULED_CODE:
                    ArrayList<String> conf_list = getParticipantsStrings(txt[4]);
                    messageReceived = (T) new ScheduledMessage(txt[1], txt[2], txt[3], conf_list);

                    // the mysql insert statement
                    query = "INSERT INTO ScheduledMessage(REQUESTNUMBER, MEETINGNUMBER, ROOMNUMBER, LISTOFCONFIRMEDPARTICIPANTS)"
                            + " VALUES (?, ?, ?, ?)";
                    break;
                case Message.CANCEL_1_CODE:
                    messageReceived = (T) new CancelMessageI(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)"
                            + " VALUES (?, ?)";
                    break;
                case Message.NOT_SCHEDULED_CODE:
                    ArrayList<String> nscheq_list = getParticipantsStrings(txt[5]);
                    messageReceived = (T) new NotScheduledMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), nscheq_list, txt[6]);

                    // the mysql insert statement
                    query = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, TIME, MINIMUM, LISTOFCONFIRMEDPARTICIPANTS, TOPIC)"
                            + " VALUES (?, ?, ?, ?, ?, ?)";
                    break;
                case Message.CANCEL_2_CODE:
                    messageReceived = (T) new CancelMessageII(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)"
                            + " VALUES (?, ?)";
                    break;
                case Message.WITHDRAW_1_CODE:
                    messageReceived = (T) new WithdrawMessageI(txt[1], txt[2]);
                    break;
                case Message.WITHDRAW_2_CODE:
                    messageReceived = (T) new WithdrawMessageII(txt[1]);

                    // the mysql insert statement
                    query = "INSERT INTO WithdrawMessage(MEETINGNUMBER, WHOWITHDRAWED)"
                            + " VALUES (?, ?)";
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
        }

        // For db-insertion
        if (query != null) {
            if (mCode == Message.ADDED_CODE)
                updateMessage(query, mCode, messageReceived, from);
            else if (mCode != Message.ADDED_CODE)
                insertMessage(query, mCode, messageReceived, from);
        }

        return (messageReceived != null) ? (T) messageReceived : (T) "Error occurred in the request message";

        //For debugging
//        for (String i:wordArray) {
//            if (i != "{")
//            System.out.println(i);
//        }
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
            //Do parse our sql values
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

            switch (mCode) {
                case Message.REQUEST_CODE:
                    preparedStmt.setString(1, ((RequestMessage) message).getRQ_NUMBER());
                    preparedStmt.setDate(2, new java.sql.Date(format.parse(((RequestMessage) message).getRQ_DATE()).getTime()));
                    preparedStmt.setTime(3, Time.valueOf(LocalTime.parse(((RequestMessage) message).getRQ_TIME())));
                    preparedStmt.setInt(4, ((RequestMessage) message).getMIN_NUMBER_OF_PARTICIPANTS());
                    preparedStmt.setString(5, ((RequestMessage) message).getLIST_OF_PARTICIPANTS().toString());
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
    private static String switchMessages (String ip, String[] ui ,int arg_1) {
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
    public static void processingPendingMessages (Iterator itr) throws IOException {
        while (itr.hasNext()) {
            Object obj = null;
            try {
                // TODO check
                obj = Utility.parsingMessage(itr.next().toString(),null);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (obj instanceof RequestMessage) {
                // For debugging purpose
                // System.out.println(((RequestMessage) obj).printReqMessage());
            } else if (obj instanceof ResponseMessage) {
                // For debugging purpose
                // System.out.println(((AcceptMessage) obj).printAMessage());
            } else if (obj instanceof InviteMessage) {
            } else if (obj instanceof AcceptMessage) {
            } else if (obj instanceof RejectMessage) {
            } else if (obj instanceof ConfirmMessage) {
            } else if (obj instanceof ScheduledMessage) {
            } else if (obj instanceof CancelMessageI) {
            } else if (obj instanceof NotScheduledMessage) {
            } else if (obj instanceof CancelMessageII) {
            } else if (obj instanceof WithdrawMessageI) {
            } else if (obj instanceof WithdrawMessageII) {
            } else if (obj instanceof AddMessage) {
            } else if (obj instanceof AddedMessage) {
            } else if (obj instanceof RoomChangeMessage) {
            } else {
            }
        }
    }

}
