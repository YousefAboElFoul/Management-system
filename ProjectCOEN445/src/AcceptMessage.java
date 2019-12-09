public class AcceptMessage {

    final static public int AC_CODE = Message.ACCEPT_CODE;
    public String MT_NUMBER;

    public AcceptMessage(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public static int getAC_CODE() {
        return AC_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String printAMessage() {
        String output = "{" + getAC_CODE() + " | " + this.getMT_NUMBER() + "}";
        return output;
    }

}
