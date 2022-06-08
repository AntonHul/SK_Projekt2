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
	
	boolean compareIPs (String ip)//returns true if this ip has this CS
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
	
	int findIP (String ip)//returns where ip is stored in ArryList, if this ip is not stored returns -1
	{
		int index = -1;
		
		for(int i = 0; i < ips.size(); i++)
		{
			if(ips.get(i).equals(ip))
			{
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	boolean removeIP (String ip)//removes ip from ArrayList
	{
		boolean removed = false;
		
		for(int i = 0; i < ips.size(); i++)
		{
			if(ips.get(i).equals(ip))
			{
				ips.remove(i);
				removed = true;
				break;
			}
		}
		
		return removed;
	}
}
