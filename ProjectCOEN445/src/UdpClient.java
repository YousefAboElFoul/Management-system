import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class UdpClient
{


    public static void main(String args[]) throws IOException
    {
        // Sending Configuration
        DatagramSocket ds = new DatagramSocket(44445);
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        byte buf[] = null;

        // Receiving Configuration
        byte bu_rec[] = new byte[1024];
        DatagramPacket DpReceive = null;


        // loop while user didn't enter "bye"
        while (true)
        {
            Scanner sc = new Scanner(System.in);
            System.out.println("Please Input your inputs");

            // send the user's input
            String inp = Utility.getUserInput(sc.nextLine(), ip.toString());

            // convert the String input into the byte array.
            buf = inp.getBytes();

            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 44444);
            ds.send(DpSend);

            //TODO to receive responses
//            DpReceive = new DatagramPacket(bu_rec, 1024);
//            ds.receive(DpReceive);
//            String str = new String(DpReceive.getData(), 0, DpReceive.getLength());
//            System.out.println("Server:-" + str);

            // break the loop if user enters "bye"
            if (inp.equals("bye"))
                break;

//            bu_rec = new byte[1024];
        }
         ds.close();
    }
}

