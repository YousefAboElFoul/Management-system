public class CancelMessageII {

    final static public int CCII_CODE = Message.CANCEL_2_CODE;
    public String MT_NUMBER;

    public CancelMessageII(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public static int getCCII_CODE() {
        return CCII_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String printCancelIIMessage() {
        String output = "{" + getCCII_CODE() + " | " + this.getMT_NUMBER() + "}";
        return output;
    }
}
