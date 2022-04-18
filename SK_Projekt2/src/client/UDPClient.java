package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import javax.swing.JFileChooser;
import config.Config;


public class UDPClient{

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
 
    	
        InetAddress serverAddress = InetAddress.getByName("localhost");

        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda


    	
    
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
            //send the list of the files to server 
            String message = Snippet.hashFile(listOfFiles[i]);
            byte[] stringContents = message.getBytes("utf8"); 

            DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
            sentPacket.setAddress(serverAddress);
            sentPacket.setPort(Config.PORT);
            socket.send(sentPacket);
          }
        }
        

        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.setSoTimeout(1010);

        try{
            socket.receive(recievePacket);
            System.out.println("Serwer otrzyma� wiadomo��");
        }catch (SocketTimeoutException ste){
            System.out.println("Serwer nie odpowiedzia�, wi�c albo dosta� wiadomo�� albo nie...");
        }
        
        
    }
}