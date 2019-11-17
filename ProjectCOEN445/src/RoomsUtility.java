import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

public class RoomsUtility {
    final static public String ROOM_TWO = "EV02.301";
    final static public String ROOM_ONE = "EV05.251";


    public static boolean reserveRoom(String date, String hr, String meeting_num) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

        //Parameter definition
        Date req_date = new java.sql.Date(format.parse(date).getTime());
        Time req_time = Time.valueOf(LocalTime.parse(String.valueOf(hr)));

        String q1 = "SELECT ROOMNUMBER"
                + " FROM RoomReservation"
                + " WHERE DATEINSERTED = " + Utility.fmtStrDB(req_date.toString())
                + " AND START_TIME = " + Utility.fmtStrDB(req_time.toString());

        try (Connection conn = Utility.connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             ResultSet res = pstmt.executeQuery()) {
            String q2 = null;

            if (!res.next()) {
                q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                        + " VALUES (" + Utility.fmtStrDB(ROOM_ONE) + ", " + Utility.fmtStrDB(req_date.toString())
                        + ", " + Utility.fmtStrDB(req_time.toString()) + ", " + Utility.fmtStrDB(meeting_num) + ")";
            } else {
                String result = "";
                result += res.getString(1) + ",";

                while (res.next()) {
                    result += res.getString(1) + ",";
                }

                if (!result.contains(ROOM_ONE)) {
                    q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                            + " VALUES (" + Utility.fmtStrDB(ROOM_ONE) + ", " + Utility.fmtStrDB(req_date.toString())
                            + ", " + Utility.fmtStrDB(req_time.toString()) + ", " + Utility.fmtStrDB(meeting_num) + ")";
                } else if (!result.contains(ROOM_TWO)) {
                    q2 = "INSERT INTO RoomReservation (ROOMNUMBER, dateinserted, start_time, meetingnumber)"
                            + " VALUES (" + Utility.fmtStrDB(ROOM_TWO) + ", " + Utility.fmtStrDB(req_date.toString())
                            + ", " + Utility.fmtStrDB(req_time.toString()) + ", " + Utility.fmtStrDB(meeting_num) + ")";
                } else {
                    return false;
                }
            }

            conn.prepareStatement(q2).execute();

//            PreparedStatement pstmt2 = conn.prepareStatement(q2);
//            pstmt2.execute();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // if reserving was unsuccessful
        return false;
    }
}
