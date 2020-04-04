package server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class StatusGUI {

	private DefaultListModel<String> statusTracker = new DefaultListModel<String>();
	private JFrame frame;

	// create the in process window
	public StatusGUI()
	{
		// code goes here
		frame = new JFrame("Remote Folder Server");
		frame.setSize(400, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// set up the scrollable status display
		JList statusDisplay = new JList(statusTracker);
		statusDisplay.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		statusDisplay.setLayoutOrientation(JList.VERTICAL);
		statusDisplay.setVisibleRowCount(-1);
		statusDisplay.setFixedCellWidth(155);
		JScrollPane statusScroller = new JScrollPane(statusDisplay);
		
		// set up the button
		JPanel panel = new JPanel();
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// code goes here
			}
		});
		panel.add(exit);
		
		// add to the frame and make it visible
		frame.add(BorderLayout.CENTER, statusScroller);
		frame.add(BorderLayout.SOUTH, panel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	void updateStatus(String status)
	{
		statusTracker.addElement(status);
	}
	
	void closeFrame()
	{
		frame.setVisible(false);
		frame.dispose();
	}
}
