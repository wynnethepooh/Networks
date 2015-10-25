package project5;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

public class UdpClient
{
	public static void main(String[] args)
	{
		UdpClient udp = new UdpClient();
		udp.run();
	}

	/**
	 * Sends 12 packets with the first one having 2 bytes of data, then doubling
	 * the data size for each consecutive packet.
	 */
	private void run()
	{
		try
		{
			// Creates socket to communicate between server and client
			final Socket socket = new Socket("45.50.5.238", 38005);

			// Prints to server
			PrintStream send = new PrintStream(socket.getOutputStream(), true);

			// Reads input from server (as bytes)
			DataInputStream receive = new DataInputStream(socket.getInputStream());

			// Message from server
			byte[] message = new byte[4];
			
			// Data: 0xDEADBEEF
			byte[] data = new byte[4];
			data[0] = (byte) (0xDEADBEEF >> 24); // DE
			data[1] = (byte) (0xDEADBEEF >> 16); // AD
			data[2] = (byte) (0xDEADBEEF >> 8); // BE
			data[3] = (byte) 0xDEADBEEF; // EF
			// Handshake packet
			byte[] handshake = createIPv4(data);
			send.write(handshake);

			// Receive message from server (should be 0xCAFEBABE)
			receive.readFully(message);
			System.out.println(toHex(message));

			// Receive port number from server
			int port = receive.readUnsignedShort();

			int dataSize = 1;
			double sum = 0;

			// Sends 12 packets and prints verification message
			for (int ndx = 0; ndx < 12; ndx++)
			{
				dataSize *= 2;

				// Create packet
				byte[] packet = createIPv4(createUDP(dataSize, port));
				
				// Send packet and record time
				long initTime = System.nanoTime();
				send.write(packet);
				long finalTime = System.nanoTime();
				
				sum += (double)((finalTime - initTime) / 1000000000.0);

				System.out.println((double)((finalTime - initTime) / 1000000000.0) + "ms");

				// Receive a magic number as a 4-byte array
				receive.readFully(message);
				// Print the message
				System.out.println(toHex(message));
			}
			
			System.out.println("Average RTT: " + (sum / 12) + "ms");
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
		int length = 20 + data.length; // hlen is 20 bytes (5 words) and data is 4 bytes
		byte[] packet = null;

		byte version = 4; // IPv4
		byte hlen = 5; // 5 words
		byte tos = 0; // (do not implement)
		byte ident = 0; // (do not implement)
		byte flags = 4; // bit 0 is 0, bit 1: DF, bit 2: MF (0)
		byte offset = 0; // (do not implement)
		byte TTL = 50; // assume 50
		byte protocol = 17; // UDP

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

			// Add data
			for (int ndx = 0; ndx < data.length; ndx++)
			{
				packet[ndx + 20] = data[ndx];
			}
		}

		return packet;
	}

	/**
	 * Calculates the checksum of the packet
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
	 * Creates the packet and fills in the header with the appropriate values.
	 * 
	 * @param dataSize
	 *          is the size of the data in the packet
	 * @return a packet
	 */
	private byte[] createUDP(int dataLength, int port)
	{
		int length = 8 + dataLength; // header is 8 bytes
		byte[] packet = null;

		if (length < 65535) // Max packet size is 65535
		{
			packet = new byte[length];

			packet[0] = 2; // source port
			packet[1] = 7;
			packet[2] = (byte) (port >> 8); // the first 8 bits of the dest port
			packet[3] = (byte) port; // the remaining bits of the dest port
			packet[4] = (byte) (length >> 8);
			packet[5] = (byte) length;

			// Fill data with random bytes
			Random rand = new Random(65535);
			byte[] data = new byte[dataLength];
			rand.nextBytes(data);

			// Put data in UDP packet
			for (int ndx = 0; ndx < dataLength; ndx++)
			{
				packet[ndx + 8] = data[ndx];
			}

			// Calculate and enter checksum
			packet[6] = (byte) ((short) UDPchecksum(packet) >> 8); // first 8 bits
			packet[7] = (byte) ((short) UDPchecksum(packet)); // remaining 8 bits
		}

		return packet;
	}

	/**
	 * 
	 * @param UDPpacket
	 * @return
	 */
	private long UDPchecksum(byte[] UDPpacket)
	{
		long checksum = 0;

		int length = 12 + UDPpacket.length;
		byte[] pseudoPacket = new byte[length];

		// Source IPv4 address
		pseudoPacket[0] = 127;
		pseudoPacket[1] = 0;
		pseudoPacket[2] = 0;
		pseudoPacket[3] = 1;

		// Destination IPv4 address
		pseudoPacket[4] = 45;
		pseudoPacket[5] = 50;
		pseudoPacket[6] = 5;
		pseudoPacket[7] = (byte) 238;

		pseudoPacket[8] = 0; // zeros
		pseudoPacket[9] = 17; // protocol
		pseudoPacket[10] = (byte) (UDPpacket.length >> 8);
		pseudoPacket[11] = (byte) UDPpacket.length;

		for (int ndx = 0; ndx < UDPpacket.length; ndx++)
		{
			pseudoPacket[ndx + 12] = UDPpacket[ndx];
		}

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
