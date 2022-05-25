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
		
		txtArea = new JTextArea();
		txtArea.setEditable(false);
		txtArea.setLineWrap(true);
		
		scrollPane = new JScrollPane(txtArea);
		
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new BtnListener());
		
		txtField = new JTextField();
		
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
	@SuppressWarnings("deprecation")
	public void check()
	{
		udp2.suspend();
	}
	
	//klasy listenery
	class BtnListener implements ActionListener
	{
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e)
		{
			udp2.send(txtField.getText());
			udp2.resume();
			txtField.setText("");
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
