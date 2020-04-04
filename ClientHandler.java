package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
	
	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private int clientCount;
	StatusGUI GUI;
	
	public ClientHandler(StatusGUI GUI, Socket socket, DataInputStream in, DataOutputStream out, int clientCount)
	{
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.clientCount = clientCount;
		this.GUI = GUI;
	}
	
	public void run()
	{
		try
		{
			// authenticate the user
			String username = in.readUTF();
			String password = in.readUTF();
			boolean authenticated = loginHandling(username, password);
			
			// if user authenticates start accepting new requests
			if (authenticated)
			{
				GUI.updateStatus("Client number " + clientCount + " has succesfully logged in");
				out.writeUTF("true");
				// switch goes here
			}
			else
			{
				GUI.updateStatus("Client number " + clientCount + " failed to log in");
				out.writeUTF("false");
			}
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// close resources
		try 
		{
			this.socket.close();
			this.in.close();
			this.out.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private boolean loginHandling(String username, String password) 
	{
		// STUB
		if (username.equals("bob") && password.contentEquals("boy"))
		{
			return true;
		}
		
		return false;
	}
}
