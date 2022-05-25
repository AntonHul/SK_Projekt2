package server;

import java.util.ArrayList;

public class CheckSum
{
	
	public CheckSum(String sum, String ip)
	{
		this.sum = sum;
		ips.add(ip);	
	}
	
	String sum;
	ArrayList<String> ips = new ArrayList<String>();
	
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
