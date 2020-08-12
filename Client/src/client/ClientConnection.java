package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
	
	// handle the initial log
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
				throw e;
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
	
	// list all the files on the remote folders current directory
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
	
	// send a delete command along with the file to delete
	public boolean delete(String path) throws IOException
	{
		try 
		{
			// send the delete command along with the path
			out.writeUTF("del");
			out.writeUTF(path);
			
			// check if it was deleted or not
			return Boolean.parseBoolean(in.readUTF());
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	// changes the path at the remote folder to match the folder we are opening
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
			
			// returns true if successful
			return Boolean.parseBoolean(in.readUTF());
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	// sends a message to the server to take us up one level of the folder
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
	
	// closes the sockets and indicates to the server that we are done
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
	
	// creates a folder with the given name in the remote folder
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
	
	// sends a download request for the filename in current directory. Will download to the location of the client
	public void download(String filename) throws IOException
	{
		try 
		{
			// send the request
			out.writeUTF("down");
			out.writeUTF(filename);
			
			// set up the resources for transfer
			byte[] bytes = new byte[1024]; 
			BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(filename));
			int bytesread = 0;
			

			// set up the timing for our data collection
			long start = System.nanoTime();
			long current = start;
			long currentData = 0;
			FileWriter reporting = new FileWriter("last_session_report.txt");
			reporting.write("Download Starting \n");
			System.out.println("Download Starting");
			
			int byteNumber = 0;
			
			while ((byteNumber = Integer.parseInt(in.readUTF())) != -1)
			{
				bytesread = instream.read(bytes, 0, byteNumber);
				outstream.write(bytes, 0, byteNumber);
				
				//handle data recording
				current = System.nanoTime();
				currentData += bytesread;
				if ((current - start) > 1000000000)
				{
					double megs = (float) currentData / 1000000.0;
					double secs = (float) (current - start) / 1000000000.0;
					System.out.println("Mbps = " + (megs/secs));
					reporting.write("Mbps = " + (megs/secs) + "\n");
					currentData = 0;
					start = current;
				}
			}
			
			// close out all resources
			System.out.println("Download complete");
			reporting.write("Download complete \n");
			reporting.close();
			outstream.flush();
			outstream.close();
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
	
	// sends an upload request using the filename (which includes path information)
	public void upload(String filename) throws IOException
	{
		// make sure the file exists before starting the upload process
		File resource = new File(filename);
		boolean fileExists = resource.exists();
		if (!fileExists)
		{
			throw new IOException();
		}
		
		try 
		{
			// send the request
			out.writeUTF("up");
			out.writeUTF(filename);
			
			// set up the resources
			byte[] bytes = new byte[1024];
			int bytesread = 0;
			
			// prepare the data collection
			long start = System.nanoTime();
			long current = start;
			long currentData = 0;
			FileWriter reporting = new FileWriter("last_session_report.txt");
			reporting.write("Upload Starting \n");
			System.out.println("Upwnload Starting");
			
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(resource));
			
			while ((bytesread = instream.read(bytes)) != -1)
			{
				out.writeUTF(String.valueOf(bytesread));
				out.write(bytes, 0, bytesread);
				
				// added here
				current = System.nanoTime();
				currentData += bytesread;
				if ((current - start) > 1000000000)
				{
					double megs = (float) currentData / 1000000.0;
					double secs = (float) (current - start) / 1000000000.0;
					System.out.println("Mbps = " + (megs/secs));
					reporting.write("Mbps = " + (megs/secs) + "\n");
					currentData = 0;
					start = current;
				}
			}
			
			out.writeUTF(String.valueOf(bytesread));
			
			// close out resources
			System.out.println("Upload complete");
			reporting.write("Upload complete \n");
			reporting.close();
			out.flush();
			instream.close();
		} 
		catch (IOException error) 
		{
			throw error;
		}
	}
}
