import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

public class RoomsUtility extends Utility{
    final static public String ROOM_TWO = "EV02.301";
    final static public String ROOM_ONE = "EV05.251";


    public static String[] reserveRoom(String date, String hr, String meeting_num) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

        //Parameter definition
        Date req_date = new java.sql.Date(format.parse(date).getTime());
        Time req_time = Time.valueOf(LocalTime.parse(String.valueOf(hr)));
        String choosenRoom = null;

        String q1 = "SELECT DISTINCT ROOMNUMBER"
                + " FROM RoomReservation"
                + " WHERE DATEINSERTED = " + fmtStrDB(req_date.toString())
                + " AND START_TIME = " + fmtStrDB(req_time.toString());

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             ResultSet res = pstmt.executeQuery()) {
            String q2 = null;

            if (!res.next()) {
                q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                        + " VALUES (" + fmtStrDB(ROOM_ONE) + ", " + fmtStrDB(req_date.toString())
                        + ", " + fmtStrDB(req_time.toString()) + ", " + fmtStrDB(meeting_num) + ")";

                choosenRoom = ROOM_ONE;
            } else {
                String result = "";
                result += res.getString(1) + ",";

                while (res.next()) {
                    result += res.getString(1) + ",";
                }

                if (!result.contains(ROOM_ONE)) {
                    q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                            + " VALUES (" + fmtStrDB(ROOM_ONE) + ", " + fmtStrDB(req_date.toString())
                            + ", " + fmtStrDB(req_time.toString()) + ", " + fmtStrDB(meeting_num) + ")";

                    choosenRoom = ROOM_ONE;
                } else if (!result.contains(ROOM_TWO)) {
                    q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                            + " VALUES (" + fmtStrDB(ROOM_TWO) + ", " + fmtStrDB(req_date.toString())
                            + ", " + fmtStrDB(req_time.toString()) + ", " + fmtStrDB(meeting_num) + ")";

                    choosenRoom = ROOM_TWO;
                } else {
                    return new String[] {"false", choosenRoom};
                }
            }

            executedDB(q2);

            return new String[] {"true", choosenRoom};

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // if reservation was unsuccessful
        return new String[] {"false", choosenRoom};
    }
}
