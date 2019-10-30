import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
public class UdpServer
{
    public static void main(String[] args) throws IOException
    {  DatagramSocket ds = new DatagramSocket(44444);
        byte[] receive = new byte[1024];
        DatagramPacket DpReceive = null;
        while (true) {
            DpReceive = new DatagramPacket(receive, 1024);
            ds.receive(DpReceive);
            String str = new String(DpReceive.getData(), 0, DpReceive.getLength());
            System.out.println("Client:-" + str);



            if (str.equals("bye"))
            { System.out.println("Client sent bye.....EXITING");
                break;

            }

            receive = new byte[1024];
            // Clear the buffer after every message.


        }
    }
}
