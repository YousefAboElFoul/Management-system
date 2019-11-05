import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;


public class UdpServer extends Utility
{

    public static void main(String[] args) throws IOException {

        establishDBConnection();

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

    private static void establishDBConnection() {
        System.out.println("Trying to Establish Database Connection.....");
        Connection conn3 = null;

        try {
            // Connect method
            String dbURL3 = "jdbc:postgresql://ec2-54-235-92-244.compute-1.amazonaws.com:5432/d70m64dg1qc8fu?sslmode=require";
            Properties parameters = new Properties();
            parameters.put("user", "gkcmczoxettaer");
            parameters.put("password", "8d4b50fd5a522fd0256536f4f6993a61fad200dde8d58372c1200b5e63cfe694");

            conn3 = DriverManager.getConnection(dbURL3, parameters);
            if (conn3 != null) {
                System.out.println("Connected to database #3");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn3 != null && !conn3.isClosed()) {
                    conn3.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
