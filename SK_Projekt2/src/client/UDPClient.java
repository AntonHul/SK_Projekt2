package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

import config.Config;


public class UDPClient extends Thread {
	public static File[] listOfFiles;
	public static ArrayList<File_sha> Files_sha;
	 public final static int SOCKET_PORT = 13267;
	  public final static String SERVER = "127.0.0.1";
	  public final static String FILE_TO_RECEIVED =  "C:/Users/anton/Desktop/test2.txt";
	  public final static int FILE_SIZE = 6022386;
	  static byte[] stringContents;
	
	boolean running = true;
	JTextArea txtArea;
	  
	public UDPClient(JTextArea textArea)
	{
		super();
		txtArea = textArea;
	}
	
	public void stopThread()
	{
		running = false;
	}
	
	void sendFilesSHA(ArrayList<File_sha> Files)
	{
		Files_sha = Files;
	}
    
    public void run()
    {
    	while (running)
    	{
			DatagramSocket datagramSocket;
			try
			{
				datagramSocket = new DatagramSocket(SOCKET_PORT);
				DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
		    	datagramSocket.receive(receivedPacket);
		    	int length = receivedPacket.getLength();
		    	String message = new String(receivedPacket.getData(), 0, length, "utf8"); 
		    	datagramSocket.close();
	
		    	//sprawdzamy czy mamy taki plik
		    	for (File_sha file_sha: Files_sha)
		    	{
		    		if (file_sha.sha.equals(message))
		    		{
		    			InetAddress address_send = receivedPacket.getAddress();
		    			System.out.println("###############################");
		    			System.out.println("Get request from: ");
		    			System.out.println(address_send); 
		    			System.out.println("Sending the file with checksum:");
		    			System.out.println(message);
		    			System.out.println("###############################");
		    			
		    			txtArea.append("###############################\n");
		    			txtArea.append("Get request from: \n");
		    			txtArea.append(address_send.toString() + "\n");
		    			txtArea.append("Sending the file with checksum:\n");
		    			txtArea.append(message + "\n");
		    			txtArea.append("###############################\n");
		
		    			String FILE_TO_SEND = (file_sha.file.getPath());
		
			    	    FileInputStream fis = null; 
			    	    BufferedInputStream bis = null; 
			    	    OutputStream os = null; 
			    	    ServerSocket servsock = null; 
			    	    Socket sock = null; 
			    	    try
			    	    {
			    	    	servsock = new ServerSocket(SOCKET_PORT); 
			    	    	while (true)
			    	    	{ 
			    	    		System.out.println("Waiting..."); 
			    	    		txtArea.append("Waiting...\n");
			    	    		try
			    	    		{ 
			    	    			sock = servsock.accept(); 
			    	    			System.out.println("Accepted connection : " + sock); 
			    	    			// send file 
			    	    			File myFile = new File (FILE_TO_SEND); 
			    	    			byte [] mybytearray  = new byte [(int)myFile.length()]; 
			    	    			fis = new FileInputStream(myFile); 
			    	    			bis = new BufferedInputStream(fis); 
			    	    			bis.read(mybytearray,0,mybytearray.length); 
			    	    			os = sock.getOutputStream(); 
			    	    			System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
			    	    			txtArea.append("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)\n");
			    	    			os.write(mybytearray,0,mybytearray.length); 
			    	    			os.flush(); 
			    	    			System.out.println("Done.");
			    	    			txtArea.append("Done.\n");
			    	    		} 
			    	    		finally
			    	    		{ 
			    	    			if (bis != null) bis.close(); 
			    	    			if (os != null) os.close(); 
			    	    			if (sock!=null) sock.close(); 
			    	    		} 
			    	    	} 
			    	    }
			    	    catch (IOException e)
			    	    {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
			    	    finally
			    	    { 
			    	    	if (servsock != null)
								try {
									servsock.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
			    	    }
		    		}
		    	}
			}	
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
    	}
    }
}