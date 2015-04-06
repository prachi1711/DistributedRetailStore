package Common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastThread extends Thread {
	
	int serverPort = 0;
	int mcPort =0;
	InetAddress mcAddress = null;
	RetailStoreOperations rs = null;
	MulticastSocket sock = null;	
	int individualPort = 0;
	public MultiCastThread( int inPort,int portNo, InetAddress address,RetailStoreOperations mrs)
	{		
		individualPort = inPort;
		mcPort = portNo;
		mcAddress = address;
		rs = mrs;
	}
	public void run()
	{
		try
		{
			sock = new MulticastSocket(mcPort);		      
		    sock.setReuseAddress(true); 		      
		    sock.joinGroup(mcAddress);
		    while (true) 
		    {  		         		        
		        byte[] buf = new byte[1024];
		        DatagramPacket packet = new DatagramPacket(buf, buf.length);		        
		        sock.receive(packet);			        
		        System.out.println("Received " + packet.getLength() + " bytes from " + packet.getAddress() + ": " + new String(packet.getData(),0,packet.getLength()));
		        ProcessingThread pThread = new ProcessingThread(individualPort,sock,packet,buf,rs); // create a different thread for every socket accept request
	            Thread th = new Thread(pThread);
	            th.start();                     
		    }		      
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public MulticastSocket getMultiCastSocketObject()
	{
		return sock;
	}
}
