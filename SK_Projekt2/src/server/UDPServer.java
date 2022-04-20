package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import config.Config;

public class UDPServer {

    public static void main(String[] args) throws Exception{

        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
        //creat list of checksums
        ArrayList<CheckSum> sums = new ArrayList<CheckSum>();
        boolean exist = false;
        
        while (true) {
        	DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        	datagramSocket.receive(receivedPacket);
        	int length = receivedPacket.getLength();
        	String message = new String(receivedPacket.getData(), 0, length, "utf8");
        	String[] sum_list = message.split(" ");
        	// Port i host kt�ry wys�a� nam zapytanie
        	InetAddress address = receivedPacket.getAddress();
        	int port = receivedPacket.getPort();

        	// check if the checksum has already appeared
        	for (String st: sum_list) { 
        		for (CheckSum compare: sums) {
        			if (compare.sum.equals(st)) {
        				compare.ips.add(address.toString());
        				compare.ports.add(Integer.toString(port));	
        				exist = true;	
        				break;
        			}
        		}
        		if(!exist) {
        			CheckSum newsum = new CheckSum(st, address.toString(),Integer.toString(port));
        			sums.add(newsum);
        			exist = false;
    				}
        		}
            

        	// confirm receipt of the data
        	String all_files = new String("All available files:");
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
        	for (CheckSum compare: sums) {
        		if (compare.sum.equals(message)) {
        			byteResponse = "The following clients have the selected file: \n".getBytes("utf8");
               	 	response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
               	 	datagramSocket.send(response);
        			for (String ip: compare.ips) {
        				ip += '\n';
        				byteResponse = ip.getBytes("utf8");
        				response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
        				datagramSocket.send(response);
        			}
        		}
            }
        	
        }
    }
}