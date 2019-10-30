public class RejectMessage {

    final static public int RJ_CODE = Message.REJECT_CODE;
    public String MT_NUMBER;

    public RejectMessage(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public static int getRJ_CODE() {
        return RJ_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String printRMessage() {
        String output = "{" + this.getRJ_CODE() + " | " + this.getMT_NUMBER() + "}";
        return output;
    }
}
