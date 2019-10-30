public class ConfirmMessage {

    final static public int CF_CODE = Message.CONFIRM_CODE;
    public String MT_NUMBER;
    public String ROOM_NUMBER;

    public ConfirmMessage(String MT_NUMBER, String ROOM_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
        this.ROOM_NUMBER = ROOM_NUMBER;
    }

    public static int getCF_CODE() {
        return CF_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getROOM_NUMBER() {
        return ROOM_NUMBER;
    }

    public void setROOM_NUMBER(String ROOM_NUMBER) {
        this.ROOM_NUMBER = ROOM_NUMBER;
    }

    public String printConfMessage() {
        String output = "{" + this.getCF_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getROOM_NUMBER() + "}";
        return output;
    }

}
