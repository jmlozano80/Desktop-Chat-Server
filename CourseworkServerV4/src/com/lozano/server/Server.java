package com.lozano.server;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;


/**
 * @author Jose Manuel Lozano Serrano ID 11033743
 * Class Server
 */
public class Server
{
	//Global Variables
	
   private static JTextArea serverConsole;
   public static ArrayList<Socket> socketList = new ArrayList<Socket>();
   private static Scanner input;
   public static ArrayList<String> usersOnline = new ArrayList<String>();
   private JFrame window;
   private static int port=1980;
   private static Socket clientSocket;
   private static ServerSocket serverSocket;
  
   /**
    * The constructor calls the method buildServerGUI
    */
   public Server()
   {
	   buildServerGUI();
   }//end constructor
   
   /**
    * The main method calls the constructor and the method runServer
    */
   public static void main(String[] args) 
   {
	   Server server=new Server();
	   runServer();   
	   
   }//end main method
  
   /**
    * This method builds the Server GUI
    */
   public void buildServerGUI()
   {
	   window = new JFrame("Server Console");
	   serverConsole=new JTextArea();      
	   DefaultCaret caret = (DefaultCaret)serverConsole.getCaret();//autoscroll
       caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	   serverConsole.setLineWrap(true);
	   serverConsole.setWrapStyleWord(true);
	   window.setContentPane(new JScrollPane(serverConsole));
	   window.setSize(350,350);
	   serverConsole.setEditable(false);
	   // action to be taken when the server closes (inform the clients)
	   window.addWindowListener(new WindowListener()
	    {    
	    	// if the server closes inform the clients      
	       public void windowClosing(WindowEvent e)
	       {
	    	   for(int i=0;i<socketList.size();i++)
	    	   {
	    		 try 
	    		 {
					PrintWriter closingOutput = new PrintWriter(socketList.get(i).getOutputStream(),true);
					closingOutput.println("5");
				 } 
	    		 catch (IOException e1)
	    		 {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}   
	    	   }
	    	   System.exit(0);
	          
	       }
	       public void windowClosed(WindowEvent e) {}         
	       public void windowOpened(WindowEvent e) {}
	       public void windowIconified(WindowEvent e) {}
	       public void windowDeiconified(WindowEvent e) {}
	       public void windowActivated(WindowEvent e) {}
	       public void windowDeactivated(WindowEvent e) {}
	        
	    });
	   window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	   window.setVisible(true);
	   
   }//end method buildServerGUI
   
   

   
	/**
	 * This method run the Server.Creates a new ServerSocket with the indicated port and do an infinite loop
	 * accepting the client request for making thread by calling the method addNewClient.
	 * @exception IOException
	 * 
	 */
	public static void runServer()
	{  
		try
		{
			serverSocket = new ServerSocket(port);
			showInServerConsole("       .:: SERVER RUNNING WAITING FOR CLIENTS::.");
			//infinite loop waiting for clients
			while (true)
			{  
				TimeUnit.SECONDS.sleep(1);//sleep the Thread every 1s in order to do not use that much of CPU
				clientSocket = serverSocket.accept();
				addNewClient(clientSocket);
			 }
		} 
		catch (IOException e)
		{	
			System.out.println("ERROR: The server is already Running");
			JOptionPane.showMessageDialog(null, "An instance of the server is already running\n"
					+ " or other program is using the same port (1980)","Server Error",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
				e.printStackTrace();
		}
    
	}//end method runServer 
   
   
   /**
    * This method adds new clients to the ArrayList usersOnline and  starts the thread
    * @param Socket clientSocket
    */
   private static void addNewClient(Socket clientSocket) throws IOException
   {
	   input = new Scanner(clientSocket.getInputStream());
	   String nameOfUser = input.nextLine();
	   //duplicate name
	   if(usersOnline.contains(nameOfUser))
	   {
		   showInServerConsole(nameOfUser+"> Rejected because of duplicating name ");
		   PrintWriter output = new PrintWriter(clientSocket.getOutputStream(),true);//true for flush
		   output.println("6");
		   clientSocket.close();
	   
	   }
	   else
	   {
		   showInServerConsole("Adding new user");
		   socketList.add(clientSocket);
		   showInServerConsole(nameOfUser+"> has connected to the chat ");
		   //Add nameOfUser to array list
		   usersOnline.add(nameOfUser);
		   
		   ServerService servService = new ServerService(nameOfUser, clientSocket, serverConsole);
		   Thread thread = new Thread(servService);
		   thread.start();
	   }
   }//end method addNewClient
   
   
	/**
	 * This method appends messages to the Server Console txtArea
	 * @param String message
	 */
	public static void showInServerConsole(String message)
	{
		serverConsole.append(message+"\n");
	
	}//end method showInServerConsole	
   
}//end class



