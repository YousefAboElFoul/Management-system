import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UdpClient extends Utility {

    private static DatagramSocket ds;
    private static String inputText;

    public static void main(String[] args) throws Exception {
        // Configuration
        System.out.println("Starting UDP CLIENT");

        // Setup
        Scanner myObj = new Scanner(System.in);

        System.out.println("Please enter the port on which the server is listening:");
        String sPort = myObj.nextLine();

        System.out.println("Please enter the IP address of the server:");
        String ipS = myObj.nextLine();

        ds = new DatagramSocket();

        String myIp = InetAddress.getLocalHost().getHostAddress();

        UIDisplay.initOutput("UDP Client");

        inputText = null;


        /**
         * Sending Thread
         */
        Thread sendingTC = new Thread( new Runnable() {
            @Override
            public void run() {

                // Initialize communication with the Server
                try {
                    sendHiMessageToTheServer(ds ,ipS, sPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (true) {
                    // Sending Configuration
                    String inp = null;

                    try {
                        // convert the String input into the byte array
                        if (inputText != null) {
                            inp = getUserInput(inputText, myIp);

                            // send the user's input
                            if (!inp.equals("Invalid Message")) {

                                //reset inputText
                                inputText = null;

                                // send a message to the server
                                sendUdpPacket(inp, Integer.parseInt(sPort), ds, ipS);

                                System.out.println(" ");
                                System.out.println("Please Input your inputs");

                            }

                            if (inp.equals("Invalid Message")) {
                                System.out.println(inp);
                            }
                        }

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        /**
         *  Receiving Thread
         */
        Thread receivingTC = new Thread( new Runnable() {
            @Override
            public void run() {

                while (true) {

                    // Receiving Configuration
                    byte[] bu_rec = new byte[1024];
                    DatagramPacket DpReceive = null;

                    try {
                        // received from the sever
                        DpReceive = new DatagramPacket(bu_rec, 1024);
                        ds.receive(DpReceive);
                        if (DpReceive != null) {
                            String strR = new String(DpReceive.getData(), 0, DpReceive.getLength());
                            String ot = "Server:-" + strR;
                            System.out.println(ot);
                            logMessages(ot);

                            if (ot.contains("2222"))
                                processingClient(ot, Integer.parseInt(sPort), ipS, InetAddress.getLocalHost().getHostName());
                        }

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        /**
         * Thread used to generate the local agenda after every 10s
         */
        Thread printLocalAgenda = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        String q1 = null;
                        try {
                            q1 = "SELECT MEETINGNUMBER, DATEINSERTED, START_TIME, ROOMNUMBER"
                                    + " FROM Bookings"
                                    + " WHERE CLIENTNAME = " + fmtStrDB(getClientNameFromDB(myIp));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (q1 != null) {

                            try (Connection conn = connect();
                                 PreparedStatement pstmt = conn.prepareStatement(q1);
                                 ResultSet res = pstmt.executeQuery()) {

                                PrintWriter writer = new PrintWriter(new FileWriter("my_agenda.txt", false));
                                writer.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");

                                ResultSetMetaData rsmd = res.getMetaData();

                                writer.format("|%25s|%22s|%18s|%18s|\n".toUpperCase(), rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3), rsmd.getColumnName(4));
                                writer.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");

                                while (res.next()) {
                                    writer.format("|%25s|%22s|%18s|%18s|\n", res.getString(1), res.getString(2), res.getString(3), res.getString(4));
                                    writer.println("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-");
                                }
                                writer.close();

                            } catch (SQLException | IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        Thread.sleep(10000);

                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        });

        sendingTC.start();
        receivingTC.start();
        printLocalAgenda.start();
        sendingTC.join();
        receivingTC.join();
    }

    /**
     * @param ds
     * @param ipS
     * @param port
     */
    private static void sendHiMessageToTheServer(DatagramSocket ds, String ipS, String port) throws IOException {
        String hiMessage = "/Hi Server, My hostname is " + InetAddress.getByName(InetAddress.getLocalHost().getHostName());

        // send a message to the server
        sendUdpPacket(hiMessage, Integer.parseInt(port), ds, ipS);
    }

    /**
     * gets the datagram socket of the client
     * @return
     */
    public static DatagramSocket getSocket() {
        return ds;
    }

    /**
     * get user input from the JFrame
     * @param inputUI
     */
    public static void getJFrameInput(String inputUI) {
        inputText = inputUI;
    }
}

