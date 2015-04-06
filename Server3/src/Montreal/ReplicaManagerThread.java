package Montreal;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import Common.MultiCastThread;
import Montreal.MontrealServerImpl;

public class ReplicaManagerThread extends Thread {
	 protected HashMap<Integer, Integer> itemHT = new HashMap<Integer, Integer>(); // to store the items with their stock in the store
	 protected HashMap<Integer, Semaphore> semaphoreHM = new HashMap<Integer, Semaphore>();	// to create semaphore for each item in the store
	 protected String montrealServer1Name = "";
	 private static Logger logger = Logger.getLogger("MontrealServerImpl");	
	 DatagramSocket rmSock = null;
	 DatagramPacket receivePacketFromRM = null;
	 byte[] receiveData = new byte[1024];
	 byte[] sendDataToRM = null;
	 boolean isNew = false;
	 MultiCastThread mcThread = null;
	 DatagramSocket serverSocket = null;
	 public ReplicaManagerThread(DatagramSocket rmSocket,DatagramPacket rcvPacket,byte[] rcvData,String mServer1Name, boolean flag)
	 {
		 this.rmSock = rmSocket;
		 this.receivePacketFromRM = rcvPacket;
		 this.receiveData = rcvData;
		 this.montrealServer1Name = mServer1Name;
		 this.isNew = flag;
	 }
    public void run()
    {       
       if(isNew)
		{
				//add Items in the HashMap of items with their availability in store
				this.addItem(1001,5);	
				this.addItem(1002,15);
				this.addItem(1003,12);
				this.addItem(1004,1);	
						
				//add semaphores in the HashMap of items for each item			 
				semaphoreHM.put(1001, new Semaphore(1,true));
				semaphoreHM.put(1002, new Semaphore(1,true));
				semaphoreHM.put(1003, new Semaphore(1,true));
				semaphoreHM.put(1004, new Semaphore(1,true));
		}
		else
		{					
			try
			{
				
				  DatagramSocket clientSocket1 = new DatagramSocket();
			      InetAddress IPAddress = InetAddress.getByName(montrealServer1Name);
			      byte[] sendData = new byte[1024];
			      byte[] receiveData = new byte[1024];		
			      String sendVal = "getItemDetails";
			      sendData = sendVal.getBytes();
			      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4001);
			      clientSocket1.send(sendPacket);
			      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket1.receive(receivePacket);
			      String returnVal = new String(receiveData,0,receivePacket.getLength());		      
			      clientSocket1.close();			      
			      itemHT = new HashMap<Integer, Integer>();
			      String[] rcvdData = returnVal.split("\n");
			      for(int i=0;i<rcvdData.length;i++)
			      {
			    	  String[] items = rcvdData[i].split("~");
			    	  {
			    		  this.addItem(Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			    	  }
			      }
			      											   
			      DatagramSocket clientSocket2 = new DatagramSocket();
			      IPAddress = InetAddress.getByName(montrealServer1Name);
			      sendData = new byte[1024];
			      receiveData = new byte[1024];		
			      sendVal = "getItemLockDetails";
			      sendData = sendVal.getBytes();
			      sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4001);
			      clientSocket2.send(sendPacket);
			      receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket2.receive(receivePacket);
			      returnVal = new String(receiveData,0,receivePacket.getLength());		      
			      clientSocket2.close();	
			      semaphoreHM = new HashMap<Integer, Semaphore>();
			      rcvdData = returnVal.split("\n");
			      for(int i=0;i<rcvdData.length;i++)
			      {
			    	  String[] items = rcvdData[i].split("~");
			    	  {
			    		  semaphoreHM.put(Integer.parseInt(items[0]), new Semaphore(Integer.parseInt(items[1]),true));			    		  
			    	  }
			      }
			      
			      //send success reply to replica manager
			      sendDataToRM = new byte[1024];
			      String val = "success";
			      sendData = val.getBytes();
	              DatagramPacket sendPacketToRM = new DatagramPacket(sendData, sendData.length, receivePacketFromRM.getAddress(), receivePacketFromRM.getPort());
	              rmSock.send(sendPacketToRM);
			      
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
						
		}
       MontrealServerImpl m = new MontrealServerImpl(itemHT,semaphoreHM);
       System.out.println("Montreal Server 3 ready...");	
       int multiCastPortNo = 4446;
       InetAddress multiCastAddress = null;
       try 
       {
    	   multiCastAddress = InetAddress.getByName("228.5.6.7");
       } 
       catch (UnknownHostException e1) 
       {		
    	   e1.printStackTrace();
       }
	   mcThread = new MultiCastThread(1002,multiCastPortNo,multiCastAddress,m);
	   mcThread.start();
       try
       {
    	 serverSocket = new DatagramSocket(6001); // create a socket to bind with other servers using UDP  
    	 receiveData = new byte[1024];
         while(true)
       	 {            		
           	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           	 serverSocket.receive(receivePacket);                                                        		
   			 Common.ProcessingThread pThread = new Common.ProcessingThread(serverSocket,receivePacket,receiveData,m); // create a different thread for every socket accept request
             Thread th = new Thread(pThread);
             th.start();                                                     		
       	 }           
       }
       catch (Exception e) 
       {
           System.out.println(e);
           e.printStackTrace();
       }  
    }
    private void writeData(int itemID, int noOfAvailableItems) 
	{        
        itemHT.put(itemID, noOfAvailableItems);       
    }
	
	public void addItem(int itemID, int noOfAvailableItems)
    {
        logger.info("RM::addItem(" + itemID + ", " + noOfAvailableItems + ") called");        
        writeData(itemID, noOfAvailableItems);                      
    }
	
	public MultiCastThread getMultiCastThreadObject()
	{
		return mcThread;
	}
	public DatagramSocket getServerSocketObject()
	{
		return serverSocket;
	}
  

}
