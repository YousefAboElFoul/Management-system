import java.util.ArrayList;
import java.util.Date;

public class NotScheduledMessage {
    final static public int NSD_CODE = Message.NOT_SCHEDULED_CODE;
    public String RQ_NUMBER;
    public Date NSD_DATETIME;
    public String NSD_DATE;
    public String NSD_TIME;
    public int MIN_NUMBER_OF_PARTICIPANTS;
    public ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS;
    public String NSD_TOPIC;


    public NotScheduledMessage(String RQ_NUMBER, String NSD_DATE, String NSD_TIME, int MIN_NUMBER_OF_PARTICIPANTS, ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS, String NSD_TOPIC) {
        this.RQ_NUMBER = RQ_NUMBER;
        this.NSD_DATE = NSD_DATE;
        this.NSD_TIME = NSD_TIME;
        setNSD_DATETIME(NSD_DATE, NSD_TIME);
        this.MIN_NUMBER_OF_PARTICIPANTS = MIN_NUMBER_OF_PARTICIPANTS;
        this.LIST_OF_CONFIRMED_PARTICIPANTS = LIST_OF_CONFIRMED_PARTICIPANTS;
        this.NSD_TOPIC = NSD_TOPIC;
    }

    public static int getNSD_CODE() {
        return NSD_CODE;
    }

    public String getRQ_NUMBER() {
        return RQ_NUMBER;
    }

    public void setRQ_NUMBER(String RQ_NUMBER) {
        this.RQ_NUMBER = RQ_NUMBER;
    }

    public Date getNSD_DATETIME() {
        return NSD_DATETIME;
    }

    public void setNSD_DATETIME(String NSD_DATE, String NSD_TIME) {
        this.NSD_DATETIME = new Date(NSD_DATE + " " + NSD_TIME);
    }

    public String getNSD_DATE() {
        return NSD_DATE;
    }

    public void setNSD_DATE(String NSD_DATE) {
        this.NSD_DATE = NSD_DATE;
    }

    public String getNSD_TIME() {
        return NSD_TIME;
    }

    public void setNSD_TIME(String NSD_TIME) {
        this.NSD_TIME = NSD_TIME;
    }

    public int getMIN_NUMBER_OF_PARTICIPANTS() {
        return MIN_NUMBER_OF_PARTICIPANTS;
    }

    public void setMIN_NUMBER_OF_PARTICIPANTS(int MIN_NUMBER_OF_PARTICIPANTS) {
        this.MIN_NUMBER_OF_PARTICIPANTS = MIN_NUMBER_OF_PARTICIPANTS;
    }

    public ArrayList<String> getLIST_OF_CONFIRMED_PARTICIPANTS() {
        return LIST_OF_CONFIRMED_PARTICIPANTS;
    }

    public void setLIST_OF_CONFIRMED_PARTICIPANTS(ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS) {
        this.LIST_OF_CONFIRMED_PARTICIPANTS = LIST_OF_CONFIRMED_PARTICIPANTS;
    }

    public String getNSD_TOPIC() {
        return NSD_TOPIC;
    }

    public void setNSD_TOPIC(String NSD_TOPIC) {
        this.NSD_TOPIC = NSD_TOPIC;
    }

    public String printNotSchedMessage() {
        String output = "{" + getNSD_CODE() + " | " + this.getRQ_NUMBER() + " | " + this.getNSD_DATE() + " | " + this.getNSD_TIME() + " | " + this.getMIN_NUMBER_OF_PARTICIPANTS() + " | " + this.getLIST_OF_CONFIRMED_PARTICIPANTS() + " | " + this.getNSD_TOPIC() + "}";
        return output;
    }

}
