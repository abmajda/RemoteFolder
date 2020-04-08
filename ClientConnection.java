package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnection {
	
	// declare the basic stuff
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean authenticated = false;
	private InputStream instream = null;
	private OutputStream outstream = null;
	
	public ClientConnection(String address, int port, String username, String password) throws Exception
	{
		// keeps track of authenticated or not
		String authenticatedString;
		
		// connect and authenticate
		try
		{
			socket = new Socket(address, port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			try {
				instream = socket.getInputStream();
				outstream = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// send the authentication
			out.writeUTF(username);
			out.writeUTF(password);
			
			// receive reply of if authenticated or not
			authenticatedString = in.readUTF();
			authenticated = Boolean.parseBoolean(authenticatedString);
			
			// if authenticated switch GUI and allow for next communications
			if (authenticated)
			{
				System.out.println("Client authenticated");
			}
			else
			{
				System.out.println("Client did not authenticate");
				throw new Exception("user/pw");
			}
		}
		catch (Exception error)
		{
			throw error;
		}
	}
	
	public String[] listFiles() throws IOException
	{
		String[] fileList = null;
		
		try 
		{
			// send the directory request with the path
			out.writeUTF("dir");
			
			// receive the number of files and return it
			int fileno = Integer.parseInt(in.readUTF());
			fileList = new String[fileno];
			for (int i = 0; i < fileno; i++)
			{
				fileList[i] = in.readUTF();
			}
		} 
		catch (IOException error) 
		{
			throw error;
		}
		
		return fileList;
	}
	
	public boolean delete(String path) throws IOException
	{
		try 
		{
			out.writeUTF("del");
			out.writeUTF(path);
			
			return Boolean.parseBoolean(in.readUTF());
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	public boolean updatePath(String newPath) throws IOException
	{
		if (newPath.charAt(0) == '*' && newPath.charAt(newPath.length() - 1) == '*')
		{
			newPath = newPath.substring(1, newPath.length() - 1);
		}
		else
		{
			return false;
		}
		
		try 
		{
			out.writeUTF("path");
			out.writeUTF(newPath);
			
			return Boolean.parseBoolean(in.readUTF());
		} 
		catch (IOException error) 
		{
			// TODO Auto-generated catch block
			throw error;
		}
	}
	
	public void goBack() throws IOException
	{
		try 
		{
			out.writeUTF("back");
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	public void close() throws IOException
	{
		// close resources after done
		try 
		{
			out.writeUTF("close");
			socket.close();
			in.close();
			out.close();
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	public void create(String name) throws IOException
	{
		try 
		{
			out.writeUTF("make");
			out.writeUTF(name);
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	public void download(String filename) throws IOException
	{
		try 
		{
			out.writeUTF("down");
			out.writeUTF(filename);
			
			byte[] bytes = new byte[10000]; 
			BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(filename));
			int bytesread = 0;
			do
			{
				bytesread = instream.read(bytes);
				outstream.write(bytes, 0, bytesread);
			}
			while (bytesread == bytes.length);
			outstream.flush();
			outstream.close();
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	public void upload(String filename) throws IOException
	{
		try 
		{
			out.writeUTF("up");
			out.writeUTF(filename);
			
			File resource = new File(filename);
			//out.writeUTF(String.valueOf(resource.length()));
			byte[] bytes = new byte[10];
			int bytesread = 0;
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(resource));
			do
			{
				bytesread = instream.read(bytes);
				out.write(bytes, 0, bytesread);
			}
			while(bytesread == bytes.length);
			out.flush();
			instream.close();
		} 
		catch (IOException error) 
		{
			error.printStackTrace();
			throw error;
		}
	}
}
