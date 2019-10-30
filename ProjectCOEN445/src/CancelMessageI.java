public class CancelMessageI {

    final static public int CCI_CODE = Message.CANCEL_1_CODE;
    public String MT_NUMBER;
    public String CCI_MESSAGE;

    public CancelMessageI(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
        this.CCI_MESSAGE = Message.CANCEL_REASON_MSG;
    }

    public static int getCCI_CODE() {
        return CCI_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getCCI_MESSAGE() {
        return CCI_MESSAGE;
    }

    public String printCancelIMessage() {
        String output = "{" + this.getCCI_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getCCI_MESSAGE() + "}";
        return output;
    }

}
