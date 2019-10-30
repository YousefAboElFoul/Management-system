import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class UdpClient
{
    public static void main(String args[]) throws IOException
    {


        DatagramSocket ds = new DatagramSocket();
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        byte buf[] = null;

        // loop while user didn't enter "bye"
        while (true)
        {

            Scanner sc = new Scanner(System.in);
            System.out.println("Please Input your inputs");
            sc.useDelimiter(",");

            int rqnum = sc.nextInt();
            int numofp= sc.nextInt();
            String tp =sc.next();
            ArrayList<String> mylist = new ArrayList<String>();
            String[] lop = sc.next().split(" ");
            for (int i=0; i < lop.length; i++) {
                mylist.add(lop[i]);
            }
//            (String RQ_NUMBER, Date RQ_DATE, String RQ_TIME, int MIN_NUMBER_OF_PARTICIPANTS, ArrayList<String> LIST_OF_PARTICIPANTS, String RQ_TOPIC)
            RequestMessage Test = new RequestMessage(ip.toString()+"-"+Integer.valueOf(rqnum).toString(), new Date(), new Date(System.currentTimeMillis()).toString(), numofp, mylist, tp);
            String output = "{" + Test.getRequestCode() + " | " + Test.getRQ_NUMBER() + " | " + Test.getRQ_DATE() + " | " + Test.getRQ_TIME() + " | " + Test.getMIN_NUMBER_OF_PARTICIPANTS() + " | " + Test.getLIST_OF_PARTICIPANTS() + " | " + Test.getRQ_TOPIC() + "}";
            String inp = output;

            // convert the String input into the byte array.
            buf = inp.getBytes();

            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 44444);
            ds.send(DpSend);

            // break the loop if user enters "bye"
            if (inp.equals("bye"))
                break;
            // ds.close();
        }
    }
}

