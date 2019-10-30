import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) throws IOException
    {
        String d = "March 25 2019 15:30";
        String t = "15:00:00";

        Date dr = new Date(d);
//        dr.setTime(t);
        System.out.println(dr.toString());
    }

}
