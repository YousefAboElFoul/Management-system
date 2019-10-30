public class RoomChangeMessage {

    final static public int RC_CODE = Message.ROOM_CHANGE_CODE;
    public String MT_NUMBER;
    public String NEW_ROOM_NUMBER;

    public RoomChangeMessage(String MT_NUMBER, String NEW_ROOM_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
        this.NEW_ROOM_NUMBER = NEW_ROOM_NUMBER;
    }

    public static int getRC_CODE() {
        return RC_CODE;
    }

    public String getMT_NUMBER() {
        return MT_NUMBER;
    }

    public void setMT_NUMBER(String MT_NUMBER) {
        this.MT_NUMBER = MT_NUMBER;
    }

    public String getNEW_ROOM_NUMBER() {
        return NEW_ROOM_NUMBER;
    }

    public void setNEW_ROOM_NUMBER(String NEW_ROOM_NUMBER) {
        this.NEW_ROOM_NUMBER = NEW_ROOM_NUMBER;
    }

    public String printRoomChangeMessage() {
        String output = "{" + this.getRC_CODE() + " | " + this.getMT_NUMBER() + " | " + this.getNEW_ROOM_NUMBER() + "}";
        return output;
    }
}
