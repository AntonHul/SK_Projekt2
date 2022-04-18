import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import javax.swing.JFileChooser;


public class UDPClient {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
 
    	// allow client to choose directory
        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
        f.showSaveDialog(null);
        // show chosen directory in console
        System.out.println(f.getSelectedFile());
        
        // save all filenames from the chosen directory
        File folder = new File(f.getSelectedFile().toString());
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            System.out.println("File " + listOfFiles[i].getName());
            //calculate the checksum using external lib hash
            System.out.println("SHA512 : " + Snippet.hashFile(listOfFiles[i]));
          }
        }
       
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