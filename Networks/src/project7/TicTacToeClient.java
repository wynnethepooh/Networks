package project7;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Tic Tac Toe
 * 
 * How to play: 1) Enter your username. 2) Enter 1 to start a new game. 3) Enter
 * 2 to make a move. 4) Enter the row of the the move you would like to make,
 * followed by the column of the move you would like to make. 5) Keep making
 * moves until someone wins or there is a stalemate.
 * 
 * Note: You have to make moves within a short amount of time or the game will
 * time out and end.
 * 
 * @author wynnetran
 *
 */
public class TicTacToeClient
{
	Scanner kb = new Scanner(System.in); // keyboard input
	CommandMessage command;
	MoveMessage move;
	BoardMessage board;
	Object serverMessage;
	ObjectOutputStream send;
	ObjectInputStream receive;

	boolean inProgress = false;

	/**
	 * Main method. Creates TicTacToeClient object and runs it.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		TicTacToeClient ticTacToe = new TicTacToeClient();

		try
		{
			ticTacToe.run();
		}
		catch (Exception err)
		{

		}
	}

	/**
	 * Connects to server and plays tic tac toe.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private void run() throws UnknownHostException, IOException
	{
		// Creates socket to communicate between server and client
		final Socket socket = new Socket("45.50.5.238", 38007);

		try
		{
			// Prints to server
			send = new ObjectOutputStream(socket.getOutputStream());

			// Reads input from server
			receive = new ObjectInputStream(socket.getInputStream());

			int option = 4;

			// Thread to continually receive messages from the server
			Thread thread = new Thread()
			{
				public void run()
				{
					try
					{
						while (true)
						{
							// Listen for messages from the server.
							serverMessage = receive.readObject();

							// Display the message
							if (serverMessage instanceof Message)
							{
								if (!(serverMessage instanceof ErrorMessage)
										&& !(serverMessage instanceof BoardMessage))
									System.out.println(serverMessage);
							}

							// If it's an error, display the error message
							if (serverMessage instanceof ErrorMessage)
							{
								System.out.println();
								System.out.println(((ErrorMessage) serverMessage).getError());
								System.out.println();
							}

							// If there has been a change to the board, display it.
							if (serverMessage instanceof BoardMessage)
							{
								board = (BoardMessage) serverMessage;
								printBoard(board.getBoard());

								if (board.getStatus() == BoardMessage.Status.IN_PROGRESS)
								{
									inProgress = true;
								}
								else
								{
									System.out.println(board.getStatus());
									inProgress = false;
									System.out.println("Goodbye!");
									socket.close();
									System.exit(0);
								}
							}
						}
					}
					catch (EOFException endOfFile)
					{
						System.out.println("///////////////////////////////");
						System.out.println("//  Timed out. Ending game.  //");
						System.out.println("///////////////////////////////");

						try
						{
							socket.close();
						}
						catch (Exception err)
						{

						}

						System.exit(0);
					}
					catch (Exception err)
					{
						err.printStackTrace();
					}
				}
			};

			thread.start();

			// Connect and create user
			System.out.println("Enter username: ");
			String username = kb.nextLine();
			ConnectMessage name = new ConnectMessage(username);
			// Send username to server
			send.writeObject(name);
			System.out.println("Name sent successfully.");

			System.out.println("Would you like to (1) start a new game or (0) exit?");
			option = kb.nextInt();
			kb.nextLine();

			while (true)
			{
				System.out.println();

				switch (option)
				{
					case 0: // Exit
					{
						command = new CommandMessage(CommandMessage.Command.EXIT);
						send.writeObject(command);
						System.out.println("Goodbye!");
						socket.close();
						System.exit(0);
					}
					case 1: // New game
					{
						if (!inProgress)
						{
							command = new CommandMessage(CommandMessage.Command.NEW_GAME);
							send.writeObject(command);

							System.out
									.println("=============================================");
							System.out.println("\nCreated new game successfully.");
							System.out.println("You are player 1.\n");

							inProgress = true;
						}
						System.out.println();

						break;
					}
					case 2: // Make a move
					{
						if (inProgress)
						{
							byte row = -1, col = -1;

							while (row < 0 || row > 2)
							{
								System.out.print("Enter row (0-2): ");
								row = kb.nextByte();
								kb.nextLine();
							}

							while (col < 0 || col > 2)
							{
								System.out.print("Enter column (0-2): ");
								col = kb.nextByte();
								kb.nextLine();
							}

							// Make a move using the user's input row and column
							move = new MoveMessage(row, col);
							send.writeObject(move);

							System.out.println();
						}

						break;
					}
					case 3: // Surrender
					{
						if (inProgress)
						{
							command = new CommandMessage(CommandMessage.Command.SURRENDER);
							send.writeObject(command);
							System.out.println("You have surrendered.");
							System.out.println("Game over.\n");

							inProgress = false;
							break;
						}

						inProgress = false;
						break;
					}
					default:
					{
						System.out.println("Invalid option.");
					}
				}

				if (!inProgress)
					System.out.println("=============================================");
				else
					System.out.println("---------------------------------------------");

				System.out.println("\nPlease select an option: ");

				if (!inProgress)
					System.out.println("1. New game");
				if (inProgress)
				{
					System.out.println("2. Make a move");
					System.out.println("3. Surrender");
				}
				if (!inProgress)
				{
					System.out.println("0. Exit");
				}
				option = kb.nextInt();
				kb.nextLine();
			}
		}
		catch (Exception err)
		{

		}
	}

	/**
	 * Prints the current board
	 * 
	 * @param board
	 */
	private void printBoard(byte[][] board)
	{
		System.out.println("\nCurrent board: \n");
		for (int row = 0; row < board.length; row++)
		{
			System.out.print("\t ");
			for (int col = 0; col < board[row].length; col++)
			{
				System.out.print(board[row][col] + " ");
			}
			System.out.println();
		}
	}
}