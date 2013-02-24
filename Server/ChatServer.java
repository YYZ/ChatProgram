import java.net.*;
import java.io.*;
import java.util.*;

class MessageSender
{
	private Queue<String> messages = new LinkedList<String>();
	private ArrayList<Handler> handlers;
	
	public MessageSender(ArrayList<Handler> handlers0)
	{
		handlers = handlers0;
	}
	
	//Gets a message from a Handler, then adds it to the Queue.
	public synchronized void getMessage(String m) 
	{
		messages.add(m);
	}
	
	//Takes the first message on the Queue and sends it to each client currently in the ArrayList of current connections.
	public synchronized void sendMessages() 
	{
		Handler sender = new Handler();// Handler to send messages.
		String output = messages.remove();// Take message from queue.
		for(int i = 0;i<handlers.size();i++)//for each client connected.
		{
			sender = handlers.get(i);//take a copy of the client.
			boolean check = sender.send(output);//send message.
			if(check == true)//if false, then the client has disconnected, so they are removed from the ArrayList.
			{
				handlers.remove(i);
				i--;
			}
		}
		System.out.println("Users : "+handlers.size());// Current connections.
		
	}
	
	public synchronized void run()
	{
		sendMessages();
	}
}

class Handler extends Thread
{
	private Socket socket = null;
	private MessageSender sender = null;
	private BufferedReader socketIn = null;
	private String nickname = "";
	private String message = "";
	private PrintWriter out;
	
	public Handler(){}
	
	//Constructor, takes the client socket and copy of MessageSender from Main.
	public Handler(MessageSender m, Socket socket)
	{
		this.socket = socket;
		sender = m;
	}
	
	//Send a message to the client.
	public boolean send(String s)
	{
		try
		{
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(s);
			return false;
		}
		catch(IOException q)
		{
			return true;// Client has disconnected.
		}
	}
	
	public void run()
	{
		try
		{
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
			socketIn = new BufferedReader (new InputStreamReader(socket.getInputStream()));
			nickname = socketIn.readLine();// Get the clients Name.
			sender.getMessage(nickname + " has just joined the chat room.");// Send a message to the MessageSender.
			sender.run();// Run the sender (ie. send the message.)
		
			while((message = socketIn.readLine()) != null)//While there's something to be read.
			{
				sender.getMessage(nickname + ": " + message);
				sender.run();
			}
			System.out.println(nickname+ " has left."); // If nothing to be read, client will be terminated.
			sender.getMessage(nickname + " has left the chat");
			socket.close();// Close the socket.
			out.close();// Close the PrintWriter.
			socketIn.close();// Close the BufferedReader.
			sender.run();
		}
		catch (IOException e)
		{
			System.out.println(nickname + "'s run method");
		}
	}
}
class ChatServer
{
	public static void main (String [] args) throws IOException
	{
		ServerSocket serverSocket  = null;// Creates the server socket to listen for connections.
		ArrayList<Handler> handlers = new ArrayList<Handler>();// ArrayList to keep track of connections.
		MessageSender s = new MessageSender(handlers); // To keep track of messages and to send them.
		try
		{
			serverSocket = new ServerSocket(7777);
		}
		catch (IOException e)
		{
			System.err.println("WOOPS");
			System.exit(1);
		}
		
		Socket clientSocket = null;
		try
		{
			clientSocket = serverSocket.accept();
		}
		catch(IOException e)
		{
			System.err.println("woops");
		}
		
		while(true)
		{
			Handler h = new Handler(s, serverSocket.accept());//New Handler thread for incoming connection, start the connection.
			handlers.add(h);// Add the handler to the arraylist.
			h.start();//Start the handler.	
		}
		
	}
}