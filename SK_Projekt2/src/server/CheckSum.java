package server;

import java.util.ArrayList;

public class CheckSum {
	
	/*
	public CheckSum(String sum) {
		this.sum = sum;	
	}
	*/
	
	public CheckSum(String sum, String ip) {
		this.sum = sum;
		ips.add(ip);	
	}
	
	/*
	public CheckSum(String sum, String ip, String port) {
		this.sum = sum;
		ips.add(ip);
		ports.add(port);	
	}
	*/
	
	String sum;
	ArrayList<String> ips = new ArrayList<String>();
//	ArrayList<String> ports = new ArrayList<String>();
	
	boolean compareIPs (String ip)
	{
		boolean exist = false;
		
		for(int i = 0; i < ips.size(); i++)
		{
			if(ips.get(i).equals(ip))
			{
				exist = true;
				break;
			}
		}
		
		return exist;
	}
}
