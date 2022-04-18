import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    public static void main(String[] args) throws Exception{

        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);

        byte[] byteResponse = "OK".getBytes("utf8");

        while (true){

            DatagramPacket receivedPacket
                    = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);

            datagramSocket.receive(receivedPacket);

            int length = receivedPacket.getLength();
            String message =
                    new String(receivedPacket.getData(), 0, length, "utf8");

            // Port i host który wys³a³ nam zapytanie
            InetAddress address = receivedPacket.getAddress();
            int port = receivedPacket.getPort();

            System.out.println(message);
            Thread.sleep(1000); //To oczekiwanie nie jest potrzebne dla
            // obs³ugi gniazda

            DatagramPacket response
                    = new DatagramPacket(
                        byteResponse, byteResponse.length, address, port);

            datagramSocket.send(response);
        }
    }
}