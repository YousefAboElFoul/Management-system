import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpClient extends Utility {

    private static DatagramSocket ds;

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

                    Scanner sc = new Scanner(System.in);
                    System.out.println(" ");
                    System.out.println("Please Input your inputs");

                    try {
                        // convert the String input into the byte array
                        inp = getUserInput(sc.nextLine(), myIp);

                        // send the user's input
                        while (!inp.equals("Invalid Message")) {

                            // send a message to the server
                            sendUdpPacket(inp, Integer.parseInt(sPort), ds, ipS);

                            System.out.println(" ");
                            System.out.println("Please Input your inputs");
                            inp = getUserInput(sc.nextLine(), myIp);

                            // break the loop if user enters "bye"
                            if (inp.equals("bye"))
                                break;
                        }

                        if(inp.equals("Invalid Message")) {
                            System.out.println(inp);
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

        sendingTC.start();
        receivingTC.start();
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
}

