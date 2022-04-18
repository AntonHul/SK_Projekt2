import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class UDPServer {

    public static void main(String[] args) throws Exception{

        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
        //creat list of clients with their files
        ArrayList<ArrayList<String>> listOLists = new ArrayList<ArrayList<String>>();
       //add client and its ip
        ArrayList<String> client = new ArrayList<String>();
      
        while (true){
        	DatagramPacket receivedPacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        	datagramSocket.receive(receivedPacket);
        	int length = receivedPacket.getLength();
        	String message = new String(receivedPacket.getData(), 0, length, "utf8");

        	// Port i host który wys³a³ nam zapytanie
        	InetAddress address = receivedPacket.getAddress();
        	int port = receivedPacket.getPort();
        	// check if the user is the same
        	if (client.isEmpty()) {
        		client.add(address.toString());
        		client.add(String.valueOf(port));
            	client.add(message);     
        	} else if (!String.valueOf(port).equals(client.get(0))) {
        		client.add(address.toString());
        		client.add(String.valueOf(port));
	        	client.add(message);
        
	        	// confirm receipt of the data
	        	byte[] byteResponse = "OK".getBytes("utf8");
	        	DatagramPacket response
                    = new DatagramPacket(
                        byteResponse, byteResponse.length, address, port);
        
	        	datagramSocket.send(response);
        	} else {
        	client.add(message);

        	}
        	// confirm receipt of the data
        	byte[] byteResponse = "OK".getBytes("utf8");
        	DatagramPacket response
                = new DatagramPacket(
                    byteResponse, byteResponse.length, address, port);
        	datagramSocket.send(response);
        	
        	
        	listOLists.add(client);
        	
        	 System.out.println(listOLists);
        }
    }
}