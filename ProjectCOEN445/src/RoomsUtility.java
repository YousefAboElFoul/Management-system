public class RoomsUtility extends Rooms{
    private static Rooms Current_room =  new Rooms();
    /*
        This for checking the state of each room
     */
    public void setRoomIndex(boolean[] timeslot, String[] occupants, int hr, boolean state, String mt_num) {
        timeslot[hr] = state;
        occupants[hr] = mt_num;
    }

    public boolean reserveRoom(int i, String meeting_num) {
        if (Current_room.ROOM_ONE_STATE[i] == true && Current_room.ROOM_ONE_OCCUPANT[i] == null) {
            setRoomIndex(Current_room.ROOM_ONE_STATE, Current_room.ROOM_ONE_OCCUPANT, i,false, meeting_num);
            return true;
        }
        else if (Current_room.ROOM_TWO_STATE[i] == true && Current_room.ROOM_TWO_OCCUPANT[i] == null) {
            setRoomIndex(Current_room.ROOM_TWO_STATE, Current_room.ROOM_TWO_OCCUPANT, i,false, meeting_num);
            return true;
        }

        // if reserving was unsuccessful
        return false;
    }
}
