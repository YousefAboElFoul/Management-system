public class RoomsUtility extends Rooms{
    private static Rooms Current_room=  new Rooms();
    /*This for checking the state of each room
     */
    public void setRoomIndex(boolean[] array, int hr, boolean state){
        array[hr]=state;
    }
    public String Reserveroom(int i)
    {
        if (Current_room.ROOM_ONE_STATE[i]==true)
        {
            setRoomIndex(Current_room.ROOM_ONE_STATE,i,false);
            return Current_room.getRoomOne()+"was reserved";
        }
        else if (Current_room.ROOM_TWO_STATE[i]==true)
        {  setRoomIndex(Current_room.ROOM_TWO_STATE,i,false);
            return Current_room.getRoomTwo() +"was reserved"; }

        return Message.UNAVAILABLE_MSG;
    }
}
