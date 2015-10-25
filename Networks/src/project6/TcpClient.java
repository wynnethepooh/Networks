package project6;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

public class TcpClient
{
	int sequenceNumber;
	int acknowledge;

	public static void main(String[] args)
	{
		TcpClient tcp = new TcpClient();
		tcp.run();
	}

	/**
	 * Does three way handshake with the server, then sends 12 packets with the 
	 * first one having 2 bytes of data, then doubling the data size for each 
	 * consecutive packet. It then does a connection teardown.
	 */
	private void run()
	{
		try
		{
			// Creates socket to communicate between server and client
			final Socket socket = new Socket("45.50.5.238", 38006);

			// Prints to server
			PrintStream send = new PrintStream(socket.getOutputStream(), true);

			// Reads input from server (as bytes)
			DataInputStream receive = new DataInputStream(socket.getInputStream());

			// Message from server
			byte[] message = new byte[4];

			// Randomized sequence number
			Random rand = new Random();
			sequenceNumber = rand.nextInt();

			// Send IPv4 packet containing TCP header with random sequence num and SYN
			// flag (0b000000010)
			send.write(createIPv4(createTCP(0, sequenceNumber, 0, (byte) 0b00000010)));

			// Receive message from server (should be 0xCAFEBABE)
			receive.readFully(message);
			System.out.println(toHex(message));

			// Receive packet from server and get the sequence number for
			// acknowledgment
			byte[] serverPacket = new byte[20];
			receive.readFully(serverPacket);
			acknowledge = acknowledge | (serverPacket[4] & 0x000000FF);
			acknowledge = acknowledge << 8;
			acknowledge = acknowledge | (serverPacket[5] & 0x000000FF);
			acknowledge = acknowledge << 8;
			acknowledge = acknowledge | (serverPacket[6] & 0x000000FF);
			acknowledge = acknowledge << 8;
			acknowledge = acknowledge | (serverPacket[7] & 0x000000FF);

			// Send IPv4 packet containing TCP header that has value of seq num + 1,
			// ACK flag, and acknowledgment of server's seq num + 1
			send.write(createIPv4(createTCP(0, ++sequenceNumber, ++acknowledge,
					(byte) 0b00010000)));

			// Receive message from server (should be 0xCAFEBABE)
			receive.readFully(message);
			System.out.println(toHex(message));

			System.out.println("Three-way handshake was successful.");

			// Sends 12 packets and prints verification message
			int dataSize = 1;
			for (int ndx = 0; ndx < 12; ndx++)
			{
				dataSize *= 2;

				// Create packet
				byte[] packet = createIPv4(createTCP(dataSize, (sequenceNumber
						+ dataSize - 1), acknowledge, (byte) 0));

				send.write(packet);

				// Receive a magic number as a 4-byte array
				receive.readFully(message);
				// Print the message
				System.out.println(toHex(message));
			}

			sequenceNumber = sequenceNumber + 24 - 1;

			System.out
					.println("Packets sent successfully. Begin connection teardown.");

			// Begin connectoin teardown by sending a packet with the FIN flag set
			send.write(createIPv4(createTCP(0, sequenceNumber, acknowledge,
					(byte) 0b00000001)));

			// Receive message from server (should be 0xCAFEBABE)
			receive.readFully(message);
			System.out.println(toHex(message));

			// Server will respond with TCP header with ACK flag set to acknowledge
			// closing first half of connection, then a TCP header with FIN flag set
			// to begin closing second half of connection.
			byte[] ignore = new byte[20];
			receive.readFully(ignore);
			receive.readFully(ignore);

			// Respond with ACK flag set to confirm closing the second half of the
			// connection
			send.write(createIPv4(createTCP(0, ++sequenceNumber, acknowledge,
					(byte) 0b00010000)));

			// Receive message from server (should be 0xCAFEBABE)
			receive.readFully(message);
			System.out.println(toHex(message));
			
			System.out.println("Connection teardown complete.");

			socket.close();
		}
		catch (Exception err)
		{

		}
	}

	/**
	 * Creates the packet and fills in the header with the appropriate values.
	 * 
	 * @param dataSize
	 *          is the size of the data in the packet
	 * @return a packet
	 */
	private byte[] createIPv4(byte[] data)
	{
		int length = 20 + data.length; // hlen is 20 bytes (5 words) and data is 4
																		// bytes
		byte[] packet = null;

		byte version = 4; // IPv4
		byte hlen = 5; // 5 words
		byte tos = 0; // (do not implement)
		byte ident = 0; // (do not implement)
		byte flags = 4; // bit 0 is 0, bit 1: DF, bit 2: MF (0)
		byte offset = 0; // (do not implement)
		byte TTL = 50; // assume 50
		byte protocol = 6; // UDP

		if (length < 65535) // Max packet size is 65535
		{
			packet = new byte[length];

			packet[0] = (byte) ((version << 4) | hlen); // version and header length
			packet[1] = tos; // (do not implement)
			packet[2] = (byte) (length >> 8); // the first 8 bits of the length
			packet[3] = (byte) length; // the remaining bits of the length
			packet[4] = ident; // (do not implement)
			packet[5] = ident;
			packet[6] = (byte) (flags << 4); // (implement assuming no fragmentation)
			packet[7] = offset; // offset
			packet[8] = TTL; // assume 50
			packet[9] = protocol; // UDP

			// Source address: 127.0.0.1
			packet[12] = 127;
			packet[13] = 0;
			packet[14] = 0;
			packet[15] = 1;

			// Destination address: 45.50.5.238
			packet[16] = 45;
			packet[17] = 50;
			packet[18] = 5;
			packet[19] = (byte) 238;

			// Calculate and enter checksum
			packet[10] = (byte) ((short) checksum(packet) >> 8); // first 8 bits
			packet[11] = (byte) ((short) checksum(packet)); // remaining 8 bits

			if (data != null)
			{
				// Add data
				for (int ndx = 0; ndx < data.length; ndx++)
				{
					packet[ndx + 20] = data[ndx];
				}
			}
		}

		return packet;
	}

	/**
	 * Calculates the checksum of the packet
	 * 
	 * @param packet
	 * @return checksum
	 */
	private long checksum(byte[] packet)
	{
		long sum = 0;

		for (int ndx = 0; ndx < packet.length; ndx += 2)
		{
			sum += (((packet[ndx] << 8) & 0xFF00) | (packet[ndx + 1] & 0xFF));

			if ((sum & 0xFFFF0000) > 0)
			{
				// carry occurred, so wrap around
				sum &= 0xFFFF;
				sum++;
			}
		}

		// 1's complement
		sum = ~sum;
		sum = sum & 0xFFFF;
		return sum;
	}

	/**
	 * Creates TCP packet.
	 * 
	 * @param dataSize
	 *          is the size of the data in the packet
	 * @param seqNum
	 *          is the sequence number of the TCP header
	 * @param ack
	 *          is the acknowledgement number of the TCP header
	 * @param flags
	 *          is the flags of the TCP header
	 * @return TCP packet with appropriate TCP header fields
	 */
	private byte[] createTCP(int dataSize, int seqNum, int ack, byte flags)
	{
		int length = 20 + dataSize; // header is 24 bytes
		byte[] packet = null;

		int srcPort = 0b0101010101010101;
		int dstPort = 0b1010101010101010;
		byte hdrLen = 5;
		byte advertisedWindow = 0;
		byte urgPtr = 0;

		if (length < 65535)
		{
			packet = new byte[length];

			packet[0] = (byte) (srcPort >> 8);
			packet[1] = (byte) srcPort;
			packet[2] = (byte) (dstPort >> 8);
			packet[3] = (byte) dstPort;
			packet[4] = (byte) (seqNum >> 24);
			packet[5] = (byte) (seqNum >> 16);
			packet[6] = (byte) (seqNum >> 8);
			packet[7] = (byte) seqNum;
			packet[8] = (byte) (ack >> 24);
			packet[9] = (byte) (ack >> 16);
			packet[10] = (byte) (ack >> 8);
			packet[11] = (byte) ack;
			packet[12] = (byte) (hdrLen << 4);
			packet[13] = flags;
			packet[14] = (byte) (advertisedWindow >> 8);
			packet[15] = advertisedWindow;
			packet[18] = (byte) (urgPtr >> 8);
			packet[19] = urgPtr;

			packet[16] = (byte) (TCPchecksum(packet) >> 8);
			packet[17] = (byte) TCPchecksum(packet);
		}

		return packet;
	}

	/**
	 * Checksum on TCP packets including the IPv4 pseudoheader.
	 * @param TCPpacket
	 * @return checksum
	 */
	private long TCPchecksum(byte[] TCPpacket)
	{
		int length = 12 + TCPpacket.length;
		byte[] pseudoPacket = new byte[length];

		for (int ndx = 0; ndx < TCPpacket.length; ndx++)
		{
			pseudoPacket[ndx] = TCPpacket[ndx];
		}

		// Source IPv4 address
		pseudoPacket[TCPpacket.length + 0] = 127;
		pseudoPacket[TCPpacket.length + 1] = 0;
		pseudoPacket[TCPpacket.length + 2] = 0;
		pseudoPacket[TCPpacket.length + 3] = 1;

		// Destination IPv4 address
		pseudoPacket[TCPpacket.length + 4] = 45;
		pseudoPacket[TCPpacket.length + 5] = 50;
		pseudoPacket[TCPpacket.length + 6] = 5;
		pseudoPacket[TCPpacket.length + 7] = (byte) 238;

		pseudoPacket[TCPpacket.length + 8] = 0; // zeros
		pseudoPacket[TCPpacket.length + 9] = 6; // protocol: TCP
		pseudoPacket[TCPpacket.length + 10] = (byte) (TCPpacket.length >> 8);
		pseudoPacket[TCPpacket.length + 11] = (byte) TCPpacket.length;

		return checksum(pseudoPacket);
	}

	/**
	 * Converts a byte array to a hex string.
	 * 
	 * @param message
	 *          is the byte array
	 * @return message as a hex string
	 */
	private String toHex(byte[] message)
	{
		String test = "0x";

		for (byte b : message)
		{
			test += Integer.toHexString(b & 0xFF).toUpperCase();
		}

		return test;
	}
}
