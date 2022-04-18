package server;

import java.util.ArrayList;

public class CheckSum {
	
	public CheckSum(String sum, String ip, String port) {
		this.sum = sum;
		ips.add(ip);
		ports.add(port);	
	}
	
	String sum;
	ArrayList<String> ips = new ArrayList<String>();
	ArrayList<String> ports = new ArrayList<String>();
}
