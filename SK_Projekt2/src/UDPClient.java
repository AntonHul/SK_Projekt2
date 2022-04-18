import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPClient {

    public static void main(String[] args) throws IOException {
 
        String message = "tekst";
        InetAddress serverAddress = InetAddress.getByName("localhost");
        System.out.println(serverAddress);

        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
        byte[] stringContents = message.getBytes("utf8"); //Pobranie strumienia bajtów z wiadomosci

        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);

        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.setSoTimeout(1010);

        try{
            socket.receive(recievePacket);
            System.out.println("Serwer otrzyma³ wiadomoœæ");
        }catch (SocketTimeoutException ste){
            System.out.println("Serwer nie odpowiedzia³, wiêc albo dosta³ wiadomoœæ albo nie...");
        }
    }
}