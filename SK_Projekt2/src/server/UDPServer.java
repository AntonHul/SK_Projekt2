package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import config.Config;

public class UDPServer implements Runnable
{
	DatagramSocket datagramSocket;
	
	String serverSumDir = "server/eGoat/sum";//plik do przechowywania sum i ip
	ArrayList<CheckSum> sums = new ArrayList<CheckSum>();//lista sum
	boolean exist = false;
	boolean serverRunning = false;
	
	//konstruktor
	public UDPServer()
	{
		checkFileSystem(serverSumDir);
		System.out.println("Server initiated!");
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
    		boolean created = sumDir.mkdirs();
    		
    		if(created)
    		{
    			System.out.println("Created!");
    		}
    		else
    		{
    			System.out.println("Not created!");
    			ok = false;
    		}
    	}
    	else
    	{
    		System.out.println(path + " - OK");
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
			
			while(serverRunning)
			{
	        	DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	        	datagramSocket.setSoTimeout(1000);
	        	
	        	try
	        	{
	        		datagramSocket.receive(receivedPacket);
	        		int length = receivedPacket.getLength();
		        	String message = new String(receivedPacket.getData(), 0, length, "utf8");
		        	// Port i host ktory wyslal nam zapytanie
		        	InetAddress address = receivedPacket.getAddress();
		        	int port = receivedPacket.getPort();
		        	
		        	System.out.print(address.toString());
	        		System.out.println(" Connected!");
	        		
		        	if(message.equals("#start#"))
		        	{
		        		try
		        		{
		        			datagramSocket.setSoTimeout(10000);
			        		while(true)
			        		{
				        		datagramSocket.receive(receivedPacket);
				        		length = receivedPacket.getLength();
				        		message = new String(receivedPacket.getData(), 0, length, "utf8");
				        		// Port i host ktory wyslal nam zapytanie
				        		address = receivedPacket.getAddress();
				        		port = receivedPacket.getPort();
				        		
				        		if(message.equals("#end#"))
				        			break;
				        		
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
				        	}
		        		}
		        		catch(SocketTimeoutException e){}
		        	}
		        	
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
	        		
	        		
	        		while(serverRunning)
	        		{
	        			try
	        			{
			        		// receive the checksum of a particular file
			        		receivedPacket = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
			        		datagramSocket.receive(receivedPacket);
			        		length = receivedPacket.getLength();
			        		message = new String(receivedPacket.getData(), 0, length, "utf8");
			        		
			        		// check if such checksum exists
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
				    			byteResponse = message.getBytes("utf8");
				    			response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
				    			datagramSocket.send(response);
				        		}
				            }
				        	if (!check_sum)
				        	{
				        		byteResponse = "There is no file with such a checksum \n".getBytes("utf8");
								response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
								datagramSocket.send(response);
				        	}
	        			}
	        			catch(SocketTimeoutException e){}
	        		}
		        }
		        catch(SocketTimeoutException e){}
			}
			
			datagramSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Server closed!");
	}
}
