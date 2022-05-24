package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;


import config.Config;
//import server.CheckSum;//nie wiem czemu to znalazlo sie w kiencie, ale kient nie moze z tego korzystac, bo to jest klasa serwera


public class UDPClient extends Thread {
	public static File[] listOfFiles;
	public static ArrayList<File_sha> Files_sha;
	 public final static int SOCKET_PORT = 13267;      // you may change this 
	  public final static String SERVER = "127.0.0.1";
	  public final static String FILE_TO_RECEIVED =  "C:/Users/anton/Desktop/test2.txt";
	  public final static int FILE_SIZE = 6022386;
	  static byte[] stringContents;
	  
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    	UDPClient thread = new UDPClient();
        thread.start();

        
        InetAddress serverAddress = InetAddress.getByName("localhost");

        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
        
        
    	// allow client to choose directory
        JFileChooser f = new JFileChooser("C:/Users/anton/Desktop/");
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showSaveDialog(null);
        // show chosen directory in console
        System.out.println(f.getSelectedFile());
        
        // save all filenames from the chosen directory
        File folder = new File(f.getSelectedFile().toString());
        listOfFiles = folder.listFiles();
        Files_sha = new ArrayList<File_sha>();
        String message = "";
        
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            System.out.println("File " + listOfFiles[i].getName());
            //calculate the checksum using external lib hash
            String st = new String(Snippet.hashFile(listOfFiles[i]));
            System.out.println("SHA512 : " + st);
            //send the list of the files to server 
            message += st + " ";
            Files_sha.add(new File_sha(listOfFiles[i],st));
          }
        }
		
        
        byte[] stringContents = message.getBytes("utf8"); 
        
        
        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);
        
        // receive the list of the checksums
        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.receive(recievePacket);
        int length = recievePacket.getLength();
    	message = new String(recievePacket.getData(), 0, length, "utf8");
    	System.out.print(message); 
    	
    	// choose the required checksum
        Scanner sc = new Scanner(System.in);
        String checkSum = sc.nextLine();
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
        
        if (message.equals("There is no file with such a checksum \n")){
        	System.out.println("Try again ..."); 
        } else {
            // send the required checksum to the selected ip and port 
            // in order to download it
        	System.out.println("Enter the IP address of the user from whom you want to get the selected file: "); 
        	sc = new Scanner(System.in);
        	String ip = sc.nextLine();
        	serverAddress = InetAddress.getByName(ip);
            System.out.println("Enter the checksum of the file you want to get: "); 
            stringContents = sc.nextLine().getBytes("utf8"); 
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
              System.out.println("File " + FILE_TO_RECEIVED 
                  + " downloaded (" + current + " bytes read)"); 
            } 
            finally { 
              if (fos != null) fos.close(); 
              if (bos != null) bos.close(); 
              if (sock != null) sock.close(); 
            } 
         }        
    }
    
    public void run() {
    	while (true) {
		DatagramSocket datagramSocket;
		try {
			datagramSocket = new DatagramSocket(SOCKET_PORT);
			DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	    	datagramSocket.receive(receivedPacket);
	    	int length = receivedPacket.getLength();
	    	String message = new String(receivedPacket.getData(), 0, length, "utf8"); 
	    	datagramSocket.close();

    	//sprawdzamy czy mamy taki plik
    	for (File_sha file_sha: Files_sha) {
    		if (file_sha.sha.equals(message)) {
    			InetAddress address_send = receivedPacket.getAddress();
    			System.out.println("###############################");
    			System.out.println("Get request from: ");
    			System.out.println(address_send); 
    			System.out.println("Sending the file with checksum:");
    			System.out.println(message);
    			System.out.println("###############################");

    			String FILE_TO_SEND = (file_sha.file.getPath());  // you may change this 

    	    FileInputStream fis = null; 
    	    BufferedInputStream bis = null; 
    	    OutputStream os = null; 
    	    ServerSocket servsock = null; 
    	    Socket sock = null; 
    	    try { 
    	      servsock = new ServerSocket(SOCKET_PORT); 
    	      while (true) { 
    	        System.out.println("Waiting..."); 
    	        try { 
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
    	          os.write(mybytearray,0,mybytearray.length); 
    	          os.flush(); 
    	          System.out.println("Done."); 
    	        } 
    	        finally { 
    	          if (bis != null) bis.close(); 
    	          if (os != null) os.close(); 
    	          if (sock!=null) sock.close(); 
    	        } 
    	      } 
    	    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	    finally { 
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
		catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
}
    }
}