import java.util.Date;

public class InviteMessage {

    final static public int IV_CODE = Message.INVITE_CODE;
    public String MT_NUMBER;
    public Date IV_DATE;
    public String IV_TIME;
    public String IV_TOPIC;
    public String IV_REQUESTER;

    public InviteMessage(String MT_NUMBER, Date IV_DATE, String IV_TIME, String IV_TOPIC, String IV_REQUESTER) {
        this.MT_NUMBER = MT_NUMBER;
        this.IV_DATE = IV_DATE;
        this.IV_TIME = IV_TIME;
        this.IV_TOPIC = IV_TOPIC;
        this.IV_REQUESTER = IV_REQUESTER;
    }

    public static int getIV_CODE() {
        return IV_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String IV_NUMBER) {
        this.MT_NUMBER = IV_NUMBER;
    }

    public Date getIV_DATE() {
        return IV_DATE;
    }

    public void setIV_DATE(Date IV_DATE) {
        this.IV_DATE = IV_DATE;
    }

    public String getIV_TIME() {
        return IV_TIME;
    }

    public void setIV_TIME(String IV_TIME) {
        this.IV_TIME = IV_TIME;
    }

    public String getIV_TOPIC() {
        return IV_TOPIC;
    }

    public void setIV_TOPIC(String IV_TOPIC) {
        this.IV_TOPIC = IV_TOPIC;
    }

    public String getIV_REQUESTER() {
        return IV_REQUESTER;
    }

    public void setIV_REQUESTER(String IV_REQUESTER) {
        this.IV_REQUESTER = IV_REQUESTER;
    }

    public String printInvMessage() {
        String output = "{" + this.getIV_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getIV_DATE() + " | " + this.getIV_TIME() + " | " + this.getIV_TOPIC() + " | " + this.getIV_REQUESTER() + "}";
        return output;
    }

}
