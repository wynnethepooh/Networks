package project1;

import java.io.*;
import java.net.*;

/**
 * 
 * @author Wynne Tran
 *
 */
public class EchoClient
{
	public static void main(String[] args)
	{
		try
		{
			// Creates socket to communicate between server and client
			Socket socket = new Socket("localhost", 22222);
			// Reads input from server
			BufferedReader serverInput = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			// Prints output
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			// Reads input from client
			BufferedReader clientInput = new BufferedReader(new InputStreamReader(
					System.in));

			String line;

			do
			{
				// Read input from server
				line = (serverInput.readLine());
				if (line.equals(""))
				{
					// Start echoing
				}
				else
				{
					// Print input from server
					System.out.println(line);
				}

				// Print input from client
				System.out.print("Client> ");
				line = clientInput.readLine();
				out.println(line);
			}
			while (!line.equals("exit"));
			System.exit(0);
		}
		catch (Exception err)
		{

		}
	}
}