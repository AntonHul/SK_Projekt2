package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Snippet {
	public static String hashFile(File file)
	        throws NoSuchAlgorithmException, FileNotFoundException, IOException {
	    // Set your algorithm
	    // "MD2","MD5","SHA","SHA-1","SHA-256","SHA-384","SHA-512"
	    MessageDigest md = MessageDigest.getInstance("SHA-512");
	    FileInputStream fis = new FileInputStream(file);
	    byte[] dataBytes = new byte[1024];
	
	    int nread = 0;
	    while ((nread = fis.read(dataBytes)) != -1) {
	        md.update(dataBytes, 0, nread);
	    }
	
	    byte[] mdbytes = md.digest();
	
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < mdbytes.length; i++) {
	        sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
}