import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpServer
{
    private static Rooms Current_room=  new Rooms();


    public static void main(String[] args) throws IOException {
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
        /*This for checking the state of each room
         */
        public static String Reserveroom()
        {
            if (Current_room.ROOM_ONE_STATE==true)
            {
                Current_room.setROOM_ONE_STATE(false);
                return Current_room.getRoomOne()+"was reserved";
            }
            else if (Current_room.ROOM_TWO_STATE==true)
            { Current_room.setROOM_TWO_STATE(false);
                return Current_room.getRoomTwo() +"was reserved"; }

            return"no Room available";
        }
}
