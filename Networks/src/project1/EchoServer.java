package project1;

import java.io.*;
import java.net.*;

/**
 * 
 * @author Wynne Tran
 *
 */
public class EchoServer
{
	
	/**
	 * Constructor for server.
	 * 
	 * @param portNumber
	 *          the port number the server and client are connecting with.
	 */
	public EchoServer(int portNumber)
	{
		try
		{
			server = new ServerSocket(portNumber);
		}
		catch (Exception err)
		{
			System.out.println(err);
		}
	}

	/**
	 * Accepts input from the client and echoes their messages back to them.
	 */
	public void echo()
	{
		try
		{
			while (true)
			{
				// Takes in client
				Socket client = server.accept();
				String address = client.getInetAddress().getHostAddress();
				System.out.println("Client connected: " + address);
				
				// Reads input from client
				BufferedReader in = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				// Prints output to client
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				
				String line;
				out.println("");
				
				do
				{
					// Reads client's input
					line = in.readLine();
					// Echoes client's input
					out.println("Server> " + line);
				}
				while (!line.equals("exit"));
				System.out.println("Client disconnected.");
				client.close();
				System.exit(0);
			}
		}
		catch (Exception err)
		{
			System.err.println(err);
		}
	}

	public static void main(String[] args)
	{
		EchoServer s = new EchoServer(22222);
		s.echo();
	}
	
	private ServerSocket server;
}