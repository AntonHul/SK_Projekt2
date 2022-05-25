package server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ServerUI extends JFrame
{
	UDPServer udpServer;
	Thread ServerThread;
	JButton btnON;
	JButton btnOFF;
	
	//konstruktor
	ServerUI()
	{
		this.setSize(800, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		udpServer = new UDPServer();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout());
		
		btnON = new JButton("Server ON");
		btnON.addActionListener(new ONlistener());
		
		btnOFF = new JButton("Server OFF");
		btnOFF.addActionListener(new OFFlistener());
		btnOFF.setEnabled(false);
		
		mainPanel.add(btnON);
		mainPanel.add(btnOFF);
		
		this.add(BorderLayout.CENTER, mainPanel);
	}
	
	
	//klasy listenery
	class ONlistener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ServerThread = new Thread(udpServer);
			udpServer.start();
			ServerThread.start();
			btnON.setEnabled(false);
			btnOFF.setEnabled(true);
		}
	}
	
	class OFFlistener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			udpServer.stop();
			btnOFF.setEnabled(false);
			
			while(ServerThread.isAlive()){}
			
			btnON.setEnabled(true);
		}
		
	}
	
	
	//main
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				
				ServerUI ui = new ServerUI();
				ui.setVisible(true);
			}
		});
	}
}
