import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) throws IOException
    {
        String d = "25/03/2019 15:30";
        String t = "15:00:00";

        Date dr = new Date(d);
//        dr.setTime(t);
        System.out.println(dr.toString());
    }

}
