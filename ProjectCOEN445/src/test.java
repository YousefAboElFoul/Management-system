import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

public class test {
    public static void main(String[] args) throws IOException
    {
        String d = "25/03/2019 15:30";
        String t = "15:00:00";
        String il = "1110 ";

        InetAddress myIp = InetAddress.getLocalHost();
        System.out.println(myIp.toString());

        Date dr = new Date(d);
//        dr.setTime(t);
//        System.out.println(dr.getHours()+":"+dr.getMinutes());
//        System.out.println(dr.toString());
//        System.out.println(Integer.parseInt(il));
    }


}
