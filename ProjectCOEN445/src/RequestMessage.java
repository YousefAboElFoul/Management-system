import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;


public class RequestMessage {
    final static public int REQUEST_CODE = 1111;
    public String RQ_NUMBER = null ;
    public Date RQ_DATE;
    String  RQ_TIME;
    public int MIN_NUMBER_OF_PARTICIPANTS;
    public ArrayList <String>LIST_OF_PARTICIPANTS = new ArrayList<>();
    public String RQ_TOPIC;


    public RequestMessage(String RQ_NUMBER, Date RQ_DATE, String RQ_TIME, int MIN_NUMBER_OF_PARTICIPANTS, ArrayList<String> LIST_OF_PARTICIPANTS, String RQ_TOPIC) {
        this.RQ_NUMBER = RQ_NUMBER;
        this.RQ_DATE = RQ_DATE;
        this.RQ_TIME = RQ_TIME;
        this.MIN_NUMBER_OF_PARTICIPANTS = MIN_NUMBER_OF_PARTICIPANTS;
        this.LIST_OF_PARTICIPANTS = LIST_OF_PARTICIPANTS;
        this.RQ_TOPIC = RQ_TOPIC;
    }

    public static int getRequestCode() {
        return REQUEST_CODE;
    }

    public Date getRQ_DATE() {
        return RQ_DATE;
    }

    public void setRQ_DATE(Date RQ_DATE) {
        this.RQ_DATE = RQ_DATE;
    }

    public String getRQ_TIME() {
        return RQ_TIME;
    }

    public void setRQ_TIME(String RQ_TIME) {
        this.RQ_TIME = RQ_TIME;
    }

    public int getMIN_NUMBER_OF_PARTICIPANTS() {
        return MIN_NUMBER_OF_PARTICIPANTS;
    }

    public void setMIN_NUMBER_OF_PARTICIPANTS(int MIN_NUMBER_OF_PARTICIPANTS) {
        this.MIN_NUMBER_OF_PARTICIPANTS = MIN_NUMBER_OF_PARTICIPANTS;
    }

    public ArrayList<String> getLIST_OF_PARTICIPANTS() {
        return LIST_OF_PARTICIPANTS;
    }

    public void setLIST_OF_PARTICIPANTS(ArrayList<String> LIST_OF_PARTICIPANTS) {
        this.LIST_OF_PARTICIPANTS = LIST_OF_PARTICIPANTS;
    }

    public String getRQ_TOPIC() {
        return RQ_TOPIC;
    }

    public void setRQ_TOPIC(String RQ_TOPIC) {
        this.RQ_TOPIC = RQ_TOPIC;
    }

    public String getRQ_NUMBER() {
        return RQ_NUMBER;
    }

    public void setRQ_NUMBER(String RQ_NUMBER) {
        this.RQ_NUMBER = RQ_NUMBER;
    }
}
