import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;


public class ReplicaManagerImpl {

	String replicaServerName ="";
	String portNo = "";
	
	public ReplicaManagerImpl(String serverName, String replicaPortNo)
	{
		this.replicaServerName = serverName;
		this.portNo = replicaPortNo;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
       ReplicaManagerImpl rm = new ReplicaManagerImpl(args[0],args[1]);
       if(rm.replicaServerName.equalsIgnoreCase("Montreal"))
       {
    	   System.out.println("Montreal Replica Manager ready...");		  
       }
       else if(rm.replicaServerName.equalsIgnoreCase("Toronto"))
       {
    	   System.out.println("Toronto Replica Manager ready...");		  
       }
       else if(rm.replicaServerName.equalsIgnoreCase("Vancouver"))
       {
    	   System.out.println("Vancouver Replica Manager ready...");		
       }
       	                     
       byte[] receiveData = new byte[1024];
       try
       {
       	DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(rm.portNo)); // create a socket to bind with other servers using UDP                                                        
        while(true)  // listen for frontend requests
       	{            		
           	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
             serverSocket.receive(receivePacket);
             ReplicaThread rthread = new ReplicaThread(serverSocket,receivePacket,receiveData);
             rthread.start();   			                                	
       	}
           
       }
       catch (Exception e) 
       {
           System.out.println(e);
           e.printStackTrace();
       }  
	}

}

class ReplicaThread extends Thread
{
	DatagramSocket serverSocket = null;	
	DatagramPacket rcvPacket = null;
	byte[] receiveData = null;
	Properties prop = new Properties();	 
    //load a properties file from class path, inside static method
	
	  
	
	
	public ReplicaThread(DatagramSocket pServerSocket, DatagramPacket pRcvPacket ,byte[] pRcvData)
	{
		serverSocket = pServerSocket;
		rcvPacket = pRcvPacket;
		receiveData = pRcvData;	
	}
	public void run()
	{
		try
		{				 
		 prop.load(new FileInputStream("C:\\Users\\prachi\\workspace\\ReplicaManager\\src\\Config.properties"));
		 // receive data from front end
		String replicaServerName = new String(receiveData,0,rcvPacket.getLength());			
		System.out.println("replicaServerName>>>>>>>>>>>>>>"+replicaServerName);
		String replicaServerHostName = "";
		String replicaServerPortNo="";
		if(replicaServerName.equalsIgnoreCase("Montreal1"))
		{
			replicaServerHostName = prop.getProperty("MServer1.hostName");
			replicaServerPortNo = prop.getProperty("MServer1RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Montreal2"))
		{
			replicaServerHostName = prop.getProperty("MServer2.hostName");
			replicaServerPortNo = prop.getProperty("MServer2RM.portNo");	
		}
		else if(replicaServerName.equalsIgnoreCase("Montreal3"))
		{
			replicaServerHostName = prop.getProperty("MServer3.hostName");
			replicaServerPortNo = prop.getProperty("MServer3RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Toronto1"))
		{
			replicaServerHostName = prop.getProperty("TServer1.hostName");
			replicaServerPortNo = prop.getProperty("TServer1RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Toronto2"))
		{
			replicaServerHostName = prop.getProperty("TServer2.hostName");
			replicaServerPortNo = prop.getProperty("TServer2RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Toronto3"))
		{
			replicaServerHostName = prop.getProperty("TServer3.hostName");
			replicaServerPortNo = prop.getProperty("TServer3RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Vancouver1"))
		{
			replicaServerHostName = prop.getProperty("VServer1.hostName");
			replicaServerPortNo = prop.getProperty("VServer1RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Vancouver2"))
		{
			replicaServerHostName = prop.getProperty("VServer2.hostName");
			replicaServerPortNo = prop.getProperty("VServer2RM.portNo");
		}
		else if(replicaServerName.equalsIgnoreCase("Vancouver3"))
		{
			replicaServerHostName = prop.getProperty("VServer3.hostName");
			replicaServerPortNo = prop.getProperty("VServer3RM.portNo");
		}
				
		 // send stop request to relevant server replica
		  DatagramSocket clientSocket1 = new DatagramSocket();
	      InetAddress IPAddress = InetAddress.getByName(replicaServerHostName);
	      byte[] sendData = new byte[1024];
	      byte[] receiveData = new byte[1024];			      
	      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(replicaServerPortNo));
	      clientSocket1.send(sendPacket);
	      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	      clientSocket1.receive(receivePacket);
	      String returnVal = new String(receiveData,0,receivePacket.getLength());		      
	      clientSocket1.close();
	 	 // receive success message from server replica
	      if(!returnVal.equalsIgnoreCase("") && returnVal.equalsIgnoreCase("success"))
	      {
	    		 // send it to the front end
	    	  String msg = "success";
	    	  sendData = msg.getBytes();
	    	  sendPacket = new DatagramPacket(sendData, sendData.length, rcvPacket.getAddress(), rcvPacket.getPort());
	    	  serverSocket.send(sendPacket);	    	  
	      }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	      
	}
}
