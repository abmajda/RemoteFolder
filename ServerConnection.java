package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ServerConnection extends Thread {
	
	// set up the basics
	private Socket socket = null;
	private ServerSocket server = null;
	private Semaphore sem = new Semaphore(1);
	private int clientCount = 0;
	private int port;
	
	public ServerConnection(int port)
	{
		this.port = port;
	}
	
	public void run()
	{
			try {
				server = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Listening on port " + port);
			
			// get client request
			while(true)
			{
				// reset the socket
				socket = null;
				
				// assign a new thread to handle the connection
				try
				{
					// receive new clients and update the status
					socket = server.accept();
					clientCount++;
					
					// obtain in and out data streams
					DataInputStream in = new DataInputStream(socket.getInputStream());
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					
					System.out.println("Assigning new thread for client number " + clientCount);
					Thread handler = new ClientHandler(socket, in, out, clientCount, sem);
					handler.start();
				}
				catch (Exception error)
				{
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Could not assign thread for client number " + clientCount);
				}
			}
	}
}
