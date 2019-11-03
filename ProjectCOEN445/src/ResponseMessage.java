public class ResponseMessage {

    final static public int RP_CODE = Message.RESPONSE_CODE;
    public String RQ_NUMBER;
    public String RP_MESSAGE;

    public ResponseMessage(String RP_NUMBER) {
        this.RQ_NUMBER = RP_NUMBER;
        this.RP_MESSAGE = Message.UNAVAILABLE_MSG;
    }

    public static int getRP_CODE() {
        return RP_CODE;
    }

    public String getRQ_NUMBER() {
        return RQ_NUMBER;
    }

    public void setRQ_NUMBER(String RQ_NUMBER) {
        this.RQ_NUMBER = RQ_NUMBER;
    }

    public String getRP_MESSAGE() {
        return RP_MESSAGE;
    }

    public void setRP_MESSAGE(String RP_MESSAGE) {
        this.RP_MESSAGE = RP_MESSAGE;
    }

    public String printRespMessage() {
        String output = "{" + this.getRP_CODE() + " | " + this.getRQ_NUMBER() + " | " + this.getRP_MESSAGE() + "}";
        return output;
    }
}
