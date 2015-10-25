package project2;

import java.io.*;
import java.net.*;

/**
 * 
 * @author Wynne Tran
 *
 */
public class ChatClient
{
	public static void main(String[] args)
	{
		try
		{
			// Creates socket to communicate between server and client
			final Socket socket = new Socket("45.50.5.238", 38002);

			// Reads input from keyboard
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(
					System.in));

			// Prints to server
			PrintWriter send = new PrintWriter(socket.getOutputStream(), true);

			String message;

			// Thread to continually receive messages from the server
			Thread thread = new Thread()
			{
				public void run()
				{
					String serverMessage;

					try
					{
						// Reads input from server
						BufferedReader receive = new BufferedReader(new InputStreamReader(
								socket.getInputStream()));

						while (true)
						{
							// If there is a message, print it
							if ((serverMessage = receive.readLine()) != null)
							{
								System.out.println(serverMessage);
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			};

			thread.start();

			System.out.println("Enter username");

			while (true)
			{
				// Send keyboard input
				message = keyboard.readLine();
				send.println(message);
			}
		}
		catch (Exception err)
		{

		}
	}
}