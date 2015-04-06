package Front_End;

import MyServer.MyServicePackage.unknown_customerID;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Properties;
/**
 * This class is the implementation object for your IDL interface.
 *
 * Let the Eclipse complete operations code by choosing 'Add unimplemented methods'.
 */

public class Front_EndImpl extends MyServer.MyServicePOA
{
	Properties prop = new Properties();	 
	InetAddress mcAddress = null;
	int mcPort = 0;	
	int m1Count,m2Count,m3Count;
	int t1Count,t2Count,t3Count;
	int v1Count,v2Count,v3Count;
	int sequenceNo, lastSendSeqNo;
	HashMap<Integer,String> requestMap = null;
	
	public Front_EndImpl()
	{	
		m1Count = 0;
		m2Count = 0;
		m3Count = 0;
		
		t1Count = 0;
		t2Count = 0;
		t3Count = 0;
		
		v1Count = 0;
		v2Count = 0;
		v3Count = 0;
		
		sequenceNo = 1;
		lastSendSeqNo = 1;
		requestMap = new HashMap<Integer,String>();
	}
	@Override
	public String buy(String customerID, int itemID, int numberOfItem) throws unknown_customerID
	{
		String msg = "";
		try
		{			
			prop.load(new FileInputStream("C:\\Users\\prachi\\workspace\\DRS.Project\\src\\Front_End\\Config.properties"));
			byte[] sendBytes = new byte[1024];			
			byte[] receiveData = new byte[1024];
			String sendVal = "buy" + "~" + customerID +"~" + itemID +"~" + numberOfItem + "~";
			MulticastSocket sock = new MulticastSocket();			     
		    sock.setTimeToLive(1);			  
		    sendBytes=sendVal.getBytes();		
			if(customerID.startsWith("M"))
			{
				String m1ReturnVal ="", m2ReturnVal="", m3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8000);
				mcAddress = InetAddress.getByName(prop.getProperty("Montreal.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Montreal.multiCast.portNo"));					       
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());				
					if(receivePacket.getPort() == 1000)
					{
						m1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1001)
					{
						m2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1002)
					{
						m3ReturnVal = returnVal;
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareMontreal(m1ReturnVal,m2ReturnVal,m3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("T"))
			{
				String t1ReturnVal ="", t2ReturnVal="", t3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8001);
				mcAddress = InetAddress.getByName(prop.getProperty("Toronto.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Toronto.multiCast.portNo"));				 
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 2000)
					{
						t1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2001)
					{
						t2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2002)
					{
						t3ReturnVal = returnVal;
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareToronto(t1ReturnVal,t2ReturnVal,t3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("V"))
			{
				String v1ReturnVal ="", v2ReturnVal="", v3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8002);
				mcAddress = InetAddress.getByName(prop.getProperty("Vancouver.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Vancouver.multiCast.portNo"));					      
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 3000)
					{
						v1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3001)
					{
						v2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3002)
					{
						v3ReturnVal = returnVal;
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareVancouver(v1ReturnVal,v2ReturnVal,v3ReturnVal);				
				msg = data;				
			}	
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return msg;	
	}

	@Override
	public String itemReturned(String customerID, int itemID, int numberOfItem) throws unknown_customerID
	{
		String msg = "";
		try
		{			
			prop.load(new FileInputStream("C:\\Users\\prachi\\workspace\\DRS.Project\\src\\Front_End\\Config.properties"));
			byte[] sendBytes = new byte[1024];			
			byte[] receiveData = new byte[1024];
			String sendVal =  "itemReturned" + "~" + customerID +"~" + itemID +"~" + numberOfItem + "~" + sequenceNo + "~";
			lastSendSeqNo = sequenceNo;
			requestMap.put(sequenceNo, "");
			sequenceNo++;
			MulticastSocket sock = new MulticastSocket();			     
		    sock.setTimeToLive(1);			  
		    sendBytes=sendVal.getBytes();		
			if(customerID.startsWith("M"))
			{
				String m1ReturnVal ="", m2ReturnVal="", m3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8000);
				mcAddress = InetAddress.getByName(prop.getProperty("Montreal.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Montreal.multiCast.portNo"));					       
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());				
					if(receivePacket.getPort() == 1000)
					{
						String[] tempVal = returnVal.split("~");
						int recSeqNo = Integer.parseInt(tempVal[0]);
						if(recSeqNo == lastSendSeqNo)
						{
							m1ReturnVal = tempVal[1];	
						}						
						else
						{
							requestMap.put(recSeqNo, tempVal[1]);
						}												
					}
					else if(receivePacket.getPort() == 1001)
					{
						String[] tempVal = returnVal.split("~");
						int recSeqNo = Integer.parseInt(tempVal[0]);
						if(recSeqNo == lastSendSeqNo)
						{
							m2ReturnVal = tempVal[1];	
						}						
						else
						{
							requestMap.put(recSeqNo, tempVal[1]);
						}	
					}
					else if(receivePacket.getPort() == 1002)
					{
						String[] tempVal = returnVal.split("~");
						int recSeqNo = Integer.parseInt(tempVal[0]);
						if(recSeqNo == lastSendSeqNo)
						{
							m3ReturnVal = tempVal[1];	
						}						
						else
						{
							requestMap.put(recSeqNo, tempVal[1]);
						}	
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareMontreal(m1ReturnVal,m2ReturnVal,m3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("T"))
			{
				String t1ReturnVal ="", t2ReturnVal="", t3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8001);
				mcAddress = InetAddress.getByName(prop.getProperty("Toronto.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Toronto.multiCast.portNo"));				 
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 2000)
					{
						t1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2001)
					{
						t2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2002)
					{
						t3ReturnVal = returnVal;
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareToronto(t1ReturnVal,t2ReturnVal,t3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("V"))
			{
				String v1ReturnVal ="", v2ReturnVal="", v3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8002);
				mcAddress = InetAddress.getByName(prop.getProperty("Vancouver.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Vancouver.multiCast.portNo"));					      
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);		
			    
			    for(int i=0;i<3;i++)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 3000)
					{
						v1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3001)
					{
						v2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3002)
					{
						v3ReturnVal = returnVal;
					}										
			    }	
			    serverSocket.close();
				String data = "";
				data = compareVancouver(v1ReturnVal,v2ReturnVal,v3ReturnVal);				
				msg = data;				
			}		
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return msg;
	}

	@Override
	public String checkStock(String customerID,int itemID) throws unknown_customerID
	{
		String msg = "";
		try
		{			
			prop.load(new FileInputStream("C:\\Users\\prachi\\workspace\\DRS.Project\\src\\Front_End\\Config.properties"));
			byte[] sendBytes = new byte[1024];			
			byte[] receiveData = new byte[1024];
			String sendVal = "checkStock" + "~" + itemID +"~" ;
			MulticastSocket sock = new MulticastSocket();			     
		    sock.setTimeToLive(1);			  
		    sendBytes=sendVal.getBytes();		
			if(customerID.startsWith("M"))
			{
				int i = 0;
				String m1ReturnVal ="", m2ReturnVal="", m3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8000);
				mcAddress = InetAddress.getByName(prop.getProperty("Montreal.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Montreal.multiCast.portNo"));					       
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {
			    	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());				
					if(receivePacket.getPort() == 1000)
					{
						m1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1001)
					{
						m2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1002)
					{
						m3ReturnVal = returnVal;
					}	
					i++;
					if(i==3)
						break;
			    }
			    serverSocket.close();
				String data = "";
				data = compareMontreal(m1ReturnVal,m2ReturnVal,m3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("T"))
			{
				int i =0;
				String t1ReturnVal ="", t2ReturnVal="", t3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8001);
				mcAddress = InetAddress.getByName(prop.getProperty("Toronto.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Toronto.multiCast.portNo"));				 
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 2000)
					{
						t1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2001)
					{
						t2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2002)
					{
						t3ReturnVal = returnVal;
					}		
					i++;
					if(i==3)
						break;
			    }	
			    serverSocket.close();
				String data = "";
				data = compareToronto(t1ReturnVal,t2ReturnVal,t3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("V"))
			{
				int i = 0;
				String v1ReturnVal ="", v2ReturnVal="", v3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8002);
				mcAddress = InetAddress.getByName(prop.getProperty("Vancouver.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Vancouver.multiCast.portNo"));					      
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 3000)
					{
						v1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3001)
					{
						v2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3002)
					{
						v3ReturnVal = returnVal;
					}	
					i++;
					if(i==3)
						break;
			    }	
			    serverSocket.close();
				String data = "";
				data = compareVancouver(v1ReturnVal,v2ReturnVal,v3ReturnVal);				
				msg = data;				
			}	
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return msg;
	}

	@Override
	public String exchange(String customerID, int boughtItemID, int boughtNumber, int desiredItemID, int desiredNumber) throws unknown_customerID
	{

		String msg = "";
		System.out.println("Customer ID::::"+customerID);
		try
		{					
			prop.load(new FileInputStream("C:\\Users\\prachi\\workspace\\DRS.Project\\src\\Front_End\\Config.properties"));
			byte[] sendBytes = new byte[1024];			
			byte[] receiveData = new byte[1024];
			String sendVal = "exchange" + "~" + customerID +"~" + boughtItemID +"~" + boughtNumber + "~" + desiredItemID +"~" + desiredNumber + "~";
			MulticastSocket sock = new MulticastSocket();			     
		    sock.setTimeToLive(1);			  
		    sendBytes=sendVal.getBytes();		
			if(customerID.startsWith("M"))
			{
				int i =0;
				String m1ReturnVal ="", m2ReturnVal="", m3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8000);
				mcAddress = InetAddress.getByName(prop.getProperty("Montreal.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Montreal.multiCast.portNo"));					       
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());				
					if(receivePacket.getPort() == 1000)
					{
						m1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1001)
					{
						m2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 1002)
					{
						m3ReturnVal = returnVal;
					}
					i++;
					if(i==3)
						break;
			    }	
			    serverSocket.close();
				String data = "";
				data = compareMontreal(m1ReturnVal,m2ReturnVal,m3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("T"))
			{
				int i =0;
				String t1ReturnVal ="", t2ReturnVal="", t3ReturnVal="";
			    DatagramSocket serverSocket = new DatagramSocket(8001);
				mcAddress = InetAddress.getByName(prop.getProperty("Toronto.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Toronto.multiCast.portNo"));				 
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {			    				   
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 2000)
					{
						t1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2001)
					{
						t2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 2002)
					{
						t3ReturnVal = returnVal;
					}
					i++;
					if(i==3)
						break;
			    }	
			    serverSocket.close();
				String data = "";
				data = compareToronto(t1ReturnVal,t2ReturnVal,t3ReturnVal);				
				msg = data;				
			}
			else if(customerID.startsWith("V"))
			{
				String v1ReturnVal ="", v2ReturnVal="", v3ReturnVal="";
				int i =0;
			    DatagramSocket serverSocket = new DatagramSocket(8002);
				mcAddress = InetAddress.getByName(prop.getProperty("Vancouver.multiCast.address"));
				mcPort = Integer.parseInt(prop.getProperty("Vancouver.multiCast.portNo"));					      
			    DatagramPacket packet = new DatagramPacket(sendBytes,sendBytes.length, mcAddress, mcPort);			       
			    sock.send(packet);				    
			    while(true)
			    {
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					String returnVal = new String(receiveData,0,receivePacket.getLength());					
					if(receivePacket.getPort() == 3000)
					{
						v1ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3001)
					{
						v2ReturnVal = returnVal;
					}
					else if(receivePacket.getPort() == 3002)
					{
						v3ReturnVal = returnVal;
					}
					i++;
					if(i==3)
						break;
			    }	
			    serverSocket.close();
				String data = "";
				data = compareVancouver(v1ReturnVal,v2ReturnVal,v3ReturnVal);				
				msg = data;				
			}	
			sock.close();			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return msg;
	}
	
	public String intimateMontrealReplicaManager(String serverName)
	{
		String returnVal = "";
		try
		{
		  DatagramSocket clientSocket = new DatagramSocket(8000);
	      InetAddress IPAddress = InetAddress.getByName(prop.getProperty("ReplicaManager.hostName"));
	      int portNo = Integer.parseInt(prop.getProperty("Montreal.ReplicaManager.portNo"));
	      byte[] sendData = new byte[1024];
	      byte[] receiveData = new byte[1024];		
	      String sendVal = serverName; //Montreal1
	      sendData = sendVal.getBytes();
	      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
	      clientSocket.send(sendPacket);
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	      clientSocket.receive(receivePacket);
	      returnVal = new String(receiveData,0,receivePacket.getLength());		      
	      clientSocket.close();
	      System.out.println("returnVal>>>>>>>>"+returnVal);	      
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnVal;
	}
	
	public String compareMontreal(String m1ServerVal, String m2ServerVal, String m3ServerVal)
	{		
		String returnVal = "";
		if(m1ServerVal.equalsIgnoreCase(m2ServerVal) && m1ServerVal.equalsIgnoreCase(m3ServerVal))
		{
			return m1ServerVal;
		}
		else
		{			
			if(m1ServerVal.equalsIgnoreCase(m2ServerVal))
			{
				m3Count++;
			}
			else if(m1ServerVal.equalsIgnoreCase(m3ServerVal))
			{
				m2Count++;
			}
			else if(m2ServerVal.equalsIgnoreCase(m3ServerVal))
			{
				m1Count++;
			}
			System.out.println("m1Count>>>>>>>>>>>>>"+m1Count);
			if(m3Count == 3)
			{
				intimateMontrealReplicaManager("Montreal3");
				m3Count = 0;
			}
			else if(m2Count == 3)
			{
				intimateMontrealReplicaManager("Montreal2");
				m2Count = 0;
			}
			else if(m1Count == 3)
			{
				intimateMontrealReplicaManager("Montreal1");
				m1Count = 0;
			}
			if(m1ServerVal.equalsIgnoreCase(m2ServerVal) || m1ServerVal.equalsIgnoreCase(m3ServerVal))
			{
				returnVal = m1ServerVal;	
			}
			else if(m2ServerVal.equalsIgnoreCase(m1ServerVal) || m2ServerVal.equalsIgnoreCase(m3ServerVal))
			{
				returnVal = m2ServerVal;
			}
			else if(m3ServerVal.equalsIgnoreCase(m1ServerVal) || m3ServerVal.equalsIgnoreCase(m2ServerVal))
			{
				returnVal = m3ServerVal;
			}
			
		}
		return returnVal;
	}
	public String intimateTorontoReplicaManager(String serverName)
	{
		String returnVal = "";
		try
		{
		  DatagramSocket clientSocket = new DatagramSocket(8000);
	      InetAddress IPAddress = InetAddress.getByName(prop.getProperty("ReplicaManager.hostName"));
	      int portNo = Integer.parseInt(prop.getProperty("Toronto.ReplicaManager.portNo"));
	      byte[] sendData = new byte[1024];
	      byte[] receiveData = new byte[1024];		
	      String sendVal = serverName; //Toronto1
	      sendData = sendVal.getBytes();
	      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
	      clientSocket.send(sendPacket);
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	      clientSocket.receive(receivePacket);
	      returnVal = new String(receiveData,0,receivePacket.getLength());		      
	      clientSocket.close();
	      System.out.println("returnVal>>>>>>>>"+returnVal);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnVal;
	}
	
	public String compareToronto(String t1ServerVal, String t2ServerVal, String t3ServerVal)
	{		
		String returnVal = "";
		if(t1ServerVal.equalsIgnoreCase(t2ServerVal) && t1ServerVal.equalsIgnoreCase(t3ServerVal))
		{
			return t1ServerVal;
		}
		else
		{			
			if(t1ServerVal.equalsIgnoreCase(t2ServerVal))
			{
				t3Count++;
			}
			else if(t1ServerVal.equalsIgnoreCase(t3ServerVal))
			{
				t2Count++;
			}
			else if(t2ServerVal.equalsIgnoreCase(t3ServerVal))
			{
				t1Count++;
			}
			System.out.println("t1Count>>>>>>>>>>>>>"+t1Count);
			if(t3Count == 3)
			{
			    intimateTorontoReplicaManager("Toronto3");	
			    t3Count = 0;
			}
			else if(t2Count == 3)
			{
				intimateTorontoReplicaManager("Toronto2");
				t2Count = 0;
			}
			else if(t1Count == 3)
			{
				intimateTorontoReplicaManager("Toronto1");
				t1Count = 0;
			}
			if(t1ServerVal.equalsIgnoreCase(t2ServerVal) || t1ServerVal.equalsIgnoreCase(t3ServerVal))
			{
				returnVal = t1ServerVal;	
			}
			else if(t2ServerVal.equalsIgnoreCase(t1ServerVal) || t2ServerVal.equalsIgnoreCase(t3ServerVal))
			{
				returnVal = t2ServerVal;
			}
			else if(t3ServerVal.equalsIgnoreCase(t1ServerVal) || t3ServerVal.equalsIgnoreCase(t2ServerVal))
			{
				returnVal = t3ServerVal;
			}
			
		}
		return returnVal;
	}
	public String intimateVancouverReplicaManager(String serverName)
	{
		String returnVal = "";
		try
		{
		  DatagramSocket clientSocket = new DatagramSocket(8000);
	      InetAddress IPAddress = InetAddress.getByName(prop.getProperty("ReplicaManager.hostName"));
	      int portNo = Integer.parseInt(prop.getProperty("Vancouver.ReplicaManager.portNo"));
	      byte[] sendData = new byte[1024];
	      byte[] receiveData = new byte[1024];		
	      String sendVal = serverName; //Vancouver1
	      sendData = sendVal.getBytes();
	      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
	      clientSocket.send(sendPacket);
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	      clientSocket.receive(receivePacket);
	      returnVal = new String(receiveData,0,receivePacket.getLength());		      
	      clientSocket.close();
	      System.out.println("returnVal>>>>>>>>"+returnVal);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnVal;
	}
	
	public String compareVancouver(String v1ServerVal, String v2ServerVal, String v3ServerVal)
	{		
		String returnVal = "";
		if(v1ServerVal.equalsIgnoreCase(v2ServerVal) && v1ServerVal.equalsIgnoreCase(v3ServerVal))
		{
			return v1ServerVal;
		}
		else
		{			
			if(v1ServerVal.equalsIgnoreCase(v2ServerVal))
			{
				v3Count++;
			}
			else if(v1ServerVal.equalsIgnoreCase(v3ServerVal))
			{
				v2Count++;
			}
			else if(v2ServerVal.equalsIgnoreCase(v3ServerVal))
			{
				v1Count++;
			}
			System.out.println("v1Count>>>>>>>>>>>>>"+v1Count);
			if(v3Count == 3)
			{
			    intimateVancouverReplicaManager("Vancouver3");	
			    v3Count = 0;
			}
			else if(v2Count == 3)
			{
				intimateVancouverReplicaManager("Vancouver2");
				v2Count = 0;
			}
			else if(v1Count == 3)
			{
				intimateVancouverReplicaManager("Vancouver1");
				v1Count = 0;
			}
			if(v1ServerVal.equalsIgnoreCase(v2ServerVal) || v1ServerVal.equalsIgnoreCase(v3ServerVal))
			{
				returnVal = v1ServerVal;	
			}
			else if(v2ServerVal.equalsIgnoreCase(v1ServerVal) || v2ServerVal.equalsIgnoreCase(v3ServerVal))
			{
				returnVal = v2ServerVal;
			}
			else if(v3ServerVal.equalsIgnoreCase(v1ServerVal) || v3ServerVal.equalsIgnoreCase(v2ServerVal))
			{
				returnVal = v3ServerVal;
			}
			
		}
		return returnVal;
	}
	
}



