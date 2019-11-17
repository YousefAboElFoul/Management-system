import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpClient
{
    public static void main(String args[]) throws Exception
    {
        // Configuration
        System.out.println("Starting UDP CLIENT");
        Scanner myObj = new Scanner(System.in);
        System.out.println("PLease enter the port on which you'd like to server the client");
        String Port = myObj.nextLine();
        System.out.println("PLease enter the IP address of the client");
        String IpAddress = myObj.nextLine();
        DatagramSocket ds = new DatagramSocket(Integer.parseInt(Port));
        InetAddress ip = InetAddress.getByName(IpAddress);
        System.out.println("UDP Client Started");
        // DatagramSocket ds = new DatagramSocket(44447);
        //InetAddress ip = InetAddress.getByName("127.0.0.1");

        Thread sendingTC = new Thread( new Runnable() {
            @Override
            public void run() {

                while (true) {
                    // Sending Configuration
                    String inp = null;
                    byte buf[] = null;

                    Scanner sc = new Scanner(System.in);
                    System.out.println("Please Input your inputs");

                    try {
                        inp = Utility.getUserInput(sc.nextLine(), ip.toString()); // convert the String input into the byte array.
                        // send the user's input
                        while (!inp.equals("Invalid Message")) {
                            buf = inp.getBytes();
                            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 44446);
                            ds.send(DpSend);

                            System.out.println("Please Input your inputs");
                            inp = Utility.getUserInput(sc.nextLine(), ip.toString()); // convert the String input into the byte array.
                            // break the loop if user enters "bye"
                            if (inp.equals("bye"))
                                break;
                        }

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        Thread receivingTC = new Thread( new Runnable() {
            @Override
            public void run() {

                while (true) {

                    // Receiving Configuration
                    byte bu_rec[] = new byte[1024];
                    DatagramPacket DpReceive = null;

                    try {
                        System.out.println("See Input");
                        // received from the sever
                        DpReceive = new DatagramPacket(bu_rec, 1024);
                        ds.receive(DpReceive);
                        if (DpReceive != null) {
                            String strR = new String(DpReceive.getData(), 0, DpReceive.getLength());
                            System.out.println("Server:-" + strR);
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

//        ds.close();
    }
}

