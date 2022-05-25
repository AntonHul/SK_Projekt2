package client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import config.Config;

public class UDPClient2 extends Thread
{
	public static File[] listOfFiles;
	public static ArrayList<File_sha> Files_sha;
	 public final static int SOCKET_PORT = 13267;
	  public final static String SERVER = "127.0.0.1";
	  public final static String FILE_TO_RECEIVED =  "C:/Users/anton/Desktop/test2.txt";
	  public final static int FILE_SIZE = 6022386;
	  static byte[] stringContents;
	
	JTextArea txtArea;
	JButton btn;
	String sendString;
	ClientUI clientUI;
	UDPClient client1;
	
	public UDPClient2(JTextArea textArea, JButton button, ClientUI ui, UDPClient udpClient)
	{
		super();
		txtArea = textArea;
		btn = button;
		clientUI = ui;
		client1 = udpClient;
		sendString = "";
	}
	
	
	//metody
	public void send(String s)
	{
		sendString = s;
	}
	
	public void run()
	{
		try
		{
			txtArea.append("Welcome to eGoat!\n\n");
			btn.setEnabled(false);
			InetAddress serverAddress = InetAddress.getByName("localhost");
	
	        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
	        
	        
	    	// allow client to choose directory
	        JFileChooser f = new JFileChooser("C:/Users/anton/Desktop/");
	        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
	        f.showSaveDialog(null);
	        // show chosen directory in console
	        System.out.println(f.getSelectedFile());
	        txtArea.append(f.getSelectedFile().toString() + "\n");
	        
	        // save all filenames from the chosen directory
	        File folder = new File(f.getSelectedFile().toString());
	        listOfFiles = folder.listFiles();
	        Files_sha = new ArrayList<File_sha>();
	        
	        String message = "#start#";
	        
	        byte[] stringContents = message.getBytes("utf8"); 
	        
	        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
	        sentPacket.setAddress(serverAddress);
	        sentPacket.setPort(Config.PORT);
	        socket.send(sentPacket);
	        
	        for (int i = 0; i < listOfFiles.length; i++) {
	          if (listOfFiles[i].isFile()) {
	            System.out.println("File " + listOfFiles[i].getName());
	            txtArea.append("File " + listOfFiles[i].getName() + "\n");
	            //calculate the checksum using external lib hash
	            String st = new String(Snippet.hashFile(listOfFiles[i]));
	            System.out.println("SHA512 : " + st);
	            txtArea.append("SHA512 : " + st + "\n");
	            //send the list of the files to server 
	            message = st;
	            Files_sha.add(new File_sha(listOfFiles[i],st));
	            
	            stringContents = message.getBytes("utf8"); 
	            
	            sentPacket = new DatagramPacket(stringContents, stringContents.length);
	            sentPacket.setAddress(serverAddress);
	            sentPacket.setPort(Config.PORT);
	            socket.send(sentPacket);
	          }
	        }
	        
	        client1.sendFilesSHA(Files_sha);
			
	        message = "#end#";
	        stringContents = message.getBytes("utf8"); 
	        
	        sentPacket = new DatagramPacket(stringContents, stringContents.length);
	        sentPacket.setAddress(serverAddress);
	        sentPacket.setPort(Config.PORT);
	        socket.send(sentPacket);
	        
	        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	        socket.receive(recievePacket);
	        int length = recievePacket.getLength();
	    	message = new String(recievePacket.getData(), 0, length, "utf8");
	    	System.out.print(message);
	    	txtArea.append("\n" + message + "\n");
	        
	        // receive the list of the checksums
	        while(true)
	        {
		        recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
		        socket.receive(recievePacket);
		        length = recievePacket.getLength();
		    	message = new String(recievePacket.getData(), 0, length, "utf8");
		    	if(message.equals("#CSumEnd#"))
		    		break;
		    	else
		    	{
		    		System.out.print(message);
		    		txtArea.append(message);
		    	}
	        }
	    	
	        btn.setEnabled(true);
	        clientUI.check();
	        btn.setEnabled(false);
	    	// choose the required checksum
//	        Scanner sc = new Scanner(System.in);
//	        String checkSum = sc.nextLine();
	        String checkSum = sendString;
	        txtArea.append("Selected checksum: " + checkSum + "\n");
	        stringContents = checkSum.getBytes("utf8"); 
	        sentPacket = new DatagramPacket(stringContents, stringContents.length);
	        sentPacket.setAddress(serverAddress);
	        sentPacket.setPort(Config.PORT);
	        socket.send(sentPacket);
	        
	        
	        recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	        socket.receive(recievePacket);
	        length = recievePacket.getLength();
	        message = new String(recievePacket.getData(), 0, length, "utf8");
	        System.out.print(message); 
	        txtArea.append(message + "\n");
	        
	        if (message.equals("There is no file with such a checksum \n"))
	        {
	        	System.out.println("Try again ...");
	        	txtArea.append("Try again ...\n");
	        } 
	        else
	        {
	            // send the required checksum to the selected ip and port 
	            // in order to download it
	        	System.out.println("Enter the IP address of the user from whom you want to get the selected file: "); 
	        	txtArea.append("Enter the IP address of the user from whom you want to get the selected file: ");
	        	
	        	btn.setEnabled(true);
	        	clientUI.check();
	        	btn.setEnabled(false);
//	        	sc = new Scanner(System.in);
//	        	String ip = sc.nextLine();
	        	String ip = sendString;
	        	txtArea.append(ip + "\n");
	        	serverAddress = InetAddress.getByName(ip);
	            System.out.println("Enter the checksum of the file you want to get: ");
	            txtArea.append("Enter the checksum of the file you want to get: ");
	            
	            btn.setEnabled(true);
	            clientUI.check();
	            btn.setEnabled(false);
//	            stringContents = sc.nextLine().getBytes("utf8");
	            stringContents = sendString.getBytes("utf8");
	            txtArea.append(sendString + "\n");
	            sentPacket = new DatagramPacket(stringContents, stringContents.length);
	            sentPacket.setAddress(serverAddress);
	            sentPacket.setPort(Integer.valueOf(SOCKET_PORT));
	            socket.send(sentPacket);
	            
	            int bytesRead; 
	            int current = 0; 
	            FileOutputStream fos = null; 
	            BufferedOutputStream bos = null; 
	            Socket sock = null; 
	            try { 
	              sock = new Socket(SERVER, SOCKET_PORT); 
	              System.out.println("Connecting...");
	              txtArea.append("Connecting...\n");
	         
	              // receive file 
	              byte [] mybytearray  = new byte [FILE_SIZE]; 
	              InputStream is = sock.getInputStream(); 
	              fos = new FileOutputStream(FILE_TO_RECEIVED); 
	              bos = new BufferedOutputStream(fos); 
	              bytesRead = is.read(mybytearray,0,mybytearray.length); 
	              current = bytesRead; 
	         
	              do { 
	                 bytesRead = 
	                    is.read(mybytearray, current, (mybytearray.length-current)); 
	                 if(bytesRead >= 0) current += bytesRead; 
	              } while(bytesRead > -1); 
	         
	              bos.write(mybytearray, 0 , current); 
	              bos.flush(); 
	              System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");
	              txtArea.append("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)\n");
	            } 
	            finally { 
	              if (fos != null) fos.close(); 
	              if (bos != null) bos.close(); 
	              if (sock != null) sock.close(); 
	            } 
	         }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Goodbye!");
		txtArea.append("Goodbye!\n");
	}

}
