package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {
	
	// set up the basics
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private int clientCount = 0;
	private StatusGUI GUI = new StatusGUI();
	
	public ServerConnection(int port) throws Exception
	{
		try 
		{
			server = new ServerSocket(port);
			
			// get client request
			while(true)
			{
				// receive new clients and update the status
				socket = server.accept();
				clientCount++;
				updateStatus("Client number " + clientCount + "is connected");
				
				// obtain in and out data streams
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				
				// assign a new thread to handle the connection
				try
				{
					updateStatus("Assigning new threat for client number " + clientCount);
					Thread handler = new ClientHandler(GUI, socket, in, out, clientCount);
					handler.run();
				}
				catch (Exception error)
				{
					socket.close();
					updateStatus("Could not assign thread for client number " + clientCount);
				}
			}
		} 
		catch (Exception error) 
		{
			GUI.closeFrame();
			throw error;
		}
	}
	
	private void updateStatus(String status)
	{
		GUI.updateStatus(status);
	}
}
