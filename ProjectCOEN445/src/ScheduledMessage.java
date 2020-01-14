import java.util.ArrayList;

public class ScheduledMessage {

    final static public int SD_CODE = Message.SCHEDULED_CODE;
    public String RQ_NUMBER;
    public String MT_NUMBER;
    public String ROOM_NUMBER;
    public ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS;

    public ScheduledMessage(String RQ_NUMBER, String MT_NUMBER, String ROOM_NUMBER, ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS) {
        this.RQ_NUMBER = RQ_NUMBER;
        this.MT_NUMBER = MT_NUMBER;
        this.ROOM_NUMBER = ROOM_NUMBER;
        this.LIST_OF_CONFIRMED_PARTICIPANTS = LIST_OF_CONFIRMED_PARTICIPANTS;
    }

    public static int getSD_CODE() {
        return SD_CODE;
    }

    public String getRQ_NUMBER() {
        return RQ_NUMBER;
    }

    public void setRQ_NUMBER(String RQ_NUMBER) {
        this.RQ_NUMBER = RQ_NUMBER;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getROOM_NUMBER() {
        return ROOM_NUMBER;
    }

    public void setROOM_NUMBER(String ROOM_NUMBER) {
        this.ROOM_NUMBER = ROOM_NUMBER;
    }

    public ArrayList<String> getLIST_OF_CONFIRMED_PARTICIPANTS() {
        return LIST_OF_CONFIRMED_PARTICIPANTS;
    }

    public void setLIST_OF_CONFIRMED_PARTICIPANTS(ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS) {
        this.LIST_OF_CONFIRMED_PARTICIPANTS = LIST_OF_CONFIRMED_PARTICIPANTS;
    }

    public String printSchedMessage() {
        String output = "{" + getSD_CODE() + " | " + this.getRQ_NUMBER() + " | " + this.getMT_NUMBER() + " | " + this.getROOM_NUMBER() + " | " + this.getLIST_OF_CONFIRMED_PARTICIPANTS() + "}";
        return output;
    }
}
