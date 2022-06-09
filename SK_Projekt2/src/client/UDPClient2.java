package client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import config.Config;

public class UDPClient2 extends Thread
{
	public static File[] listOfFiles;
	public static ArrayList<File_sha> Files_sha;
	 public final static int SOCKET_PORT = 13267;
	  public final static String SERVER = "127.0.0.1";
	  public final static String FILE_TO_RECEIVED =  "client/received_file.txt";
	  public final static int FILE_SIZE = 6022386;
	  static byte[] stringContents;
	
	JTextArea txtArea;
	JButton btn;
	String sendString;
	ClientUI clientUI;
	UDPClient client1;
	boolean clientRunning;
	boolean wait;
	
	public UDPClient2(JTextArea textArea, JButton button, ClientUI ui, UDPClient udpClient)
	{
		super();
		txtArea = textArea;
		btn = button;
		clientUI = ui;
		client1 = udpClient;
		sendString = "";
		clientRunning = true;
		wait = false;
	}
	
	
	//metody
	public void send(String s)
	{
		sendString = s;
		wait = false;
	}
	
	public void close()
	{
		clientRunning = false;
		wait = false;
	}
	
	private void dispMessage(String s)
	{
		System.out.print(s);
        txtArea.append(s);
        txtArea.setCaretPosition(txtArea.getDocument().getLength());
	}
	
	public void run()
	{
		try
		{
			dispMessage("Welcome to eGoat!\n\n");
			btn.setEnabled(false);
			InetAddress serverAddress = InetAddress.getByName("localhost");
	
	        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
	        
	        
	    	// allow client to choose directory
	        JFileChooser f = new JFileChooser("C:/Users/anton/Desktop/");
	        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
	        
	        // show chosen directory in console
	        String message;
	        byte[] stringContents;
	        DatagramPacket sentPacket;
	         
	        if (f.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	        {
	        	dispMessage(f.getSelectedFile().toString());
	
		        // save all filenames from the chosen directory
		        File folder = new File(f.getSelectedFile().toString());
		        listOfFiles = folder.listFiles();
		        Files_sha = new ArrayList<File_sha>();
		        
		        message = "#start#";
		        
		        stringContents = message.getBytes("utf8"); 
		        
		        sentPacket = new DatagramPacket(stringContents, stringContents.length);
		        sentPacket.setAddress(serverAddress);
		        sentPacket.setPort(Config.PORT);
		        socket.send(sentPacket);
		        
		        for (int i = 0; i < listOfFiles.length; i++)
		        {
					if (listOfFiles[i].isFile())
					{
						dispMessage("File " + listOfFiles[i].getName() + "\n");
						//calculate the checksum using external lib hash
						String st = new String(Snippet.hashFile(listOfFiles[i]));
						dispMessage("SHA512 : " + st + "\n");
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
		        
	        }
	        else 
	        {
	        	message = "#start#"; 
		        stringContents = message.getBytes("utf8"); 
		        sentPacket = new DatagramPacket(stringContents, stringContents.length);
		        sentPacket.setAddress(serverAddress);
		        sentPacket.setPort(Config.PORT);
		        socket.send(sentPacket);
	        } 
	     
		    message = "#end#";
		    stringContents = message.getBytes("utf8"); 
	        sentPacket = new DatagramPacket(stringContents, stringContents.length);
	        sentPacket.setAddress(serverAddress);
	        sentPacket.setPort(Config.PORT);
	        socket.send(sentPacket);
	        
	        dispMessage("Files send to server!");
	        
	        while(clientRunning)
	        {
	        	try
	        	{
			        dispMessage("To download a file type \"Download\"\n");
			        dispMessage("To close a client type \"Close\"\n");
			        while(clientRunning)
			        {
			        	btn.setEnabled(true);
			        	wait = true;
				        while(wait)
				        {
				        	this.sleep(10);
				        	if(!wait)
				        		break;
				        }
				        btn.setEnabled(false);
				        if(!clientRunning)
				        	break;
				        
				        if(sendString.equals("Download") || sendString.equals("download"))
				        	break;
				        else
				        {
				        	dispMessage("Unknown command!\n");
				        }
			        }
			        if(!clientRunning)
			        	break;
			        
			        message = "#send#";
				    stringContents = message.getBytes("utf8"); 
			        sentPacket = new DatagramPacket(stringContents, stringContents.length);
			        sentPacket.setAddress(serverAddress);
			        sentPacket.setPort(Config.PORT);
			        socket.send(sentPacket);
			        
			        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
			        socket.receive(recievePacket);
			        int length = recievePacket.getLength();
			    	message = new String(recievePacket.getData(), 0, length, "utf8");
			    	dispMessage("\n" + message + "\n");
			        
			        // receive the list of the checksums
			        while(clientRunning)
			        {
				        recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
				        socket.receive(recievePacket);
				        length = recievePacket.getLength();
				    	message = new String(recievePacket.getData(), 0, length, "utf8");
				    	if(message.equals("#CSumEnd#"))
				    		break;
				    	else
				    	{
				    		dispMessage(message);
				    	}
			        }
			        if(!clientRunning)
			        	break;
			    	
			        btn.setEnabled(true);
			        wait = true;
			        while(wait)
			        {
			        	this.sleep(10);
			        	if(!wait)
			        		break;
			        }
			        btn.setEnabled(false);
			    	// choose the required checksum
		
			        String checkSum = sendString;
			        dispMessage("Selected checksum: " + checkSum + "\n");
				
				message = "#sendIP#";
				    stringContents = message.getBytes("utf8"); 
			        sentPacket = new DatagramPacket(stringContents, stringContents.length);
			        sentPacket.setAddress(serverAddress);
			        sentPacket.setPort(Config.PORT);
			        socket.send(sentPacket);
				
			        stringContents = checkSum.getBytes("utf8"); 
			        sentPacket = new DatagramPacket(stringContents, stringContents.length);
			        sentPacket.setAddress(serverAddress);
			        sentPacket.setPort(Config.PORT);
			        socket.send(sentPacket);
			        
			        
			        recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
			        socket.receive(recievePacket);
			        length = recievePacket.getLength();
			        message = new String(recievePacket.getData(), 0, length, "utf8");
			        dispMessage(message + "\n");
			        
			        if (message.equals("There is no file with such a checksum \n"))
			        {
			        	dispMessage("Try again ...\n");
			        } 
			        else
			        {
			            // send the required checksum to the selected ip and port 
			            // in order to download it
			        	dispMessage("Enter the IP address of the user from whom you want to get the selected file: ");
			        	
			        	btn.setEnabled(true);
			        	wait = true;
				        while(wait)
				        {
				        	this.sleep(10);
				        	if(!wait)
				        		break;
				        }
			        	btn.setEnabled(false);
			        	String ip = sendString;
			        	dispMessage(ip + "\n");
			        	serverAddress = InetAddress.getByName(ip);
			        	sleep(10);
			            stringContents = checkSum.getBytes("utf8");
			            sentPacket = new DatagramPacket(stringContents, stringContents.length);
			            sentPacket.setAddress(serverAddress);
			            sentPacket.setPort(Integer.valueOf(SOCKET_PORT));
			            socket.send(sentPacket);
			            
			            int bytesRead; 
			            int current = 0; 
			            FileOutputStream fos = null; 
			            BufferedOutputStream bos = null; 
			            Socket sock = null;
			            while(clientRunning)
			            {
				            try { 
				              sock = new Socket(SERVER, SOCKET_PORT); 
				              dispMessage("Connecting...\n");
				         
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
				              dispMessage("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)\n");
				              
				              String sum_check = new String(Snippet.hashFile(new File(FILE_TO_RECEIVED)));
				              if (sum_check.equals(checkSum))JOptionPane.showMessageDialog(null, "Received file is correct!");
				              else JOptionPane.showMessageDialog(null, "Received file is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
				              
				              if (is != null) is.close();
				              break;
				            }
				            catch (IOException ex)
				        	{}
				            finally
				            {
				              if (fos != null) fos.close(); 
				              if (bos != null) bos.close(); 
				              if (sock != null) sock.close(); 
				            } 
			            }
			         }
				}
	        	catch (IOException ex)
	        	{
					JOptionPane.showMessageDialog(null, "The server is unavailable. Try later",  "Error", JOptionPane.ERROR_MESSAGE);
					System.out.println("Error IO 2");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	        }
		}
		catch (IOException ex)
    	{
			JOptionPane.showMessageDialog(null, "The server is unavailable. Try later",  "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Error IO 1");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		dispMessage("Goodbye!\n");
	}
	
}
