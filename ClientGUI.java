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
		panel.add(username);
		panel.add(usernameInput);
		panel.add(password);
		panel.add(passwordInput);
		panel.add(ipaddress);
		panel.add(ipInput);
		panel.add(port);
		panel.add(portInput);
		panel.add(errorDisplay);
		
		// set up the button
		JButton login = new JButton("Log In");
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get user input need to put in nested try/catch for input
				String username = usernameInput.getText();
				String password = String.valueOf(passwordInput.getPassword());
				String address = ipInput.getText();
				int port = Integer.parseInt(portInput.getText());
				
				// still need to handle bad user/password
				try
				{
					network = new ClientConnection(address, port, username, password);
					frame.setVisible(false);
					frame.dispose();
					createMainFrame();
				}
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
		
		JList<String> fileDisplay = new JList<String>(fileList);
		fileDisplay.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		fileDisplay.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		fileDisplay.setVisibleRowCount(-1);
		fileDisplay.setFixedCellWidth(150);
		JScrollPane fileScroller = new JScrollPane(fileDisplay);
		
		// the bottom panel
		JPanel bottom = new JPanel();
		JLabel errorDisplay = new JLabel(" ");
		errorDisplay.setSize(100, 50);
		JButton downloadButton = new JButton("Download");
		downloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					network.download(fileDisplay.getSelectedValue());
				} 
				catch (IOException error) 
				{
					errorDisplay.setText("Error, could not download");
				}
			}
		});
		
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
					errorDisplay.setText("Error, could not close");
				}
			}
		});
		
		bottom.add(errorDisplay);
		bottom.add(downloadButton);
		bottom.add(exitButton);
		
		// set up the side panel
		JPanel side = new JPanel();
		side.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		BoxLayout layout = new BoxLayout(side, BoxLayout.Y_AXIS);
		side.setLayout(layout);
		
		JButton newFileButton = new JButton("New");
		newFileButton.setMaximumSize(new Dimension(80, 25));
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
					errorDisplay.setText("Error, could not access network");
				}
			}
		});
		
		JButton openFileButton = new JButton("Open");
		openFileButton.setMaximumSize(new Dimension(80, 25));
		openFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					// delete the file and refresh the file listings
					boolean success = network.updatePath(fileDisplay.getSelectedValue());
					if (!success)
					{
						errorDisplay.setText("Error, could not open folder");
						JOptionPane.showMessageDialog(null, "Could not open folder", "Error", JOptionPane.INFORMATION_MESSAGE);
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
					errorDisplay.setText("Error, could not access network");
				}
			}
		});
		JButton backButton = new JButton("Back");
		backButton.setMaximumSize(new Dimension(80, 25));
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
					errorDisplay.setText("Error, could not retreive file list");
				}
			}
		});
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setMaximumSize(new Dimension(80, 25));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					// delete the file and refresh the file listings
					boolean success = network.delete(fileDisplay.getSelectedValue());
					if (!success)
					{
						errorDisplay.setText("Error, could not delete");
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
					errorDisplay.setText("Error, could not access network");
				}
			}
		});
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.setMaximumSize(new Dimension(80, 25));
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
					errorDisplay.setText("Error, could not retreive file list");
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
						} 
						catch (IOException error) 
						{
							errorDisplay.setText("Error, could not upload");
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
		
		// create the button
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
					frame.dispose();
					
				} 
				catch (IOException e1) 
				{
					// error handling goes here
				}
			}
		});
		
		frame.add(BorderLayout.CENTER, panel);
		frame.add(BorderLayout.SOUTH, submit);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
