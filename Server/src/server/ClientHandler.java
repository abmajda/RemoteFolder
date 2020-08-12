package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ClientHandler extends Thread {
	
	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private int clientCount;
	private String path = "C:\\RemoteFolder\\SharedFolder";
	private ArrayList<String> subfolders = new ArrayList<String>();
	private InputStream instream = null;
	private OutputStream outstream = null;
	private Semaphore sem;
	
	// set up the basic stuff
	public ClientHandler(Socket socket, DataInputStream in, DataOutputStream out, int clientCount, Semaphore sem)
	{
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.clientCount = clientCount;
		this.sem = sem;
		try {
			instream = socket.getInputStream();
			outstream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// a runnable which is the main loop for a server thread
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
				System.out.println("Client number " + clientCount + " has succesfully logged in");
				out.writeUTF("true");

				// set up the main loop
				boolean inUse = true;
				
				// wait for a response
				while (inUse)
				{
					String request = in.readUTF();
					switch (request)
					{
					case "dir":
						directory();
						break;
					case "del":
						delete();
						break;
					case "path":
						updatePath();
						break;
					case "back":
						goBack();
						break;
					case "make":
						createDirectory();
						break;
					case "down":
						download();
						break;
					case "up":
						upload();
						break;
					// user disconnects end the thread
					default:
						System.out.println("Client number " + clientCount + " has disconnected");
						inUse = false;
					}
				}
			}
			else
			{
				System.out.println("Client number " + clientCount + " failed to log in");
				out.writeUTF("false");
			}
		}
		catch (IOException e) 
		{
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
	
	// ass the directory of the sub-folders to the main path to get a path to the current directory
	private String subfolders()
	{
		String subfolderPath = "";
		
		if (subfolders != null)
		{
			for (int i = 0; i < subfolders.size(); i++)
			{
				subfolderPath = subfolderPath + subfolders.get(i);
			}
		}
		
		return subfolderPath;
	}

	// handle the user logging in
	private boolean loginHandling(String username, String password) 
	{
		// check the accounts text file for a user name and password pair
		try 
		{
			File loginList = new File("C:\\RemoteFolder\\Accounts.txt");
			Scanner reader = new Scanner(loginList);
			while (reader.hasNextLine())
			{
				String loginInfo = reader.nextLine();
				String[] user = loginInfo.split(", ");
				if (user[0].equals(username) && user[1].equals(password))
				{
					return true;
				}
			}
			
			reader.close();
		} 
		catch (FileNotFoundException error) 
		{
			System.out.println("Accounts cannot be accessed");		
		}
		
		return false;
	}
	
	// get a listing of the current directory
	private void directory()
	{
		try 
		{
			String fullPath = path + subfolders();
			File folder = new File(fullPath);
			File[] fileList = folder.listFiles();
			
			// find length and send it
			int fileno = fileList.length;
			out.writeUTF(String.valueOf(fileno));
			
			// then send all the files
			for (int  i = 0; i < fileno; i++)
			{
				// get rid of leading path
				String filename = String.valueOf(fileList[i]);
				filename = filename.substring(fullPath.length() + 1, filename.length());
				
				// mark directories
				if (fileList[i].isDirectory())
				{
					filename = "*" + filename + "*";
				}
				
				// send the name
				out.writeUTF(filename);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	// deletes a file from the shared folder
	private void delete() throws IOException
	{
		try 
		{
			sem.acquire();
			String received = in.readUTF();
			
			// remove the markings if it is a folder
			if (received.charAt(0) == '*' && received.charAt(received.length() - 1) == '*')
			{
				received = received.substring(1, received.length() - 1);
			}
			
			String filePath = path + subfolders() + "\\" + received;
			File file = new File(filePath);
			
			// if file is a folder, delete inside
			if(file.isDirectory())
			{
				String[] contents = file.list();
				if (contents != null)
				{
					folderDelete(file);
				}
			}
			
			// return whether deletion was successful
			if (file.delete())
			{
				out.writeUTF("true");
			}
			else
			{
				out.writeUTF("false");
			}
			
			sem.release();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	// a recursive call to delete a folder and everything in it. Used if a folder is selected for deletion
	private void folderDelete(File file)
	{
		String[] contents = file.list();
		for (int i = 0; i < contents.length; i++)
		{
			File deletion = new File(file.getPath(), contents[i]);
			if(file.isDirectory())
			{
				String[] newContents = deletion.list();
				if (newContents != null)
				{
					folderDelete(deletion);
				}
			}
			
			deletion.delete();
		}
	}

	// handle the user going to a new sub-folder
	private void updatePath()
	{
		try 
		{
			String subfolder = in.readUTF();
			String checkPath = path + subfolders() + "\\" + subfolder;
			File file = new File(checkPath);
			if (file.isDirectory())
			{
				subfolders.add("\\" + subfolder);
				out.writeUTF("true");
			}
			else
			{
				out.writeUTF("false"); 
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	// go to a higher level of the folder. Will not do anything if already at root
	private void goBack()
	{
		if (subfolders.size() != 0)
		{
			subfolders.remove(subfolders.size() - 1);
		}
	}
	
	// create a sub-folder with the name provided by the client
	private void createDirectory()
	{
		try 
		{
			String name = in.readUTF();
			String fullPath = path + subfolders() + "\\" + name;
			File directory = new File(fullPath);
			directory.mkdir();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	// handle a download request from the client
	public void download()
	{
		try 
		{
			// get the semaphore
			sem.acquire();
			
			// get the file requested
			String filename = path + subfolders() + "\\" + in.readUTF();
			File resource = new File(filename);
			byte[] bytes = new byte[1024];
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(resource));
			int bytesread;
			
			// send the file
			while ((bytesread = instream.read(bytes)) != -1)
			{
				out.writeUTF(String.valueOf(bytesread));
				outstream.write(bytes, 0, bytesread);
			}
			
			// make sure to signal the end of stream
			out.writeUTF(String.valueOf(bytesread));
			
			// close resources
			outstream.flush();
			instream.close();
			
			// release the semaphore
			sem.release();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	// handle an upload request by the client
	public void upload()
	{
		try 
		{
			// squire the semaphore
			sem.acquire();
			
			// get the file and open it server side
			String clientFilename = in.readUTF();
			String filename = path + subfolders() + clientFilename.substring(clientFilename.lastIndexOf("\\"), clientFilename.length());
			byte[] bytes = new byte[1024]; 
			BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(filename));
			int bytesread = 0;
			int byteNumber = 0;
			
			// read until we get an end of reading notification
			while ((byteNumber = Integer.parseInt(in.readUTF())) != -1)
			{
				bytesread = in.read(bytes, 0, byteNumber);
				outstream.write(bytes, 0, byteNumber);
			}
			
			// Close resources
			outstream.flush();
			outstream.close();
			
			// release the semaphore
			sem.release();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}
