package client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;


import config.Config;
//import server.CheckSum;//nie wiem czemu to znalazlo sie w kiencie, ale kient nie moze z tego korzystac, bo to jest klasa serwera


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
        String message = "";
        
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            System.out.println("File " + listOfFiles[i].getName());
            //calculate the checksum using external lib hash
            System.out.println("SHA512 : " + Snippet.hashFile(listOfFiles[i]));
            //send the list of the files to server 
            message += Snippet.hashFile(listOfFiles[i]) + " ";
 
          }
        }

        byte[] stringContents = message.getBytes("utf8"); 

        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);
        
        // receive the list of the checksums
        DatagramPacket recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.receive(recievePacket);
        int length = recievePacket.getLength();
    	message = new String(recievePacket.getData(), 0, length, "utf8");
    	System.out.print(message); 
    	
    	// choose the required checksum
        Scanner sc = new Scanner(System.in);
        String checkSum = sc.nextLine();
        stringContents = checkSum.getBytes("utf8"); 
        sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);
        
        
        recievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.receive(recievePacket);
        length = recievePacket.getLength();
        message = new String(recievePacket.getData(), 0, length, "utf8");
        System.out.print(message); 
        
        if (message.equals("There is no file with such a checksum \n")){
        	System.out.print("Try again ..."); 
        } else {
            // send the required checksum to the selected ip and port 
            // in order to download it
        	System.out.print("Enter the IP address and the port number of the user from whom you want to get the selected file: \n"); 
        	sc = new Scanner(System.in);
        	String ip = sc.nextLine();
        	serverAddress = InetAddress.getByName(ip);
            String port = sc.nextLine();
            System.out.print("Enter the checksum of the file you want to get: \n"); 
            stringContents = sc.nextLine().getBytes("utf8"); 
            sentPacket = new DatagramPacket(stringContents, stringContents.length);
            sentPacket.setAddress(serverAddress);
            sentPacket.setPort(Integer.valueOf(port));
            socket.send(sentPacket);
        }
        
    }
}