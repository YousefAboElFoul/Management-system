public class AddMessage {

    final static public int A_CODE = Message.ADD_CODE;
    public String MT_NUMBER;

    public AddMessage(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public static int getA_CODE() {
        return A_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String printAddMessage() {
        String output = "{" + getA_CODE() + " | " + this.getMT_NUMBER() + "}";
        return output;
    }
}
