package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClientUI extends JFrame
{
	UDPClient udp1;
	UDPClient2 udp2;
	
	JButton sendBtn;
	JTextArea txtArea;
	JScrollPane scrollPane;
	JTextField txtField;
	
	//konstruktor
	public ClientUI()
	{
		this.setSize(800, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("eGoat Client");
		
		txtArea = new JTextArea();
		txtArea.setEditable(false);
		txtArea.setLineWrap(true);
		
		scrollPane = new JScrollPane(txtArea);
		
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new BtnListener());
		
		txtField = new JTextField();
		txtField.addActionListener(new TFListener());
		
		udp1 = new UDPClient(txtArea);
		udp2 = new UDPClient2(txtArea, sendBtn, this, udp1);
		
		this.add(BorderLayout.CENTER, scrollPane);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		panel.add(BorderLayout.CENTER, txtField);
		panel.add(BorderLayout.EAST, sendBtn);
		
		this.add(BorderLayout.SOUTH, panel);
		
		udp1.start();
		udp2.start();
	}
	
	//metody
	
	//klasy listenery
	class BtnListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(txtField.getText().equals("close") || txtField.getText().equals("Close"))
			{
				udp2.close();
				txtField.setText("");
				sendBtn.setEnabled(true);
				System.exit(0);
			}
			else
			{
				udp2.send(txtField.getText());
				txtField.setText("");
			}
		}
	}
	
	class TFListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			sendBtn.doClick();
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
				ClientUI ui = new ClientUI();
				ui.setVisible(true);
			}
		});
	}
}
