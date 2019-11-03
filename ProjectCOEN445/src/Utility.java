import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Utility {

    @SuppressWarnings("uncheked")
    public static <T> T parsingMesssage(String in) throws IOException {
        // parsing
        String[] txt = in.replaceAll(".+\\{","").split("\\||\\}");

        switch (Integer.valueOf(txt[0])) {
            case Message.REQUEST_CODE:
                ArrayList<String> req_list = getParticipantsStrings(txt[5]);
                return (T) new RequestMessage(txt[1], new Date(txt[2]), txt[3], Integer.valueOf(txt[4]), req_list, txt[6]);
            case Message.RESPONSE_CODE:
                return (T) new ResponseMessage(txt[1]);
            case Message.INVITE_CODE:
                return (T) new InviteMessage(txt[1], new Date(txt[2]), txt[3], txt[4], txt[5]);
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
                return (T) new NotScheduledMessage(txt[1], new Date(txt[2]), txt[3], Integer.valueOf(txt[4]), nscheq_list, txt[6]);
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


    public static String getUserInput(String userInput, String ip) throws IOException{

        String[] ui = userInput.split(" ");

        switch (Integer.parseInt(ui[0])) {
            case Message.REQUEST_CODE:
                ArrayList<String> req_list = getParticipantsStrings(ui[5]);
                return new RequestMessage(ip.toString() + "-" + ui[1], new Date(ui[2]), ui[3], Integer.valueOf(ui[4]), req_list, ui[6]).printReqMessage();
            case Message.RESPONSE_CODE:
                return new ResponseMessage(ip.toString() + "-" + ui[1]).printRespMessage();
            case Message.INVITE_CODE:
                return new InviteMessage(ip.toString() + "-" + ui[1], new Date(ui[2]), ui[3], ui[4], ui[5]).printInvMessage();
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
                return new NotScheduledMessage(ip.toString() + "-" + ui[1], new Date(ui[2]), ui[3], Integer.valueOf(ui[4]), nscheq_list, ui[6]).printNotSchedMessage();
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
            default:
                return null;

        }
    }


    private static ArrayList<String> getParticipantsStrings(String s) {
        ArrayList<String> mylist = new ArrayList<String>();
        String[] lop = s.split(",");
        for (int i = 0; i < lop.length; i++) {
            mylist.add(lop[i]);
        }
        return mylist;
    }
}
