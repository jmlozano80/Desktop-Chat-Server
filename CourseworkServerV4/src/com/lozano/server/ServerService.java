package com.lozano.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JTextArea;

/**
 * @author Jose Manuel Lozano Serrano ID 11033743
 * 
 * This class provide the multi-threading environment for handling the clients.
 */
public class ServerService implements Runnable
{
	private PrintWriter output;
	private Scanner input;
	private String nameOfUser;
	private Socket clientSocket;
	private JTextArea serverConsole;
	
/**
 * Constructor
 * @param nameOfUser
 * @param clientSocket
 * @param serverConsole
 */
	public ServerService(String nameOfUser, Socket clientSocket, JTextArea serverConsole)
	{
		this.nameOfUser = nameOfUser;
		this.clientSocket = clientSocket;
		this.serverConsole = serverConsole;
	
	}//end constructor

	
	
	/**
	 * This method is necessary due to the implementation of Runnable interface.
	 * Listen to the incoming request and calls the appropriate method to deliver service to the clients
	 */
	@Override
	public void run() 
	{
		try
		{
			output = new PrintWriter(clientSocket.getOutputStream());
			input = new Scanner(clientSocket.getInputStream());
			informOtherUsersOfConnection();
		}
		catch (IOException e) 
		{
			serverConsole.append("Problem with creating output/input \n");
			e.printStackTrace();
		}
				
		while (input.hasNext())
		{
			int option = Integer.parseInt(input.nextLine());
			switch (option)
			{
			
				//Client sends Integers through the geInputStream:
				case 1: //option 1 user leaving the chat and informing other users
					try 
					{
						userLeavingChat();
						break;
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
									
				case 2: //option 2 user send a message to everybody				
					try 
					{
						sendMessageToAll();
						
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
					break;
				case 3: //option 3 to send private messages
					sendToOne();
					break;
					
			}//end switch					
			
		}//end while loop
		
	}//end method run
	
				
		
	/**
	 * 	This is a synchronised method (displays the messages in order otherwise the order of messages are not guarantee).
	 *  It sends messages in private chat room.
	 */
	
	private synchronized void sendToOne() 
	{
		String friendForPrivateChat = input.nextLine();
		String theMessage = input.nextLine();
		int indexOfFriend = Server.usersOnline.indexOf(friendForPrivateChat);
		
		try
		{
			
			PrintWriter friendOutput = new PrintWriter(Server.socketList.get(indexOfFriend).getOutputStream(), true);
			friendOutput.println("4");
			friendOutput.println(nameOfUser);
			friendOutput.println(theMessage);
			serverConsole.append( nameOfUser +" is sending a private message to " +friendForPrivateChat+"\n");
		}
		catch (IOException e)
		{
			System.out.println("Impossible to create the output stream");
			e.printStackTrace();
		}
	}//end method sendToOne
	
	
	
	/**
	 * This is a synchronised method (displays the messages in order otherwise the order of messages are not guarantee)
	 * It sends messages to the main console for every client connected to the chat
	 * @throws IOException
	 */
	private synchronized void sendMessageToAll() throws IOException
	{
		String clientName = input.nextLine();
		String message = input.nextLine();
		serverConsole.append( clientName + "> said: " +"'"+ message+"'"+"\n");
		for (int i = 0; i < Server.usersOnline.size(); i++) 
		{
			PrintWriter temporary = new PrintWriter(Server.socketList.get(i).getOutputStream(),true);
			temporary.println("2"); 
			temporary.println(clientName + "> " + message);
		
		}// end for loop
		
	}//end method sendMessageToAll
	
	
	
	/**
	 * This synchronised method inform to every connected client when a client leave the chat
	 *  and remove him/her from the 2  ArrayLists (one for sockets and another for listing the users online).
	 */
	private synchronized void userLeavingChat() throws IOException
	{
		
		for (int i = 0; i < Server.socketList.size(); i++)
		{
			PrintWriter temporary = new PrintWriter(Server.socketList.get(i).getOutputStream(), true);
			//temporary.println("2");
			//temporary.println( Server.usersOnline.get(i)  );
			temporary.println("3");
			temporary.println(nameOfUser);
			
		}//end for loop

		int index = Server.usersOnline.indexOf(nameOfUser);
		serverConsole.append( nameOfUser  + "> has left the chat. "+"\n");
		
		Server.usersOnline.remove(index);
		Server.socketList.remove(index);
	
	}//end method userLeavingChat
	
	
	
	/**
	 * This synchronised method informs to all connected clients when a new client connects to the chat update useronline list.
	 * 1st loop check everybody connected for each connected user creates a temporary print writer then using nested loop
	 * sends all users online to itself using the temporary print writer 
	 */
	private synchronized void informOtherUsersOfConnection() throws IOException
	{
		
		for (int i = 0; i < Server.socketList.size(); i++)
		{
			
			PrintWriter temporary = new PrintWriter(Server.socketList.get(i).getOutputStream(), true);
			temporary.println("2");
			temporary.println(nameOfUser  + "> has connected to the chat. ");
			
			for (int j = 0; j < Server.socketList.size(); j++) 
			{
				temporary.println("1");
				temporary.println(Server.usersOnline.get(j));
				
			}
		}
	
	}//end method informOtherUsersOfConnection
	
	
}//end class
