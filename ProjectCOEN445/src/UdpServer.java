import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.text.ParseException;
import java.util.*;


public class UdpServer extends Utility
{
    public static void main(String[] args) throws IOException, SQLException, Exception {

        establishDBConnection();
        // Configuration
        System.out.println("Starting UDP SERVER");

        // Setup
        Scanner myObj = new Scanner(System.in);

        System.out.println("Please enter the port on which you'd like to listen (server):");
        String Port = myObj.nextLine();

        // TODO - might be removed
//        System.out.println("Please enter your IP address (server):");
//        String IpAddress = myObj.nextLine();

        DatagramSocket ds = new DatagramSocket(Integer.parseInt(Port));
//        InetAddress ipS = InetAddress.getByName(IpAddress);
        InetAddress ipS = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());

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
                            //add a function for saving the client
                            if (ot.contains("Hi Server"))
                            {insertClientintoDB(ot);}
                            else {
                                // For debugging purposes
                                System.out.println(parsingMessage(ot, from).toString());
                                System.out.println(ot);
                                // Storing in the pending queue
                                pendingMessagesToBeTreated.add(ot + "#" + from);
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
         * Message Thread
         */
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
                            System.out.println("Please Enter Your Inputs:");
                            if (inp != null) {
                                if (!inp.equals("Invalid Message")) {

                                    if (inp.contains("2222") || inp.contains("6660"))
                                    {
                                        String []msgR = inp.split(" & ");
                                        String msgToSend = msgR[0];
                                        String []lOP = msgR[1].replace("[", "").replace("]", "").split(",");
                                        buf_s = msgToSend.getBytes();
                                        for (int i= 0 ; i<lOP.length ; i++)
                                        {
                                            sendUdpPacket(buf_s, Port, ds, lOP[i]);
                                        }
                                        if(inp.contains("2222"))
                                        {
                                            TIMER(msgToSend, lOP ,ds);
                                        }
                                    }

                                    buf_s = inp.getBytes();
                                    InetAddress ipC = InetAddress.getByName(currMsg[1].replace("/", "").split(":")[0]);
                                    int ipC_port = Integer.parseInt(currMsg[1].split(":")[1]);
                                    DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipC, ipC_port);
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

        receivingTS.start();
        sendingTS.start();
        receivingTS.join();
        sendingTS.join();
        //TODO to close the connection
    }

    /**
     *This to insert the client into the DB once he initializes communication
     * @param ot
     */
    private static void insertClientintoDB(String ot) throws SQLException {
        String [] parts =ot.split("/");
        for (String s:parts)
        {System.out.println(s+"");
        System.out.println("    ");
        }
        String IPAddress  = parts[1].split(":")[0];
        String ListeningPort = parts[1].split(":")[1];;
        String Hostname = parts[2].split("is ")[1];
        //for debugging
        System.out.println("Hostname:"+Hostname+"ListeningPort:"+ListeningPort+"IPAddress: "+IPAddress);
        /*String query="Insert into Registration(CLIENTNAME,IPADDRESS, LISTENINGPORT) VALUES("+Hostname+","+IPAddress+","+ListeningPort+")";
        Connection conn = Utility.connect();
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet res = pstmt.executeQuery();*/
    }

    /**
     * This is to send  a packet to the list of participants
     * @param buf_s
     * @param port
     * @param ds
     * @param s
     * @throws IOException
     */
    private static void sendUdpPacket(byte[] buf_s, String port, DatagramSocket ds, String s) throws IOException {
        InetAddress ipLOP = InetAddress.getByName(s.trim());
        DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, Integer.valueOf(port));
        ds.send(DpSend);
    }

    /**
     * This for the when we want to send invite messages
     * @param msgToSend
     * @param lOP
     * @param ds
     */
    private static void followUpLogicForInviteMessage(String msgToSend, String[] lOP, DatagramSocket ds) {
        String []msgArgs = msgToSend.replaceAll(" ","").replaceAll(".+\\{","").split("\\||\\}");
        String q1 = "SELECT count(*)"
                + " FROM ParticipantsConfirmed"
                + " WHERE MEETINGNUMBER = " + Utility.fmtStrDB(msgArgs[1])
                + " AND CONFIRMED = TRUE";
        String q2 ="SELECT REQUESTNUMBER ,ROOMNUMBER,REQUSTER from INVITEMESSAGE  WHERE MEETINGNUMBER = " + Utility.fmtStrDB(msgArgs[1])
                +" INNER JOIN ROOMRESERVATION ON INVITMESSAGE.MEETINGNUMBER = ROOMRESERVATION.MEETINGNUMBER";
        ArrayList<String> ListofParticipants =
                new ArrayList<String>(Arrays.asList(lOP));
        try (Connection conn = Utility.connect();
             PreparedStatement pstmt = conn.prepareStatement(q1);
             PreparedStatement pstmt2 = conn.prepareStatement(q2);
             ResultSet res = pstmt.executeQuery();
             ResultSet res2 = pstmt2.executeQuery();)
        {
            String[] queries = null;

            if (res.next()) {
                int result = Integer.valueOf(res.getString(1));
                if (result >= lOP.length) {
                    //Queries
                    queries[0] = "INSERT INTO ScheduledMessage(REQUESTNUMBER, MEETINGNUMBER, ROOMNUMBER, LISTOFCONFIRMEDPARTICIPANTS)"
                            + " VALUES ("+res2.getString(1)+","+msgArgs[1]+", "+res2.getString(2)+","+lOP+")";
                    queries[1] = "INSERT INTO ConfirmMessage(MEETINGNUMBER, ROOMNUMBER)"
                            + " VALUES ("+msgArgs[1]+", "+res2.getString(2)+")";
                    //logic for the forming and sending the confirmation to list of participants and scheduled to requester
                    if (res2.next())
                    {
                        //String RQ_NUMBER, String MT_NUMBER, String ROOM_NUMBER, ArrayList<String> LIST_OF_CONFIRMED_PARTICIPANTS
                        ScheduledMessage ScheduledMsg = new ScheduledMessage(res2.getString(1),msgArgs[1],res2.getString(2),ListofParticipants);
                        //String MT_NUMBER, String ROOM_NUMBER
                        ConfirmMessage ConfirmMsg  = new ConfirmMessage(msgArgs[1],res2.getString(2));
                        // send a message to the list of participants here
                        String []currMsg = ScheduledMsg.toString().split("#");
                        sendaConfirmorCancelMessagetoLOP(lOP, ds, currMsg);
                        //send a message to the requster/ send a message to the list of participants here
                        String []currMsg2 = ConfirmMsg.toString().split("#");
                        String inp = Utility.processingServer(currMsg[0], res2.getString(3), currMsg2[1]);
                        byte []buf_s = inp.getBytes();
                        sendUdpPacket(buf_s, res.getString(4), ds, res2.getString(3));
                    }
                }
            }
            else {
                queries[0] = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, LISTOFCONFIRMEDPARTICIPANTS, TOPIC)"
                        + " VALUES (?, ?, ?, ?, ?, ?)";
                queries[1] = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)"
                        + " VALUES (?, ?)";
            }
            Statement sts = conn.createStatement();
            for (String s : queries) {
                sts.addBatch(s);
            }
            sts.executeBatch();

            //TODO - make sure these messages are sent to the requester[0] or participants[1] accordingly


        } catch(SQLException | IOException | ParseException ex){
            ex.printStackTrace();
        }
    }

    /**
     * ConfirmorCancelMessage to the list of participants
     * @param lOP
     * @param ds
     * @param currMsg
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    private static void sendaConfirmorCancelMessagetoLOP(String[] lOP, DatagramSocket ds, String[] currMsg) throws IOException, ParseException, SQLException {
        for (int i = 0 ; i<lOP.length;i++)
        {   String inp = Utility.processingServer(currMsg[0], lOP[i].toString(), currMsg[1]);
            InetAddress ipLOP = InetAddress.getByName(lOP[i].trim());
            byte []buf_s = inp.getBytes();
            DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, getPortFromDB(lOP[i]));
            ds.send(DpSend);
        }
    }

    /**
     * return the port of the IP Address
     * @param s
     * @return
     * @throws SQLException
     */
    private static Integer getPortFromDB(String s) throws SQLException {
        String query ="SELECT LISTENINGPORT from REGISTERATION  WHERE CLIENTNAME= " + s;
        try {
            Connection conn = Utility.connect();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet res = pstmt.executeQuery();
            return Integer.valueOf(res.getString(1));
        }
        catch(SQLException e)
        { System.out.println(e);
        }

        return null;
    }

    /**
     * This for the timer whenever we send an invite message to the list of participants
      * @param msgToSend
     * @param lOP
     * @param ds
     */
    public static void TIMER(String msgToSend, String[] lOP, DatagramSocket ds)
    {
        Thread timer = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Thread.sleep(20000);
                    followUpLogicForInviteMessage(msgToSend, lOP , ds);
                }
                catch(InterruptedException e)
                {
                    System.out.println(e);
                }
            }
        });
        timer.start();
    }

    /**
     * Datasbase Connection
     */
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
                System.out.println("Connected to database");
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
