import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.io.*;

public class UdpServer extends Utility
{

    public static void main(String[] args) throws IOException {

        System.out.println("Starting UDP SERVER");
        DatagramSocket ds = new DatagramSocket(44444);
        byte[] receive = new byte[1024];
        DatagramPacket DpReceive = null;

        // Queue that holds all the pending messages
        PriorityQueue <String> pendingMessagesToBeTreated = new PriorityQueue<>();
        Iterator itr = pendingMessagesToBeTreated.iterator();

        //Sending conf
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        byte buf_s[] = null;

        while (true) {
            DpReceive = new DatagramPacket(receive, 1024);
            ds.receive(DpReceive);
            String str = new String(DpReceive.getData(), 0, DpReceive.getLength());
            String st = DpReceive.getSocketAddress().toString();
            String ot = "Client-" + st +":-" + str;

            System.out.println(ot);

            if (str.contains("0000")) {
                System.out.println("Client sent bye.....EXITING");
                break;
            }

            // Storing in the pending queue
            pendingMessagesToBeTreated.add(ot);

            //TODO to close the connection
//            if (str.contains("bye")) {
//                System.out.println("Client sent bye.....EXITING");
//                break;
//            }

            // Clear the buffer after every message.
            receive = new byte[1024];


            //TODO to send responses
//            Scanner sc = new Scanner(System.in);
//            System.out.println("Please Input your inputs");
//            String inp = sc.next().toString();
//            buf_s = inp.getBytes();
//            reserveroom = Reserveroom();
//            System.out.println(reserveroom);
//            DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ip, 44445);
//            ds.send(DpSend);

        }


        //TODO to process different messages
//        Utility.processingPendingMessages(itr);

        ds.close();
    }
}
