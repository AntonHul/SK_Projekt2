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
        CheckSum sum = new CheckSum("0","0","0");
        ArrayList<CheckSum> sums = new ArrayList<CheckSum>();
        sums.add(sum);
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
        			if (compare.sum.equals(st) && exist != true) {
        				compare.ips.add(address.toString());
        				compare.ports.add(Integer.toString(port));	
        				exist = true;	
        			}
        		}
        		if(!exist) {
        			CheckSum newsum = new CheckSum(st, address.toString(),Integer.toString(port));
        			sums.add(newsum);
        			exist = false;
    				}
        		}
            


 
        	// confirm receipt of the data
        	byte[] byteResponse = "Send checksum of the required file \n".getBytes("utf8");
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
        		System.out.print(compare.sum); 
        		if (compare.sum.equals(message)) {
        			for (String ip: compare.ips) {
        			byteResponse = ip.getBytes("utf8");
                	 response = new DatagramPacket(
                            byteResponse, byteResponse.length, address, port);
                	datagramSocket.send(response);
        			}
        		}
            }
        	
        }
    }
}