import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Properties;


public class UdpServer extends Utility
{
    public static void main(String[] args) throws IOException, SQLException, Exception {

        establishDBConnection();

        // Configuration
        System.out.println("Starting UDP SERVER");
        DatagramSocket ds = new DatagramSocket(44446);
        InetAddress ipS = InetAddress.getByName("127.0.0.1");


        // Queue that holds all the pending messages
        PriorityQueue <String> pendingMessagesToBeTreated = new PriorityQueue<>();

        Thread receivingTS = new Thread( new Runnable() {
            @Override
            public void run() {

                while (true)
                {
                    // Receiving Configuration
                    byte bu_rec[] = new byte[1024];
                    DatagramPacket DpReceive = null;

                    try {
                        // received from the client
                        DpReceive = new DatagramPacket(bu_rec, 1024);
                        ds.receive(DpReceive);

                        if (DpReceive != null) {
                            String str = new String(DpReceive.getData(), 0, DpReceive.getLength());
                            String from = DpReceive.getSocketAddress().toString();
                            String ot = "Client-" + from + ":-" + str;

                            // For debugging purposes
                            System.out.println(parsingMessage(ot, from).toString());

                            System.out.println(ot);

                            // Storing in the pending queue
                            pendingMessagesToBeTreated.add(ot + "#" + from);
                        }

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread sendingTS = new Thread( new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Iterator itr = pendingMessagesToBeTreated.iterator();

                    // Sending Configuration
                    String inp = null;
                    byte buf_s[] = new byte[1024];

                    while (itr.hasNext()) {
                        try {
                            Object currObj = itr.next();

                            // get the message and who sent it
                            String []currMsg = currObj.toString().split("#");

                            // process the message
                            inp = Utility.processingServer(currMsg[0], ipS.toString(), currMsg[1]); // convert the String input into the byte array.

                            // send the user's input
                            System.out.println("Please Input your inputs");
                            if (inp != null) {
                                if (!inp.equals("Invalid Message")) {
                                    buf_s = inp.getBytes();

                                    DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipS, 44447);
                                    ds.send(DpSend);
                                    pendingMessagesToBeTreated.remove(currObj);
                                    if (inp.equals("bye"))
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e) {
                        System.out.println(e + "Interrupted");
                    }
                }
            }
        });

//        while (true) {
        receivingTS.start();
        sendingTS.start();
        receivingTS.join();
        sendingTS.join();
//            ds.close();
//        }


        //TODO to close the connection
//        if (str.contains("bye")) {
//            System.out.println("Client sent bye.....EXITING");
//            break;
//        }
    }

    private static void establishDBConnection() {
        System.out.println("Trying to Establish Database Connection.....");
        Connection conn3 = null;

        try {
            // Connect method
            String dbURL3 = Utility.url;
            Properties parameters = new Properties();
            parameters.put("user", Utility.user);
            parameters.put("password", Utility.password);

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
