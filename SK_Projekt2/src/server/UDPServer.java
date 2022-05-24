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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import config.Config;

public class UDPServer extends Thread
{
	DatagramSocket datagramSocket;
	
	String serverSumDir = "server/eGoat/sum";//plik do przechowywania sum i ip
	ArrayList<CheckSum> sums = new ArrayList<CheckSum>();//lista sum
	boolean exist = false;
	public boolean serverRunning = true;
	
	

    public static void main(String[] args) throws Exception{
    	String serverSumDir = "server/eGoat/sum";//plik do przechowywania sum i ip
    	
    	//Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
        //creat list of checksums
        ArrayList<CheckSum> sums = new ArrayList<CheckSum>();
    	
    	//sprawdzenie czy istnieje plik do zapisu sum
    	File sumDir = new File(serverSumDir);
    	
    	if(!(sumDir.exists()) || !(sumDir.isDirectory()))
    	{
    		System.out.println("Missing directory \"" + serverSumDir + "\"!\nCreating a new one!");
    		boolean test = sumDir.mkdirs();
    		if(test)
    			System.out.println("Created!");
    		else
    			System.out.println("Not created!");
    	}
    	else
    	{
    		System.out.println(serverSumDir + " - OK");
    		File[] sumFilesList = sumDir.listFiles();
    		
    		for(File sumFile : sumFilesList)//zczytanie informacji z plikow sum do listy sum
    		{
    			try
    			{
    				BufferedReader sc = new BufferedReader(new FileReader(sumFile));
    				String scc = sc.readLine();
    				while(scc != null)
    				{
    					String[] ip_port = scc.split("\t");
    					String fip = ip_port[0];
    					String fport = ip_port[1];
    					sums.add(new CheckSum(sumFile.getName(), fip, fport));
    					scc = sc.readLine();
    				}
    				sc.close();	
    			}
    			catch(IOException e)
    			{
    				e.printStackTrace();
    			}
    		}
    	}
        
        while (true) {
        	DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        	datagramSocket.receive(receivedPacket);
        	int length = receivedPacket.getLength();
        	String message = new String(receivedPacket.getData(), 0, length, "utf8");
        	String[] sum_list = message.split(" ");
        	// Port i host ktory wyslal nam zapytanie
        	InetAddress address = receivedPacket.getAddress();
        	int port = receivedPacket.getPort();

        	// check if the checksum has already appeared
        	for (String st: sum_list){ 
        		boolean exist = false;
        		for (CheckSum compare: sums){
        			if (compare.sum.equals(st)){
        				if (compare.compareIPs(address.toString())){
        					compare.ips.add(address.toString());
        					compare.ports.add(Integer.toString(port));	
        					
        					FileWriter fw = new FileWriter((serverSumDir + "/" + st),true);
        					fw.write(address.toString());
        					fw.write('\t');
        					fw.write(Integer.toString(port));
        					fw.write('\n');
        					fw.close();
        				}
        				exist = true;	
        				break;
        			}
        		}
        		if(!exist){
        			CheckSum newsum = new CheckSum(st, address.toString(),Integer.toString(port));
        			sums.add(newsum);
        			
        			//tworzenie nowego pliku sumy
        			String filePath = (serverSumDir + "/" + st);
        			File file = new File(filePath);
        			file.createNewFile();
        			
        			FileWriter fw = new FileWriter(file);
        			fw.write(address.toString());
        			fw.write('\t');
        			fw.write(Integer.toString(port));
        			fw.write('\n');
        			fw.close();
        			
        			exist = false;
    			}
        	}
        	

        	// confirm receipt of the data
        	String all_files = new String("All available files: \n");
        	for (CheckSum file: sums) {
        		all_files += file.sum + '\n';
        	}
        	all_files += "Send checksum of the required file \n";
        	
        	byte[] byteResponse = all_files.getBytes("utf8");
        	DatagramPacket response
                = new DatagramPacket(
                    byteResponse, byteResponse.length, address, port);
        	datagramSocket.send(response);

        	
        	
        	// receive the checksum of a particular file
        	receivedPacket = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        	datagramSocket.receive(receivedPacket);
        	length = receivedPacket.getLength();
        	message = new String(receivedPacket.getData(), 0, length, "utf8");

        	// check if such checksum exists
        	boolean check_sum = false;
        	for (CheckSum compare: sums) {
        		if (compare.sum.equals(message)) {
        			message = "The following clients have the selected file: \n";
        			check_sum = true;
        			for (int i = 0; i < compare.ports.size(); i++) {
        				message += compare.ips.get(i) + '\t' + compare.ports.get(i) + '\n';
        		}
    			byteResponse = message.getBytes("utf8");
    			response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
    			datagramSocket.send(response);
        		}
            }
        	if (!check_sum) {
        		byteResponse = "There is no file with such a checksum \n".getBytes("utf8");
				response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
				datagramSocket.send(response);
        	}
        }
    }
}