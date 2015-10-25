package project4;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Ipv6Client
{
	byte version = 0b0110; 				// IPv6
	byte trafficClass = 0; 		// (do not implement)
	byte flowLabel = 0; 			// (do not implement)
	short payloadLen; 				// length of packet excluding header
	byte nextHeader = 17; 		// set to UDP protocol value (17)
	byte hopLimit = 20; 			// set to 20
	int length = 0; 					// length of header is always 40 bytes

	public static void main(String[] args)
	{
		Ipv6Client ipv6 = new Ipv6Client();
		ipv6.run();
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
			final Socket socket = new Socket("45.50.5.238", 38004);

			// Prints to server
			PrintStream send = new PrintStream(socket.getOutputStream(), true);

			// Reads input from server (as bytes)
			DataInputStream receive = new DataInputStream(socket.getInputStream());

			byte[] message = new byte[4];
			int dataSize = 1;

			// Sends 12 packets and prints verification message
			for (int ndx = 0; ndx < 12; ndx++)
			{
				dataSize *= 2;

				// Create and send packet
				byte[] packet = createPacket(dataSize);
				send.write(packet);

				// Receive a magic number as a 4-byte array
				receive.readFully(message);
				// Print the message
				System.out.println(toHex(message));
			}
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
	private byte[] createPacket(int dataSize)
	{
		payloadLen = (short) dataSize; // payload length is length excluding header
		length = 40 + dataSize; // length of header is always 40
		byte[] packet = null;

		if (length < 65535) // Max packet size is 65535 bytes
		{
			packet = new byte[length];

			packet[0] = (byte) (version << 4); // version and 0
			packet[1] = 0; // (do not implement traffic class & flow label)
			packet[2] = 0; // (do not implement flow label)
			packet[3] = 0; // (do not implement flow label)
			packet[4] = (byte) (payloadLen >> 8); // first 8 bits of payload length
			packet[5] = (byte) (payloadLen); // second 8 bits of payload length
			packet[6] = nextHeader; // implement and set to UDP protocol value: 17
			packet[7] = hopLimit; // implement and set to 20

			// Source address: 127.0.0.1
			packet[18] = (byte) 255;
			packet[19] = (byte) 255;
			packet[20] = (byte) 127;
			packet[21] = (byte) 0;
			packet[22] = (byte) 0;
			packet[23] = (byte) 1;

			// Destination address: 45.50.5.238
			packet[34] = (byte) 255;
			packet[35] = (byte) 255;
			packet[36] = (byte) 45;
			packet[37] = (byte) 50;
			packet[38] = (byte) 5;
			packet[39] = (byte) 238;
		}

		return packet;
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
