package Common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


	public class ProcessingThread implements Runnable{
		DatagramSocket serverSocket = null;
		RetailStoreOperations rs = null;
		DatagramPacket rcvPacket = null;
		byte[] receiveData = null;
		int individualPort  = 0;
		
		public ProcessingThread(DatagramSocket pServerSocket, DatagramPacket pRcvPacket ,byte[] pRcvData,RetailStoreOperations pRs)
		{
			serverSocket = pServerSocket;
			rcvPacket = pRcvPacket;
			receiveData = pRcvData;
			rs = pRs;
		}	
		public ProcessingThread(int indPort,DatagramSocket pServerSocket, DatagramPacket pRcvPacket ,byte[] pRcvData,RetailStoreOperations pRs)
		{
			individualPort = indPort;
			serverSocket = pServerSocket;
			rcvPacket = pRcvPacket;
			receiveData = pRcvData;
			rs = pRs;
		}	
		
		@Override
		public void run() {		          
	        String method = "";
	        String[] arrData = null;
	        byte[] sendData = null;
			 try
			 {  
				 String sentence = new String(receiveData,0,rcvPacket.getLength());		
				 System.out.println("Output>>>>>>>>"+sentence);
	             InetAddress IPAddress = rcvPacket.getAddress();
	             int port = rcvPacket.getPort();             
	             arrData = sentence.split("~");
	             method = arrData[0]!=null?arrData[0]:"";                
	            if(method.equalsIgnoreCase("checkStockLocally"))
	            {                	                
		                int itemID = arrData[1]!=null?Integer.parseInt(arrData[1]):0;	                 
		                Integer noOfAvailableItems = rs.checkStockLocally(itemID);     	                
		            	sendData = noOfAvailableItems.toString().getBytes();
		                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		                serverSocket.send(sendPacket);	                
	            }
	            else if(method.equalsIgnoreCase("checkStock"))
	            {
	            	IPAddress = InetAddress.getByName("localhost"); // host name of Front End
	            	if(individualPort == 1000 || individualPort == 1001 || individualPort == 1002)
	            	{
	            		port = 8000; // port no of Front End	
	            	}
	            	else if(individualPort == 2000 || individualPort == 2001 || individualPort == 2002)
	            	{
	            		port = 8001; // port no of Front End	
	            	}
	            	else if(individualPort == 3000 || individualPort == 3001 || individualPort == 3002)
	            	{
	            		port = 8002; // port no of Front End	
	            	}
	            	String successVal = "";	            	
	            	int itemID = arrData[1]!=null?Integer.parseInt(arrData[1]):0;            		            	
	            	successVal = rs.checkStock(itemID);
	            	sendData = successVal.getBytes();
	            	DatagramSocket socket = new DatagramSocket(individualPort);
	                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                socket.send(sendPacket);    
	                socket.close();
	            }
	            else if(method.equalsIgnoreCase("buy"))
	            {
	            	IPAddress = InetAddress.getByName("localhost"); // host name of Front End
	            	if(individualPort == 1000 || individualPort == 1001 || individualPort == 1002)
	            	{
	            		port = 8000; // port no of Front End	
	            	}
	            	else if(individualPort == 2000 || individualPort == 2001 || individualPort == 2002)
	            	{
	            		port = 8001; // port no of Front End	
	            	}
	            	else if(individualPort == 3000 || individualPort == 3001 || individualPort == 3002)
	            	{
	            		port = 8002; // port no of Front End	
	            	}
	            	String successVal = "";
	            	String customerId = arrData[1]!=null?arrData[1]:""; 
	            	int itemID = arrData[2]!=null?Integer.parseInt(arrData[2]):0;            	
	            	int noOfReqItem = Integer.parseInt(arrData[3]!=null?arrData[3]:"");
	            	successVal = rs.buy(customerId,itemID, noOfReqItem);
	            	sendData = successVal.getBytes();
	            	DatagramSocket socket = new DatagramSocket(individualPort);
	                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                socket.send(sendPacket);       
	                socket.close();
	            }
	            else if(method.equalsIgnoreCase("itemReturned"))
	            {
	            	IPAddress = InetAddress.getByName("localhost"); // host name of Front End
	            	if(individualPort == 1000 || individualPort == 1001 || individualPort == 1002)
	            	{
	            		port = 8000; // port no of Front End	
	            	}
	            	else if(individualPort == 2000 || individualPort == 2001 || individualPort == 2002)
	            	{
	            		port = 8001; // port no of Front End	
	            	}
	            	else if(individualPort == 3000 || individualPort == 3001 || individualPort == 3002)
	            	{
	            		port = 8002; // port no of Front End	
	            	}
	            	String successVal = "";
	            	String customerId = arrData[1]!=null?arrData[1]:""; 
	            	int itemID = arrData[2]!=null?Integer.parseInt(arrData[2]):0;            	
	            	int noOfReqItem = Integer.parseInt(arrData[3]!=null?arrData[3]:"");
	            	String sequenceNo = arrData[4]!=null?arrData[4]:"";
	            	successVal = rs.itemReturned(customerId,itemID, noOfReqItem);
	            	successVal  = sequenceNo+"~"+successVal;
	            	sendData = successVal.getBytes();
	            	DatagramSocket socket = new DatagramSocket(individualPort);
	                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                socket.send(sendPacket);  
	                socket.close();
	            }
	            else if(method.equalsIgnoreCase("exchange"))
	            {
	            	IPAddress = InetAddress.getByName("localhost"); // host name of Front End
	            	if(individualPort == 1000 || individualPort == 1001 || individualPort == 1002)
	            	{
	            		port = 8000; // port no of Front End	
	            	}
	            	else if(individualPort == 2000 || individualPort == 2001 || individualPort == 2002)
	            	{
	            		port = 8001; // port no of Front End	
	            	}
	            	else if(individualPort == 3000 || individualPort == 3001 || individualPort == 3002)
	            	{
	            		port = 8002; // port no of Front End	
	            	}
	            	
	            	String successVal = "";
	            	String customerID = arrData[1]!=null?arrData[1]:""; 
	            	int itemID = arrData[2]!=null?Integer.parseInt(arrData[2]):0;            	
	            	int noOfReqItem = Integer.parseInt(arrData[3]!=null?arrData[3]:"");
	            	int desiredItemID = arrData[4]!=null?Integer.parseInt(arrData[4]):0;            	
	            	int desiredNumber = Integer.parseInt(arrData[5]!=null?arrData[5]:"");
	            	successVal = rs.exchange(customerID, itemID, noOfReqItem, desiredItemID, desiredNumber);
	            	sendData = successVal.getBytes();
	            	DatagramSocket socket = new DatagramSocket(individualPort);
	                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                socket.send(sendPacket);    
	                socket.close();
	            }
	            else if(method.equalsIgnoreCase("orderItem"))
	            {
	            	Integer successVal ;
	            	int itemID = arrData[1]!=null?Integer.parseInt(arrData[1]):0;            	
	            	int noOfReqItem = Integer.parseInt(arrData[2]!=null?arrData[2]:"");
	            	successVal = rs.isItemAvailableInCurrentStore(itemID, noOfReqItem);
	            	sendData = successVal.toString().getBytes();
	                DatagramPacket sendPacket =	new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                serverSocket.send(sendPacket);            	
	            }
	            else if(method.equalsIgnoreCase("lockStatus"))
	            {
	            	Boolean successFlag = false ;
	            	int itemID = arrData[1]!=null?Integer.parseInt(arrData[1]):0;            
	            	int flag = Integer.parseInt(arrData[2]!=null?arrData[2]:"");
	            	if(flag == 0)
	            	{
	            		successFlag = rs.createLock(itemID);	
	            	}
	            	else if(flag == 1)
	            	{
	            		successFlag = rs.releaseLock(itemID);
	            	}
	            	            	
	            	sendData = successFlag.toString().getBytes();
	                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	                serverSocket.send(sendPacket);
	            }	
	            else if(method.equalsIgnoreCase("getItemDetails"))
	            {                	                		                             
	            		String rcvdData = rs.getItemDetails();     	                
		            	sendData = rcvdData.getBytes();
		                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		                serverSocket.send(sendPacket);	                
	            }
	            else if(method.equalsIgnoreCase("getItemLockDetails"))
	            {                	                		                             
	            		String rcvdData = rs.getItemLockDetails();     	                
		            	sendData = rcvdData.getBytes();
		                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		                serverSocket.send(sendPacket);	                
	            }
	        }
	        catch (Exception e) {
	            System.out.println(e);
	            e.printStackTrace();
	            }

	  }
}
