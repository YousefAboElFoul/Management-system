public class Rooms {

    final static public String ROOM_ONE = "EV05.251";
    public boolean ROOM_ONE_STATE;
    final static public String ROOM_TWO = "EV02.301";
    public boolean ROOM_TWO_STATE;

    public Rooms() {
        this.ROOM_ONE_STATE = true;
        this.ROOM_TWO_STATE = true;
    }


    public static String getRoomOne() {
        return ROOM_ONE;
    }

    public static String getRoomTwo() {
        return ROOM_TWO;
    }

    public boolean isROOM_ONE_STATE() {
        return ROOM_ONE_STATE;
    }

    public boolean isROOM_TWO_STATE() {
        return ROOM_TWO_STATE;
    }

    public void setROOM_TWO_STATE(boolean ROOM_TWO_STATE) {
        this.ROOM_TWO_STATE = ROOM_TWO_STATE;
    }

    public void setROOM_ONE_STATE(boolean ROOM_ONE_STATE) {
        this.ROOM_ONE_STATE = ROOM_ONE_STATE;
    }
}
