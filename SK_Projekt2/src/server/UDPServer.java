package server;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import config.Config;

public class UDPServer implements Runnable 
{
	String message = "#";
	DatagramSocket datagramSocket;
	JTextArea txtArea;
	String serverSumDir = "server/eGoat/sum";//plik do przechowywania sum i ip
	ArrayList<CheckSum> sums = new ArrayList<CheckSum>();//lista sum
	boolean exist = false;
	boolean serverRunning = false;
	
	//konstruktor
	public UDPServer(JTextArea textArea)
	{
		txtArea = textArea;
		checkFileSystem(serverSumDir);
		System.out.println("Server initiated!");
		txtArea.append("Server initiated!\n");
	}
	
	//metody
	public void start()
	{
		serverRunning = true;
	}
	
	public void stop()
	{
		serverRunning = false;
	}
	
	boolean checkFileSystem(String path)//sprawdzenie czy istnieje plik do zapisu sum i ewentualnie wczytanie danych
	{
		File sumDir = new File(path);
		boolean ok = true;
    	
    	if(!(sumDir.exists()) || !(sumDir.isDirectory()))
    	{
    		System.out.println("Missing directory \"" + path + "\"!\nCreating a new one!");
    		txtArea.append("Missing directory \"" + path + "\"!\nCreating a new one!\n");
    		boolean created = sumDir.mkdirs();
    		
    		if(created)
    		{
    			System.out.println("Created!");
    			txtArea.append("Created!\n");
    		}
    		else
    		{
    			System.out.println("Not created!");
    			txtArea.append("Not created!\n");
    			ok = false;
    		}
    	}
    	else
    	{
    		System.out.println(path + " - OK");
    		txtArea.append(path + " - OK\n");
    		File[] sumFilesList = sumDir.listFiles();
    		
    		for(File sumFile : sumFilesList)//zczytanie informacji z plikow sum do listy sum
    		{
    			try
    			{
    				Scanner sc = new Scanner(sumFile);
    				while(sc.hasNextLine())
    				{
    					sums.add(new CheckSum(sumFile.getName(), sc.nextLine()));
    				}
    				sc.close();	
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    				ok = false;
    			}
    		}
    	}
		
		return ok;
	}
	
	public void run()
	{
		try
		{
			
			datagramSocket = new DatagramSocket(Config.PORT);
			System.out.println("Server is running!");
			txtArea.append("Server is running!\n");
			
			while(serverRunning)
			{
	        	DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	        	datagramSocket.setSoTimeout(1000);
	        	
	        	try
	        	{
	        		datagramSocket.receive(receivedPacket);
	        		int length = receivedPacket.getLength();
	        		if(!message.equals("#start#") || message.equals("#") )
		        	message = new String(receivedPacket.getData(), 0, length, "utf8");
		        	
		        	// Port i host ktory wyslal nam zapytanie
		        	InetAddress address = receivedPacket.getAddress();
		        	int port = receivedPacket.getPort();
		        	
		        	System.out.print(address.toString());
	        		System.out.println(" Connected!");
	        		txtArea.append(address.toString() + " Connected!\n");
	        		System.out.println(message);
	        		txtArea.append(address.toString() + ": " + message + "\n");
		        	if(message.equals("#start#"))
		        	{
		        		ArrayList<String> cs = new ArrayList();
		        		
		        		try
		        		{
			        		while(serverRunning)
			        		{
				        		datagramSocket.receive(receivedPacket);
				        		length = receivedPacket.getLength();
				        		message = new String(receivedPacket.getData(), 0, length, "utf8");
				        		// Port i host ktory wyslal nam zapytanie
				        		address = receivedPacket.getAddress();
				        		port = receivedPacket.getPort();
				        		
				        		if(message.equals("#end#"))
				        		{
				        			break;
				        		}
				        		// check if the checksum has already appeared
			        			boolean exist = false;
			        			for (CheckSum compare: sums)
			        			{
			        				if (compare.sum.equals(message))
			        				{
			        					if (!compare.compareIPs(address.toString()))
			        					{
			        						compare.ips.add(address.toString());	
			        						
			        						FileWriter fw = new FileWriter((serverSumDir + "/" + message),true);
			        						fw.write(address.toString());
			        						fw.write('\n');
			        						fw.close();
			        					}
			        					exist = true;	
			        					break;
			        				}
			        			}
			        			if(!exist)
			        			{
			        				CheckSum newsum = new CheckSum(message, address.toString());
			        				sums.add(newsum);
			        				
			        				//tworzenie nowego pliku sumy
			        				String filePath = (serverSumDir + "/" + message);
			        				File file = new File(filePath);
			        				file.createNewFile();
			        				
			        				FileWriter fw = new FileWriter(file);
			        				fw.write(address.toString());
			        				fw.write('\n');
			        				fw.close();
			        				
			        				exist = false;
			        			}
			        			
			        			cs.add(message);
				        	}
		        		}
		        		catch(SocketTimeoutException e){}
		        		
		        		for(CheckSum sumy: sums)
		        		{
		        			boolean cont = false;
		        			
		        			for(int i = 0; i < cs.size(); i++)
		        			{
		        				if(sumy.sum.equals(cs.get(i)))
		        				{
		        					cont = true;
		        					cs.remove(i);
		        					break;
		        				}
		        			}
		        			if(cont)
		        				continue;
		        			
		        			//usuwanie ip z sum ktorych dany adress ip juz nie posiada
		        			sumy.removeIP(address.toString());
		        			
		        			String filePath = (serverSumDir + "/" + sumy.sum);
		        			File inputFile = new File(filePath);
		        			File tempFile = new File(serverSumDir + "/tempFile.txt");

		        			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		        			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		        			String lineToRemove = address.toString();
		        			String currentLine;

		        			while((currentLine = reader.readLine()) != null)
		        			{
			        			String trimmedLine = currentLine.trim();
			        			if(trimmedLine.equals(lineToRemove))
			        				continue;
			        			writer.write(currentLine + '\n');
		        			}
		        			writer.close(); 
		        			reader.close(); 
		        			boolean successful = tempFile.renameTo(inputFile);
		        		}
		        	}
		        	else if(message.equals("#send#"))
		        	{
		        		datagramSocket.setSoTimeout(1000);
			        	
		        		String all_files = new String("All available files: \n");
		        		byte[] byteResponse = all_files.getBytes("utf8");
		        		DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
		        		datagramSocket.send(response);
		        		
		        		for (CheckSum file: sums)
		        		{
		        			all_files = file.sum + '\n';
		        			byteResponse = all_files.getBytes("utf8");
		        			response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
			        		datagramSocket.send(response);
		        		}
		        		
		        		all_files = "Send checksum of the required file \n\n";
		        		byteResponse = all_files.getBytes("utf8");
		        		response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
		        		datagramSocket.send(response);
		        		
		        		all_files = "#CSumEnd#";
		        		byteResponse = all_files.getBytes("utf8");
		        		response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
		        		datagramSocket.send(response);
		        	}
				else if(message.equals("#sendIP#"))
				{
					try
		        		{
				        	// receive the checksum of a particular file
				        	receivedPacket = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
				        	datagramSocket.receive(receivedPacket);
				        	length = receivedPacket.getLength();
				        	message = new String(receivedPacket.getData(), 0, length, "utf8");
				        	txtArea.append(address.toString() + ": " + message + "\n");
				        	boolean check_sum = false;
				        	for (CheckSum compare: sums)
				        	{
				        		if (compare.sum.equals(message))
				        		{
					    			message = "The following clients have the selected file: \n";
					    			check_sum = true;
					    			for (int i = 0; i < compare.ips.size(); i++)
					    			{
					    				message += compare.ips.get(i) + '\n';
					    			}
					    			byte[] byteResponse = message.getBytes("utf8");
					    			DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
					    			datagramSocket.send(response);
					       		}
					        }
					       	if (!check_sum)
					       	{
					       		byte[] byteResponse = "There is no file with such a checksum \n".getBytes("utf8");
					       		DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
					       		datagramSocket.send(response);
					       	}
		        		}
		        		catch(SocketTimeoutException e){}
				}
		        	else
		        		continue;
		        	
		        	datagramSocket.setSoTimeout(1000);
		        }
		        catch(SocketTimeoutException e){}
			}
			datagramSocket.close();
		}
		catch(BindException e2)
		{
			JOptionPane.showMessageDialog(null, "The server is already running",  "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Server closed!");
		txtArea.append("Server closed!\n");
	}
}
