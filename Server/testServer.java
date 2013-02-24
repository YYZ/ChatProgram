import java.net.*;
import java.io.*;
import java.util.*;

class MessageSender
{
	Queue<String> messages = new LinkedList<String>();
	ArrayList<Handler> handlers;
	
	public MessageSender(ArrayList<Handler> handlers0)
	{
		handlers = handlers0;
	}
	
	public synchronized void getMessage(String m) 
	{
		messages.add(m);
	}
	
	public synchronized void sendMessages() 
	{
		Handler sender = new Handler();
		String output = messages.remove();
		System.out.println(output);
		System.out.println(handlers.size());
		for(int i = 0;i<handlers.size();i++)
		{
			try
			{
				sender = handlers.get(i);
				sender.send(output);
			}
			catch(IOException q)
			{
				handlers.remove(i);
				System.out.println(handlers.size());
			}
		}
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
	BufferedReader socketIn = null;
	String nickname = "";
	String message = "";
	
	public Handler(){}
	
	public Handler(MessageSender m, Socket socket)
	{
		this.socket = socket;
		sender = m;
	}
	
	public void send(String s)
	{
	
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
		out.println(s);
	}
	
	public void run()
	{
		try
		{
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
			socketIn = new BufferedReader (new InputStreamReader(socket.getInputStream()));
			nickname = socketIn.readLine();
			sender.getMessage(nickname + " has just joined the chat room.");
			sender.run();
		
			while(true)
			{
				message = socketIn.readLine();
				if(message != null)
				{
					sender.getMessage(nickname + ": " + message);
					sender.run();
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("FUCK FUCK FUCK FUCK FUCK");
		}
	}
}
class testServer
{
	public static void main (String [] args) throws IOException
	{
		ServerSocket serverSocket  = null;
		ArrayList<Handler> handlers = new ArrayList<Handler>();
		MessageSender s = new MessageSender(handlers);
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
			Handler h = new Handler(s, serverSocket.accept());
			h.start();
			handlers.add(h);
		}
		
	}
}