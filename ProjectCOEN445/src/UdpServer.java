import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;

public class UdpServer extends Utility {

	private static DatagramSocket ds;

	public static void main(String[] args) throws IOException, SQLException, Exception {

		// establish connection with database
		establishDBConnection();

		// Configuration
		System.out.println("Starting UDP SERVER");

		// Setup
		Scanner myObj = new Scanner(System.in);

		System.out.println("Please enter the port on which you'd like to listen (server):");
		String Port = myObj.nextLine();

		ds = new DatagramSocket(Integer.parseInt(Port));

		// IP Address of the server
		InetAddress ipS = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());

		// Queue that holds all the pending messages
		PriorityQueue<String> pendingMessagesToBeTreated = new PriorityQueue<>();

		/**
		 *  Receiving Thread
		 */
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

							System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");

							// add a function for saving the client
							if (ot.contains("Hi Server")) {
								insertClientIntoDB(ot);
							} else {
								// For debugging purposes
								System.out.println(parsingMessage(ot, from).toString());
								System.out.println(ot);
								// Storing in the pending queue
								pendingMessagesToBeTreated.add(ot + "#" + from2);
							}
							System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
						}

						Thread.sleep(1000);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		/**
		 * Sending Thread
		 */
		Thread sendingTS = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Iterator itr = pendingMessagesToBeTreated.iterator();

					// Sending Configuration
					String inp = null;

					while (itr.hasNext()) {
						try {
							Object currObj = itr.next();

							// get the message and who sent it
							String[] currMsg = currObj.toString().split("#");

							// process the message
							inp = processingServer(currMsg[0], ipS.toString(), getClientNameFromDB(currMsg[1])); // convert the

							// remove the current message from the queue after being processed
							pendingMessagesToBeTreated.remove(currObj);

							System.out.println("Please Enter Your Inputs:");
							if (inp != null) {
								if (!inp.equals("Invalid Message")) {
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
	 * inserts the client into the DB once he initializes communication
	 *
	 * @param ot
	 */
	private static void insertClientIntoDB(String ot) throws SQLException {
		System.out.println("Client joining...");

		String[] parts = ot.split("/");

		System.out.println(parts[2]);

		String IPAddress = parts[1].split(":")[0];
		String ListeningPort = parts[1].split(":")[1];
		String Hostname = parts[2].split("is ")[1];

		System.out.println("\nListening port\t: " + ListeningPort + "\nIP Address\t: " + IPAddress);

		String query = "INSERT INTO Registration(CLIENTNAME,IPADDRESS, LISTENINGPORT) VALUES("
				+ fmtStrDB(Hostname) + "," + fmtStrDB(IPAddress) + "," + ListeningPort + ")"
				+ "ON CONFLICT(IPADDRESS) DO UPDATE SET "
				+ "CLIENTNAME = " + fmtStrDB(Hostname) + "," + "LISTENINGPORT= " + ListeningPort + ";";

		executedDB(query);
	}

	public static DatagramSocket getSocket(){
		return ds;
	}

}
