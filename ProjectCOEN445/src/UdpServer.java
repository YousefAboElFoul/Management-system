import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

public class UdpServer extends Utility {

	private static DatagramSocket ds;

	public static void main(String[] args) throws Exception {

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
		String ipS = InetAddress.getLocalHost().getHostName();

		// Queue that holds all the pending messages
		PriorityBlockingQueue<String> pendingMessagesToBeTreated = new PriorityBlockingQueue<>();

		/**
		 *  Receiving Thread
		 */
		Thread receivingTS = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// Receiving Configuration
					byte[] bu_rec = new byte[1024];
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
								System.out.println(ot);
								logMessages(ot);

								System.out.println(parsingMessage(ot, from, 1).toString());
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

					// Sending Configuration
					String inp = null;

					for (Iterator itr = pendingMessagesToBeTreated.iterator(); itr.hasNext();) {

						try {
							Object currObj = itr.next();

							// get the message and who sent it
							String[] currMsg = currObj.toString().split("#");

							// process the message
							inp = processingServer(currMsg[0], ipS, getClientNameFromDB(currMsg[1])); // convert the

							// remove the current message from the queue after being processed
							pendingMessagesToBeTreated.remove(currObj);

							System.out.println(" ");
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

		/**
		 * Input Thread
		 */
		Thread inputTS = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					// Sending Configuration
					String input = null;

					Scanner sc = new Scanner(System.in);
					System.out.println("Please Input your inputs");

					try {
						String inp = sc.nextLine();
						boolean good = false;
						while (!good) {
							while (!inp.contains(String.valueOf(Message.ROOM_CHANGE_CODE))) {
								inp = sc.nextLine();
								System.out.println("\nInvalid input message\n\n");
							}

							input = getUserInput(inp, null);
							if (!input.equals("Invalid Message"))
								good = true;
							else {
								System.out.println("\nRoom number does not exist\n\n");
								System.out.println("Please Input your inputs");
								inp = sc.nextLine();
							}
						}

						System.out.println("\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
						// For debugging purposes
						System.out.println(input);
						input = input.replace("{","");
						System.out.println(parsingMessage(input, null, 1).toString());
						// Storing in the pending queue
						pendingMessagesToBeTreated.add(input + "#" + " ");

						System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");

						// break the loop if user enters "bye"
						if (inp.equals("bye"))
							break;
					} catch (Exception e) {
						e.printStackTrace();
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
		inputTS.start();
		receivingTS.join();
		sendingTS.join();
		inputTS.join();
	}

	/**
	 * inserts the client into the DB once he initializes communication
	 *
	 * @param ot
	 */
	private static void insertClientIntoDB(String ot) throws SQLException, IOException {
		System.out.println("Client joining...");

		String[] parts = ot.split("/");

		System.out.println(parts[2]);
		logMessages(parts[2]);

		String IPAddress = parts[3];
		String ListeningPort = parts[1].split(":")[1];
		String Hostname = parts[2].split("is ")[1];

		System.out.println("\nListening port\t: " + ListeningPort + "\nIP Address\t: " + IPAddress);

		String query = "INSERT INTO Registration(CLIENTNAME,IPADDRESS, LISTENINGPORT) VALUES("
				+ fmtStrDB(Hostname) + "," + fmtStrDB(IPAddress) + "," + ListeningPort + ")"
				+ "ON CONFLICT(IPADDRESS) DO UPDATE SET "
				+ "CLIENTNAME = " + fmtStrDB(Hostname) + "," + "LISTENINGPORT= " + ListeningPort + ";";

		executedDB(query);
	}

	/** gets the datagram socket of the server **/
	public static DatagramSocket getSocket(){
		return ds;
	}

}
