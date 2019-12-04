import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

public class RequestMessage {

    final static private int RQ_CODE = Message.REQUEST_CODE;
    private String RQ_NUMBER = null ;
    private Date RQ_DATETIME;
    private String RQ_DATE;
    private String RQ_TIME;
    private int MIN_NUMBER_OF_PARTICIPANTS;
    private ArrayList <String>LIST_OF_PARTICIPANTS;
    private String RQ_TOPIC;

    // Since we have to auto increment the request number
    private static int curr_rq_num = 1;

    // The ip of the system running the udpClient
    String myIp = InetAddress.getLocalHost().getHostName();


    public RequestMessage(String in, String RQ_DATE, String RQ_TIME, int MIN_NUMBER_OF_PARTICIPANTS, ArrayList<String> LIST_OF_PARTICIPANTS, String RQ_TOPIC) throws UnknownHostException {
        if (in.contains("-")) {
            // use this notation on the server side
            this.RQ_NUMBER = in;
        }
        else {
            // use this notation on the client side and save the state
            this.RQ_NUMBER = in + "-" + Utility.messageCount(myIp, curr_rq_num);
        }
        this.RQ_DATE = RQ_DATE;
        this.RQ_TIME = RQ_TIME;
        setRQ_DATETIME(RQ_DATE, RQ_TIME);
        this.MIN_NUMBER_OF_PARTICIPANTS = MIN_NUMBER_OF_PARTICIPANTS;
        this.LIST_OF_PARTICIPANTS = LIST_OF_PARTICIPANTS;
        this.RQ_TOPIC = RQ_TOPIC;
    }

    public static int getRQ_CODE() {
        return RQ_CODE;
    }

    public Date getRQ_DATETIME() {
        return RQ_DATETIME;
    }

    public void setRQ_DATETIME(String RQ_DATE, String RQ_TIME) {
        this.RQ_DATETIME = new Date(RQ_DATE + " " + RQ_TIME);
    }

    public String getRQ_DATE() {
        return RQ_DATE;
    }

    public void setRQ_DATE(String DATE) {
        this.RQ_DATE = DATE;
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

    public String printReqMessage() {
        String output = "{" + this.getRQ_CODE() + " | " + this.getRQ_NUMBER() + " | " + this.getRQ_DATE() + " | " + this.getRQ_TIME() + " | " + this.getMIN_NUMBER_OF_PARTICIPANTS() + " | " + this.getLIST_OF_PARTICIPANTS() + " | " + this.getRQ_TOPIC() + "}";
        return output;
    }
}
