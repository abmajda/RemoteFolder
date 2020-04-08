package server;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ServerGUI {
	
	// a server connection
	private ServerConnection network;

	// create the setup window
	void createSetup()
	{
		// set up frame
		JFrame frame = new JFrame("Remote Folder Server");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set up the panel
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(new Insets(25, 25, 25, 25)));
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);
		JLabel port = new JLabel("Please Enter Port Number: ");
		JTextField portInput = new JTextField(7);
		JLabel errorDisplay = new JLabel(" ");
		panel.add(port);
		panel.add(portInput);
		panel.add(errorDisplay);
		
		// make the button
		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// check if number and server are good then launch the server
				try
				{
					int portno = Integer.parseInt(portInput.getText());
					
					// check if server can be established
					try
					{
						network = new ServerConnection(portno);
						network.start();
						createStatus(portno);
						frame.setVisible(false);
						frame.dispose();
					}
					catch (Exception error)
					{
						errorDisplay.setText("Error, could not establish server");
					}
				}
				catch (Exception error)
				{
					errorDisplay.setText("Error, please enter a valid number");
				}
			}
		});
		
		// add to the frame and make it visible
		frame.add(BorderLayout.CENTER, panel);
		frame.add(BorderLayout.SOUTH, launch);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	void createStatus(int portno)
	{
		// set up the frame
		JFrame frame = new JFrame("Remote Folder Server");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		String port = String.valueOf(portno);
		JLabel status = new JLabel("Listening on port " + port, SwingConstants.CENTER);
		
		// add to frame and make visible
		frame.add(BorderLayout.CENTER, status);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
