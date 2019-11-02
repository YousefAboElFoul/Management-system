import java.lang.reflect.Array;
import java.util.Arrays;

public class Rooms {
    final static public String ROOM_TWO = "EV02.301";
    final static public String ROOM_ONE = "EV05.251";
    // Index of this booleans are defined sas follows:
    // index 0 = 00-01h
    // index 1 = 01-02h
    // ....
    // index 22 = 22-23h
    // index 23 = 23-00h
    public boolean[] ROOM_ONE_STATE = new boolean[24];
    public boolean[] ROOM_TWO_STATE = new boolean[24];

    public Rooms() {
        Arrays.fill(ROOM_ONE_STATE, true);
        Arrays.fill(ROOM_TWO_STATE, true);
    }


    public static String getRoomOne() {
        return ROOM_ONE;
    }

    public static String getRoomTwo() {
        return ROOM_TWO;
    }

    public boolean[] getROOM_ONE_STATE() {
        return ROOM_ONE_STATE;
    }

    public void setROOM_ONE_STATE(boolean[] ROOM_ONE_STATE) {
        this.ROOM_ONE_STATE = ROOM_ONE_STATE;
    }

    public boolean[] getROOM_TWO_STATE() {
        return ROOM_TWO_STATE;
    }

    public void setROOM_TWO_STATE(boolean[] ROOM_TWO_STATE) {
        this.ROOM_TWO_STATE = ROOM_TWO_STATE;
    }
}
