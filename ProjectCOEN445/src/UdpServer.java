import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UdpServer
{



    public static void main(String[] args) throws IOException {

        System.out.println("Starting UDP SERVER");
        DatagramSocket ds = new DatagramSocket(44444);
        byte[] receive = new byte[1024];
        DatagramPacket DpReceive = null;
        String reserveroom;

        //Sending conf
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        byte buf_s[] = null;

        while (true) {
            DpReceive = new DatagramPacket(receive, 1024);
            ds.receive(DpReceive);
            String str = new String(DpReceive.getData(), 0, DpReceive.getLength());
            String st = DpReceive.getSocketAddress().toString();
            System.out.println("Client:-" + str);
            System.out.println("Client1:-" + st);


            if (str.equals("bye")) {
                System.out.println("Client sent bye.....EXITING");
                break;
            }

            // Clear the buffer after every message.
            receive = new byte[1024];


            Scanner sc = new Scanner(System.in);
            System.out.println("Please Input your inputs");
            String inp = sc.next().toString();
            buf_s = inp.getBytes();
            reserveroom = Reserveroom();
            System.out.println(reserveroom);
            DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ip, 44445);
            ds.send(DpSend);

        }
        ds.close();
    }
}
