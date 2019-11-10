import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

public class RoomsUtility extends Rooms {
    final static public String ROOM_TWO = "EV02.301";
    final static public String ROOM_ONE = "EV05.251";


    public static boolean reserveRoom(String date, String hr, String meeting_num) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        Date req_date = new java.sql.Date(format.parse(date).getTime());
        Time req_time = Time.valueOf(LocalTime.parse(String.valueOf(hr)));

        String q1 = "SELECT ROOMNUMBER"
                + " FROM RoomReservation"
                + " WHERE DATEINSERTED = " + req_date
                + " AND START_TIME = " + Time.valueOf(LocalTime.parse(String.valueOf(hr)));

        try (Connection conn = Utility.connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             ResultSet res = pstmt.executeQuery()) {
            String q2 = null;

            if (res == null) {
                q2 = "INSERT INTO RoomReservation"
                        + " VALUES (" + ROOM_ONE + ", " + req_date
                        + ", " + req_time + ", " + meeting_num + ")";
            } else {
                String result = "";
                while (res.next())
                {
                    result.join(res.getString(1), " ");
                }
                if (result.contains(ROOM_ONE)) {
                    q2 = "INSERT INTO RoomReservation"
                            + " VALUES (" + ROOM_TWO + ", " + req_date
                            + ", " + req_time + ", " + meeting_num + ")";
                } else if (result.contains(ROOM_TWO)) {
                    q2 = "INSERT INTO RoomReservation"
                            + " VALUES (" + ROOM_ONE + ", " + req_date
                            + ", " + req_time + ", " + meeting_num + ")";

                }
            }

            conn.prepareStatement(q2).execute();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // if reserving was unsuccessful
        return false;
    }
}
