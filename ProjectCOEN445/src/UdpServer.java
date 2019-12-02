import java.io.IOException;
import java.io.UTFDataFormatException;
import java.math.RoundingMode;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class UdpServer extends Utility {
	private static DatagramSocket ds;
	public static void main(String[] args) throws IOException, SQLException, Exception {

		establishDBConnection();
		// Configuration
		System.out.println("Starting UDP SERVER");

		// Setup
		Scanner myObj = new Scanner(System.in);

		System.out.println("Please enter the port on which you'd like to listen (server):");
		String Port = myObj.nextLine();

		// TODO - might be removed
		// System.out.println("Please enter your IP address (server):");
		// String IpAddress = myObj.nextLine();

		ds = new DatagramSocket(Integer.parseInt(Port));
		// InetAddress ipS = InetAddress.getByName(IpAddress);
		InetAddress ipS = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());

		// Queue that holds all the pending messages
		PriorityQueue<String> pendingMessagesToBeTreated = new PriorityQueue<>();


		Thread receivingTS = new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {
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
							String from2 = DpReceive.getAddress().toString();
							String ot = "Client-" + from + ":-" + str;
							// add a function for saving the client
							if (ot.contains("Hi Server")) {
								insertClientintoDB(ot);
							} else {
								// For debugging purposes
								System.out.println(parsingMessage(ot, from).toString());
								System.out.println(ot);
								// Storing in the pending queue
								pendingMessagesToBeTreated.add(ot + "#" + from2);
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
		Thread sendingTS = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Iterator itr = pendingMessagesToBeTreated.iterator();

					// Sending Configuration
					String inp = null;
					byte buf_s[] = new byte[1024];

					while (itr.hasNext()) {
						try {
							Object currObj = itr.next();
							// get the message and who sent it
							String[] currMsg = currObj.toString().split("#");
							// process the message
							inp = Utility.processingServer(currMsg[0], ipS.toString(), getClientNameFromDB(currMsg[1])); // convert the
							// String input into
							// the byte array.
							// send the user's input
							System.out.println("End of thread");
							pendingMessagesToBeTreated.remove(currObj);
							System.out.println("Please Enter Your Inputs:");
							if (inp != null) {
								if (!inp.equals("Invalid Message")) {

									if (inp.contains("2222") || inp.contains("6660")) {
										String[] msgR = inp.split(" & ");
										String msgToSend = msgR[0];
										String[] lOP = msgR[1].replace("[", "").replace("]", "").split(",");
//										buf_s = msgToSend.getBytes();
										for (int i = 0; i < lOP.length; i++) {
											sendUdpPacket(msgToSend, getPortFromDB(lOP[i]), ds, lOP[i]);
										}
										if (inp.contains("2222")) {
											TIMER(msgToSend, lOP, ds);
										}
									}
									else {
										buf_s = inp.getBytes();
										InetAddress ipC = InetAddress.getByName(currMsg[1].replace("/", "").split(":")[0]);
										DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipC, getPortFromDB(currMsg[1]));
										ds.send(DpSend);
									}

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
					} catch (InterruptedException e) {
						System.out.println(e + "Interrupted");
					}
				}
			}
		});

		receivingTS.start();
		sendingTS.start();
		receivingTS.join();
		sendingTS.join();
		// TODO to close the connection
	}

	/**
	 * This to insert the client into the DB once he initializes communication
	 *
	 * @param ot
	 */
	private static void insertClientintoDB(String ot) throws SQLException {
		String[] parts = ot.split("/");
		for (String s : parts) {
			System.out.println(s + "");
			System.out.println("    ");
		}
		String IPAddress = parts[1].split(":")[0];
		String ListeningPort = parts[1].split(":")[1];
		;
		String Hostname = parts[2].split("is ")[1];
		// for debugging
		System.out.println("Hostname:" + Hostname + "ListeningPort:" + ListeningPort + "IPAddress: " + IPAddress);
		String query = "Insert into Registration(CLIENTNAME,IPADDRESS, LISTENINGPORT) VALUES("
				+ Utility.fmtStrDB(Hostname) + "," + Utility.fmtStrDB(IPAddress) + "," + ListeningPort + ")"
				+"ON CONFLICT(IPADDRESS) DO UPDATE SET "+"clientname= "+Utility.fmtStrDB(Hostname)+","+"listeningport= "+ListeningPort+";";

		executedDB(query);
	}

	public static void executedDB(String query) throws SQLException {
		Connection conn = Utility.connect();
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.execute();
		conn.close();
	}

	/**
	 * This is to send a packet to the list of participants
	 *
	 * @param buf_s
	 * @param port
	 * @param ds
	 * @param s
	 * @throws IOException
	 */
	private static void sendUdpPacketStringPort(byte[] buf_s, String port, DatagramSocket ds, String s) throws IOException {
		InetAddress ipLOP = InetAddress.getByName(s.trim());
		DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, Integer.valueOf(port));
		ds.send(DpSend);
	}
	/**
	 * In case the port we fetch is integer instead of string which user in followuplogic for invite message
	 * @param msg
	 * @param port
	 * @param ds
	 * @param s
	 * @throws IOException
	 */
	private static void sendUdpPacket(String msg, Integer port, DatagramSocket ds, String s) throws IOException {
		byte[] buf_s = msg.getBytes();
		InetAddress ipLOP = InetAddress.getByName(s.trim());
		DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, port);
		ds.send(DpSend);
	}

	/**
	 * This for the when we want to send invite messages
	 *
	 * @param msgToSend
	 * @param lOP
	 * @param ds
	 */
	private static void followUpLogicForInviteMessage(String msgToSend, String[] lOP, DatagramSocket ds) {
		String[] msgArgs = msgToSend.replaceAll(" ", "").replaceAll(".+\\{", "").split("\\||\\}");
		String q1 = "SELECT WHO"
				+ " FROM ParticipantsConfirmed"
				+ " WHERE MEETINGNUMBER = " + Utility.fmtStrDB(msgArgs[1]) + " AND CONFIRMED = TRUE";
		String q2 = "SELECT REQUESTMESSAGE.REQUESTNUMBER , ROOMNUMBER , REQUESTER, MINIMUM  from INVITEMESSAGE"
				+ " INNER JOIN ROOMRESERVATION ON INVITEMESSAGE.MEETINGNUMBER = ROOMRESERVATION.MEETINGNUMBER"
				+ " INNER JOIN REQUESTMESSAGE ON INVITEMESSAGE.REQUESTNUMBER = REQUESTMESSAGE.REQUESTNUMBER"
				+ " WHERE INVITEMESSAGE.MEETINGNUMBER = " + Utility.fmtStrDB(msgArgs[1]);
		ArrayList<String> ListofParticipants = new ArrayList<String>(Arrays.asList(lOP));
		try 	(Connection conn = Utility.connect();
				PreparedStatement pstmt = conn.prepareStatement(q1);
				PreparedStatement pstmt2 = conn.prepareStatement(q2);
				ResultSet res = pstmt.executeQuery();
				ResultSet res2 = pstmt2.executeQuery();) {
			String[] queries = new String[2];

			String lOfConfPart = "";
			String Requestnumberquery = null;
			String Minimumparticipants = null;
			String Requester = null;
			String Roomnumber = null;

			if (res.next()) {
				int result = 0;

				lOfConfPart += res.getString(1);
				while (res.next())
				{
					lOfConfPart += "," + res.getString(1);
				}

				result = lOfConfPart.split(",").length;
				if (res2.next()) {


					Requestnumberquery = res2.getString(1);
					System.out.println(res2.getString(1) + "1");
					Roomnumber =res2.getString(2);
					System.out.println("Roomnumber is :" + Roomnumber);
					Requester =res2.getString(3);
					System.out.println("Requester is :" + Requester);
					Minimumparticipants = res2.getString(4);
					System.out.println(res2.getString(4) + "4");


					// logic for the forming and sending the confirmation to list of participants
					// and scheduled to requester
					System.out.println("Result is :" + result +"Minimum is:"+Minimumparticipants);
					if (result >= Integer.valueOf(Minimumparticipants)) {

						// Queries
						queries[0] = "INSERT INTO ScheduledMessage(REQUESTNUMBER, MEETINGNUMBER, ROOMNUMBER, LISTOFCONFIRMEDPARTICIPANTS)"
								+ " VALUES (" + Utility.fmtStrDB(Requestnumberquery) + "," + Utility.fmtStrDB(msgArgs[1]) + ", " + Utility.fmtStrDB(Roomnumber) + "," + Utility.fmtStrDB(lOfConfPart)
								+ ")";
						queries[1] = "INSERT INTO ConfirmMessage(MEETINGNUMBER, ROOMNUMBER)" + " VALUES (" + Utility.fmtStrDB(msgArgs[1])
								+ ", " + Utility.fmtStrDB(Roomnumber) + ")";

						// LIST_OF_CONFIRMED_PARTICIPANTS
						ArrayList<String> ListofConfParticipants = new ArrayList<String>(Arrays.asList(lOfConfPart));
						ScheduledMessage ScheduledMsg = new ScheduledMessage(Requestnumberquery, msgArgs[1], Roomnumber, ListofConfParticipants);
						// String MT_NUMBER, String ROOM_NUMBER
						ConfirmMessage ConfirmMsg = new ConfirmMessage(msgArgs[1], Roomnumber);

						// Scheduled Message
						String currMsg = ScheduledMsg.printSchedMessage();

						// Confirm message
						String currMsg2 = ConfirmMsg.printConfMessage();
						String[]arrayoflOfConfPart =lOfConfPart.split(",");

						//Send Message to list of Confirmed Participants
						sendaConfirmorCancelMessagetoLOP(arrayoflOfConfPart, ds, currMsg2);

						// send a message to the requester/ send a message to the list of participant
						sendUdpPacket(currMsg, getPortFromDBByClientName(Requester), ds, Requester);
					}
				}
			} else {
				while (res2.next()) {
					Requestnumberquery = res2.getString(1);
					System.out.println(res2.getString(1) + "1");
					Roomnumber =res2.getString(2);
					System.out.println("Roomnumber is :" + Roomnumber);
					Requester =res2.getString(3);
					System.out.println("Requester is :" + Requester);
					Minimumparticipants = res2.getString(4);
					System.out.println(res2.getString(4) + "4");
				}

				System.out.println("First string is: "+Requestnumberquery+" Second string is:"+Minimumparticipants);
				if (lOfConfPart == "")
				{queries[0] = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, TOPIC)"
						+ " VALUES (" + Utility.fmtStrDB(Requestnumberquery) + "," + Utility.fmtStrDB(msgArgs[2]) + "," + Utility.fmtStrDB(msgArgs[3]) + "," + Minimumparticipants
						+  "," + Utility.fmtStrDB(msgArgs[4]) + ")";
					queries[1] = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)" + " VALUES (" + Utility.fmtStrDB(msgArgs[1]) + "," + Utility.fmtStrDB(InetAddress.getLocalHost().getHostName()) + ")";}
				else {
					queries[0] = "INSERT INTO NotScheduledMessage(REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, LISTOFCONFIRMEDPARTICIPANTS, TOPIC)"
							+ " VALUES (" + Utility.fmtStrDB(Requestnumberquery) + "," + Utility.fmtStrDB(msgArgs[2]) + "," + Utility.fmtStrDB(msgArgs[3]) + "," + Minimumparticipants
							+ "," + Utility.fmtStrDB(lOfConfPart) + "," + Utility.fmtStrDB(msgArgs[4]) + ")";
					queries[1] = "INSERT INTO CancelMessage(MEETINGNUMBER, WHOCANCELED)" + " VALUES (" + Utility.fmtStrDB(msgArgs[1]) + "," + Utility.fmtStrDB(InetAddress.getLocalHost().getHostName()) + ")";
				}

				// LIST_OF_CONFIRMED_PARTICIPANTS
				ArrayList<String> ListofConfParticipants = new ArrayList<String>(Arrays.asList(lOfConfPart));
				NotScheduledMessage NotScheduledMsg = new NotScheduledMessage(Requestnumberquery, msgArgs[2], msgArgs[3],
						Integer.valueOf(Minimumparticipants), ListofConfParticipants, msgArgs[4]);
				// String MT_NUMBER, String ROOM_NUMBER
				CancelMessageI CancelMsg = new CancelMessageI(msgArgs[1]);

				// NotScheduled message
				String currMsg = NotScheduledMsg.printNotSchedMessage();

				// Cancel Message
				String currMsg2 = CancelMsg.printCancelIMessage();
				String[] arrayoflOfConfPart = lOfConfPart.split(",");

				//Send Message to list of Confirmed Participants
				sendaConfirmorCancelMessagetoLOP(arrayoflOfConfPart, ds, currMsg2);

				// send a message to the requester/ send a message to the list of participant
				sendUdpPacket(currMsg, getPortFromDBByClientName(Requester), ds,Requester);
			}
			Statement sts = conn.createStatement();
			for (String s : queries) {
				sts.addBatch(s);
			}
			sts.executeBatch();
			conn.close();

			// TODO - make sure these messages are sent to the requester[0] or
			// participants[1] accordingly

		} catch (SQLException | IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * ConfirmorCancelMessage to the list of participants
	 *
	 * @param lOP
	 * @param ds
	 * @param currMsg
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void sendaConfirmorCancelMessagetoLOP(String[] lOP, DatagramSocket ds, String currMsg)
			throws IOException, SQLException {
		for (int i = 0; i < lOP.length; i++) {
			if(!lOP[i].equals("")) {
				InetAddress ipLOP = InetAddress.getByName(lOP[i].trim());
				byte[] buf_s = currMsg.getBytes();
				DatagramPacket DpSend = new DatagramPacket(buf_s, buf_s.length, ipLOP, getPortFromDBByClientName(lOP[i]));
				ds.send(DpSend);
			}
		}
	}

	/**
	 * return the port of the IP Address
	 *
	 * @param s
	 * @return
	 * @throws SQLException
	 */
	private static Integer getPortFromDB(String s) throws SQLException {
		String query = "SELECT LISTENINGPORT from REGISTRATION  WHERE IPADDRESS= " + Utility.fmtStrDB(s);
		try {
			Connection conn = Utility.connect();
			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet res = pstmt.executeQuery();
			conn.close();
			while (res.next())
			{return Integer.valueOf(res.getString(1));}
		} catch (SQLException e) {
			System.out.println(e);
		}

		return null;
	}

	/**
	 *
	 * @param s
	 * @return
	 * @throws SQLException
	 */
	private static Integer getPortFromDBByClientName(String s) throws SQLException {
		String query = "SELECT LISTENINGPORT from REGISTRATION  WHERE CLIENTNAME= " + Utility.fmtStrDB(s);
		try {
			Connection conn = Utility.connect();
			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet res = pstmt.executeQuery();
			conn.close();
			while (res.next())
			{return Integer.valueOf(res.getString(1));}
		} catch (SQLException e) {
			System.out.println(e);
		}

		return null;
	}

	/**
	 * return the client name of the IP Address
	 *
	 * @param s
	 * @return
	 * @throws SQLException
	 */
	private static String getClientNameFromDB(String s) throws SQLException {
		String query = "SELECT CLIENTNAME from REGISTRATION  WHERE IPADDRESS= " + Utility.fmtStrDB(s.replace("/", ""));
		try {
			Connection conn = Utility.connect();
			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet res = pstmt.executeQuery();
			conn.close();
			while (res.next())
				return res.getString(1);
		} catch (SQLException e) {
			System.out.println(e);
		}

		return null;
	}

	/**
	 * This for the timer whenever we send an invite message to the list of
	 * participants
	 *
	 * @param msgToSend
	 * @param lOP
	 * @param ds
	 */
	public static void TIMER(String msgToSend, String[] lOP, DatagramSocket ds) {
		Thread timer = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(20000);
					followUpLogicForInviteMessage(msgToSend, lOP, ds);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
		});
		timer.start();
	}

	public static DatagramSocket getSocket(){
		return ds;
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
