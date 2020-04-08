package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection extends Thread {
	
	// set up the basics
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private int clientCount = 0;
	private int port;
	
	public ServerConnection(int port)
	{
		this.port = port;
	}
	
	public void run()
	{
		try 
		{
			server = new ServerSocket(port);
			
			System.out.println("Listening on port " + port);
			
			// get client request
			while(true)
			{
				// receive new clients and update the status
				socket = server.accept();
				clientCount++;
				
				// obtain in and out data streams
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				
				// assign a new thread to handle the connection
				try
				{
					System.out.println("Assigning new thread for client number " + clientCount);
					Thread handler = new ClientHandler(socket, in, out, clientCount);
					handler.run();
				}
				catch (Exception error)
				{
					socket.close();
					System.out.println("Could not assign thread for client number " + clientCount);
				}
			}
		} 
		catch (IOException error) 
		{
			
		}
	}
}
