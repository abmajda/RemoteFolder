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

	private boolean loginHandling(String username, String password) 
	{
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
	
	private void delete() throws IOException
	{
		try 
		{
			sem.acquire();
			String received = in.readUTF();
			
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
	
	private void goBack()
	{
		if (subfolders.size() != 0)
		{
			subfolders.remove(subfolders.size() - 1);
		}
	}
	
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
	
	public void download()
	{
		try 
		{
			sem.acquire();
			
			String filename = path + subfolders() + "\\" + in.readUTF();
			File resource = new File(filename);
			byte[] bytes = new byte[1024];
			int bytesread = 0;
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(resource));
			
			do
			{
				bytesread = instream.read(bytes);
				outstream.write(bytes, 0, bytesread);
			}
			while(bytesread == bytes.length);
			outstream.flush();
			instream.close();
			
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
	
	public void upload()
	{
		try 
		{
			sem.acquire();
			
			String clientFilename = in.readUTF();
			String filename = path + subfolders() + clientFilename.substring(clientFilename.lastIndexOf("\\"), clientFilename.length());
			byte[] bytes = new byte[1024]; 
			BufferedOutputStream outstream = new BufferedOutputStream(new FileOutputStream(filename));
			int bytesread = 0;
			do
			{
				bytesread = in.read(bytes);
				outstream.write(bytes, 0, bytesread);
			}
			while (bytesread == bytes.length);
			outstream.flush();
			outstream.close();
			
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
