import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

public class Utility {
    private static final String url = "jdbc:postgresql://ec2-54-235-92-244.compute-1.amazonaws.com:5432/d70m64dg1qc8fu?sslmode=require";
    private static final String user = "gkcmczoxettaer";
    private static final String password = "8d4b50fd5a522fd0256536f4f6993a61fad200dde8d58372c1200b5e63cfe694";

    private final static Logger LOGGER = Logger.getLogger(Utility.class.getName());

    @SuppressWarnings("uncheked")
    public static <T> T parsingMessage(String in) throws IOException, SQLException {
        // parsing
        String[] txt = in.replaceAll(" ","").replaceAll(".+\\{","").split("\\||\\}");

        switch (Integer.valueOf(txt[0])) {
            case Message.REQUEST_CODE:
                ArrayList<String> req_list = getParticipantsStrings(txt[5]);
                insertRequestMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), req_list, txt[6]);
                RequestMessage requestm= new RequestMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), req_list, txt[6]);
                return  (requestm!=null) ?  (T) requestm:(T)"Error occurred in the request message";
            case Message.RESPONSE_CODE:
                return (T) new ResponseMessage(txt[1]);
            case Message.INVITE_CODE:
                return (T) new InviteMessage(txt[1], txt[2], txt[3], txt[4], txt[5]);
            case Message.ACCEPT_CODE:
                return (T) new AcceptMessage(txt[1]);
            case Message.REJECT_CODE:
                return (T) new RejectMessage(txt[1]);
            case Message.CONFIRM_CODE:
                return (T) new ConfirmMessage(txt[1], txt[2]);
            case Message.SCHEDULED_CODE:
                ArrayList<String> conf_list = getParticipantsStrings(txt[4]);
                return (T) new ScheduledMessage(txt[1], txt[2], txt[3], conf_list);
            case Message.CANCEL_1_CODE:
                return (T) new CancelMessageI(txt[1]);
            case Message.NOT_SCHEDULED_CODE:
                ArrayList<String> nscheq_list = getParticipantsStrings(txt[5]);
                return (T) new NotScheduledMessage(txt[1], txt[2], txt[3], Integer.valueOf(txt[4]), nscheq_list, txt[6]);
            case Message.CANCEL_2_CODE:
                return (T) new CancelMessageII(txt[1]);
            case Message.WITHDRAW_1_CODE:
                return (T) new WithdrawMessageI(txt[1], txt[2]);
            case Message.WITHDRAW_2_CODE:
                return (T) new WithdrawMessageII(txt[1]);
            case Message.ADD_CODE:
                return (T) new AddMessage(txt[1]);
            case Message.ADDED_CODE:
                return (T) new AddedMessage(txt[1], txt[2]);
            case Message.ROOM_CHANGE_CODE:
                return (T) new RoomChangeMessage(txt[1], txt[2]);
            default:
                return null;
        }

        //For debugging
//        for (String i:wordArray) {
//            if (i != "{")
//            System.out.println(i);
//        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }


    private static void insertRequestMessage(String s, String s1, String s2, Integer valueOf, ArrayList<String> req_list, String s3 )  {
        //Establish Database Conneciton

        // the mysql insert statement
        String query = " insert into RequestMessage (IPADDRESS, REQUESTNUMBER, DATEINSERTED, CURRENTTIME, MINIMUM, LISTOFPARTICIPANTS, TOPIC)"
                + " values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query,
                     Statement.RETURN_GENERATED_KEYS)) {
        //Do parse our sql values
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString (1, s);
        preparedStmt.setInt(2,RequestMessage.RQ_CODE);
    //    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
      //  Date date = formatter.parse(s1);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            java.util.Date parsed =  format.parse(s1);
            java.sql.Date sql = new java.sql.Date(parsed.getTime());
        preparedStmt.setDate (3, sql);
            //DateFormat df = new SimpleDateFormat("HH:mm");
           // java.sql.Time timeValue = new java.sql.Time(df.parse(s2).getTime());
            //java.sql.Time sql2 = new java.sql.Time(timeValue.getTime());
        preparedStmt.setTime (4, Time.valueOf(LocalTime.parse(s2)));
        preparedStmt.setInt (5, valueOf);
        preparedStmt.setString (6, req_list.toString());
        preparedStmt.setString(7,s3);

        // execute the preparedstatement
        preparedStmt.execute();}
        catch (SQLException | ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }


    private static ArrayList<String> getParticipantsStrings (String s){
        ArrayList<String> mylist = new ArrayList<String>();
        String[] lop = s.split(",");
        for (int i = 0; i < lop.length; i++) {
            mylist.add(lop[i]);
        }
        return mylist;
    }
    /*
    This for getting the User  Input
     */
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
        /*Switch between messages
         */
        private static String switchMessages (String ip, String[] ui ,int arg_1)
        {
            switch (arg_1) {
                case Message.REQUEST_CODE:
                    ArrayList<String> req_list = getParticipantsStrings(ui[5]);
                    return new RequestMessage(ip.toString() + "-" + ui[1], ui[2], ui[3], Integer.valueOf(ui[4]), req_list, ui[6]).printReqMessage();
                case Message.RESPONSE_CODE:
                    return new ResponseMessage(ip.toString() + "-" + ui[1]).printRespMessage();
                case Message.INVITE_CODE:
                    return new InviteMessage(ip.toString() + "-" + ui[1], ui[2], ui[3], ui[4], ui[5]).printInvMessage();
                case Message.ACCEPT_CODE:
                    return new AcceptMessage(ip.toString() + "-" + ui[1]).printAMessage();
                case Message.REJECT_CODE:
                    return new RejectMessage(ip.toString() + "-" + ui[1]).printRMessage();
                case Message.CONFIRM_CODE:
                    return new ConfirmMessage(ip.toString() + "-" + ui[1], ui[2]).printConfMessage();
                case Message.SCHEDULED_CODE:
                    ArrayList<String> conf_list = getParticipantsStrings(ui[4]);
                    return new ScheduledMessage(ip.toString() + "-" + ui[1], ui[2], ui[3], conf_list).printSchedMessage();
                case Message.CANCEL_1_CODE:
                    return new CancelMessageI(ip.toString() + "-" + ui[1]).printCancelIMessage();
                case Message.NOT_SCHEDULED_CODE:
                    ArrayList<String> nscheq_list = getParticipantsStrings(ui[5]);
                    return new NotScheduledMessage(ip.toString() + "-" + ui[1], ui[2], ui[3], Integer.valueOf(ui[4]), nscheq_list, ui[6]).printNotSchedMessage();
                case Message.CANCEL_2_CODE:
                    return new CancelMessageII(ip.toString() + "-" + ui[1]).printCancelIIMessage();
                case Message.WITHDRAW_1_CODE:
                    return new WithdrawMessageI(ip.toString() + "-" + ui[1], ui[2]).printWithdrawIMessage();
                case Message.WITHDRAW_2_CODE:
                    return new WithdrawMessageII(ip.toString() + "-" + ui[1]).printWithdrawIIMessage();
                case Message.ADD_CODE:
                    return new AddMessage(ip.toString() + "-" + ui[1]).printAddMessage();
                case Message.ADDED_CODE:
                    return new AddedMessage(ip.toString() + "-" + ui[1], ui[2]).printAddedMessage();
                case Message.ROOM_CHANGE_CODE:
                    return new RoomChangeMessage(ip.toString() + "-" + ui[1], ui[2]).printRoomChangeMessage();
                case Message.STOP_CONNECTION:
                    return new String("0000");
                default:
                    LOGGER.warning("You have entered a wrong code try again please");
                    return "Invalid Message";
            }
        }

        public static void processingPendingMessages (Iterator itr) throws IOException {
            while (itr.hasNext()) {
                Object obj = null;
                try {
                    obj = Utility.parsingMessage(itr.next().toString());
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

        //TO DO error handling for the remaining messages


    private static String RequestMsg (String ip, String[]ui){
        String requestm = null;
        try {
            ArrayList<String> req_list = getParticipantsStrings(ui[5]);
            requestm = new RequestMessage(ip.toString() + "-" + ui[1], ui[2], ui[3], Integer.valueOf(ui[4]), req_list, ui[6]).printReqMessage();
        } catch (Exception e) {
            e.getMessage();
        }
        return (requestm != null) ? requestm : "Error occurred in the request message";
    }

}
