package project3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Ipv4Client
{
	byte[] byteMessage;
	byte version = 4;									// IPv4
	byte hlen = 5;										// 5 words
	byte tos = 0;											// (do not implement)
	int length = 0;										// max 65,535 bytes
	byte ident = 0;										// (do not implement)
	byte flags = 4;										// bit 0 is 0, bit 1: DF, bit 2: MF (0)
	byte offset = 0;									// (do not implement)
	byte TTL = 50;										// assume 50
	byte protocol = 6;								// TCP
	long checksum = 0;
	
	public static void main(String[] args)
	{
		Ipv4Client ipv4 = new Ipv4Client();
		ipv4.run();
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
			final Socket socket = new Socket("45.50.5.238", 38003);

			// Prints to server
			PrintStream send = new PrintStream(socket.getOutputStream(), true);
			// Reads input from server
			BufferedReader receive = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String message;
			int dataSize = 1;
			
			// Sends 12 packets and prints verification message
			for (int ndx = 0; ndx < 12; ndx++)
			{
				dataSize *= 2;
				
				// Create and send packet
				byte[] packet = createPacket(dataSize);
				send.write(packet);
				
				// Receive and print message
				message = receive.readLine();
				System.out.println(message);
			}
		}
		catch (Exception err)
		{

		}
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
	 * @param dataSize is the size of the data in the packet
	 * @return a packet
	 */
	private byte[] createPacket(int dataSize)
	{
		length = 20 + dataSize; // hlen is 20 bytes (5 words)
		byte[] packet = null;
		
		if (length < 32767)		// Max packet size is 65535
		{
			packet = new byte[length];
			
			packet[0] = (byte) ((version << 4) | hlen);	// version and header length
			packet[1] = tos;									// (do not implement)
			packet[2] = (byte)(length >> 8);	// the first 8 bits of the length
			packet[3] = (byte)length;					// the remaining bits of the length
			packet[4] = ident;								// (do not implement)
			packet[5] = ident;
			packet[6] = (byte) (flags << 4);	// (implement assuming no fragmentation)
			packet[7] = 0;										// offset
			packet[8] = TTL;									// assume 50
			packet[9] = protocol;							// TCP
			
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
			packet[10] = (byte)((short)checksum(packet) >> 8);	// first 8 bits
			packet[11] = (byte)((short)checksum(packet));				// remaining 8 bits
		}
		
		return packet;
	}
}
