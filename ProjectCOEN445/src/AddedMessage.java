public class AddedMessage {

    final static public int AD_CODE = Message.ADDED_CODE;
    public String MT_NUMBER;
    public String IP_ADDRESS;

    public AddedMessage(String MT_NUMBER, String IP_ADDRESS) {
        this.MT_NUMBER = MT_NUMBER;
        this.IP_ADDRESS = IP_ADDRESS;
    }

    public static int getAD_CODE() {
        return AD_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getIP_ADDRESS() {
        return IP_ADDRESS;
    }

    public void setIP_ADDRESS(String IP_ADDRESS) {
        this.IP_ADDRESS = IP_ADDRESS;
    }

    public String printAddedMessage() {
        String output = "{" + getAD_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getIP_ADDRESS() + "}";
        return output;
    }
}
