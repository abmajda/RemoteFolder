package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClientGUI {

	private ClientConnection network;
	DefaultListModel<String> fileList = new DefaultListModel<String>();
	
	// Creates the Login window
	void createLogin()
	{
		// set up the frame
		JFrame frame = new JFrame("Remote Folder Client");
		frame.setSize(300, 270);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set up the panel
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(new Insets(25, 25, 25, 25)));
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		JLabel username = new JLabel("Please Enter Username: ");
		JTextField usernameInput = new JTextField(7);
		JLabel password = new JLabel("Please Enter Password: ");
		JPasswordField passwordInput = new JPasswordField(7);
		JLabel ipaddress = new JLabel("Please Enter IP Address: ");
		JTextField ipInput = new JTextField(7);
		JLabel port = new JLabel("Please Enter Port Number: ");
		JTextField portInput = new JTextField(7);
		JLabel errorDisplay = new JLabel(" ");
		// adds everything to the panel
		panel.add(username);
		panel.add(usernameInput);
		panel.add(password);
		panel.add(passwordInput);
		panel.add(ipaddress);
		panel.add(ipInput);
		panel.add(port);
		panel.add(portInput);
		panel.add(errorDisplay);
		
		// set up the login button which will attempt to authenticate using provided info
		JButton login = new JButton("Log In");
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get user input need to put in nested try/catch for input
				String username = usernameInput.getText();
				String password = String.valueOf(passwordInput.getPassword());
				String address = ipInput.getText();
				int port = Integer.parseInt(portInput.getText());
				
				// try to establish a connection and authenticate
				try
				{
					network = new ClientConnection(address, port, username, password);
					frame.setVisible(false);
					frame.dispose();
					createMainFrame();
				}
				// display an error message based on what went wrong
				catch (Exception error)
				{
					if(error.getMessage().equals("user/pw"))
					{
						errorDisplay.setText("Error, invalid username/password");
					}
					else
					{
						errorDisplay.setText("Error, could not connect to server");
					}
				}
			}
		});
		
		// add to the frame and make it visible
		frame.add(BorderLayout.CENTER, panel);
		frame.add(BorderLayout.SOUTH, login);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// creates the main window
	void createMainFrame()
	{
		// set up the frame
		JFrame frame = new JFrame("Remote Folder Client");
		frame.setSize(590, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// get the initial list of files
		try 
		{
			String[] networkFileList = network.listFiles();
			fileList.removeAllElements();
			
			// for each file name update
			for (int i = 0; i < networkFileList.length; i++)
			{
				fileList.addElement(networkFileList[i]);
			}
		} 
		catch (IOException error) 
		{
			// error handling
		}
		
		// set up the display that will list the files
		JList<String> fileDisplay = new JList<String>(fileList);
		fileDisplay.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		fileDisplay.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		fileDisplay.setVisibleRowCount(-1);
		fileDisplay.setFixedCellWidth(150);
		JScrollPane fileScroller = new JScrollPane(fileDisplay);
		
		// the bottom panel
		JPanel bottom = new JPanel();
		JButton downloadButton = new JButton("Download");
		// attempts to download the selected file if download is pressed
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.download(fileDisplay.getSelectedValue());
					JOptionPane.showMessageDialog(frame, "Download complete. See last_session_report for details");
				} 
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error in downloading");
				}
			}
		});
		
		// closes the socket and exits the program when exit is pressed
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.close();
					System.exit(0);
				} 
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error in closing network");
					System.exit(1);
				}
			}
		});
		
		bottom.add(downloadButton);
		bottom.add(exitButton);
		
		// set up the side panel
		JPanel side = new JPanel();
		side.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		BoxLayout layout = new BoxLayout(side, BoxLayout.Y_AXIS);
		side.setLayout(layout);
		
		JButton newFileButton = new JButton("New");
		newFileButton.setMaximumSize(new Dimension(80, 25));
		/* if the new button is pressed, load the GUI to ask for the file name then create it
		 * and refresh the list afterwards
		 */
		newFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					getEntryFrame();
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
				}  
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error in accessing network");
				}
			}
		});
		
		JButton openFileButton = new JButton("Open");
		openFileButton.setMaximumSize(new Dimension(80, 25));
		// if the open button is pressed, send the new path to the network for root and refresh
		openFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					// send the selected path to the network
					boolean success = network.updatePath(fileDisplay.getSelectedValue());
					if (!success)
					{
						JOptionPane.showMessageDialog(frame, "Error opening folder");
					}
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
				}  
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error could not access network");
				}
			}
		});
		
		JButton backButton = new JButton("Back");
		backButton.setMaximumSize(new Dimension(80, 25));
		// if the back button is pressed sent to network to remove the last added path and refresh
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.goBack();
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
				} 
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error, could not retreive file list");
				}
			}
		});
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setMaximumSize(new Dimension(80, 25));
		// If delete is pressed send a delete request to the network and refresh
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					// delete the file and refresh the file listings
					boolean success = network.delete(fileDisplay.getSelectedValue());
					if (!success)
					{
						JOptionPane.showMessageDialog(frame, "Error in deleting");
					}
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
				}  
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error in accessing network");
				}
			}
		});
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.setMaximumSize(new Dimension(80, 25));
		// if refresh is clicked, clear the display and request the name of files in the directory to display
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
				} 
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error, could not retreive file lists");
				}
			}
		});
		
		side.add(newFileButton);
		side.add(Box.createVerticalStrut(15));
		side.add(openFileButton);
		side.add(Box.createVerticalStrut(15));
		side.add(backButton);
		side.add(Box.createVerticalStrut(15));
		side.add(deleteButton);
		side.add(Box.createVerticalStrut(15));
		side.add(refreshButton);
		side.setPreferredSize(new Dimension(100, 300));
		
		// the top panel
		JPanel top = new JPanel();
		JLabel bottomLabel = new JLabel("Please enter filename");
		JTextField fileEntry = new JTextField(15);
		JButton uploadButton = new JButton("Upload");
		// begin uploading a file to the server, then refresh
		uploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.upload(fileEntry.getText());
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
							
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
					JOptionPane.showMessageDialog(frame, "Upload complete. See last_session_report for details");
				} 
				catch (IOException error) 
				{
					JOptionPane.showMessageDialog(frame, "Error in uploading");
				}
			}
		});
				
		top.add(bottomLabel);
		top.add(fileEntry);
		top.add(uploadButton);
		
		// add to the frame and make visible
		frame.add(BorderLayout.WEST, side);
		frame.add(BorderLayout.NORTH, top);
		frame.add(BorderLayout.CENTER, fileScroller);
		frame.add(BorderLayout.SOUTH, bottom);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		// if the user closes the window without using the exit button, close the socket before exiting
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				try 
				{
					network.close();
					System.exit(0);
				} 
				catch (IOException error) 
				{}
			}
			});
	}
	
	private void getEntryFrame()
	{
		// create the frame
		JFrame frame = new JFrame("Remote Folder Client");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// create the entry box
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(new Insets(25, 25, 25, 25)));
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		JLabel label = new JLabel("Please enter the name of the file:");
		JTextField input = new JTextField(7);
		panel.add(label);
		panel.add(input);
		
		// a button that takes the name the user implements and creates a new directory on the server
		JButton submit = new JButton("submit");
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.create(input.getText());
					String[] networkFileList = network.listFiles();
					fileList.removeAllElements();
					
					// for each file name update
					for (int i = 0; i < networkFileList.length; i++)
					{
						fileList.addElement(networkFileList[i]);
					}
					// dispose of this frame when we are done
					frame.dispose();
				} 
				catch (IOException e1) 
				{
					// error handling goes here
				}
			}
		});
		
		// add everything to the frame and make it visible
		frame.add(BorderLayout.CENTER, panel);
		frame.add(BorderLayout.SOUTH, submit);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
