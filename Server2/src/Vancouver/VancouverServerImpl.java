package Vancouver;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import Common.RetailStoreOperations;

public class VancouverServerImpl implements RetailStoreOperations{
		

		private static Logger logger = Logger.getLogger("VancouverServerImpl");	
		protected HashMap<Integer, Integer> itemHT = new HashMap<Integer, Integer>(); // to store the items with their stock in the store
		protected HashMap<Integer, Semaphore> semaphoreHM = new HashMap<Integer, Semaphore>();	// to create semaphore for each item in the store
		protected HashMap<String, Semaphore> semaphoreCM = new HashMap<String, Semaphore>();	// to create semaphore for each Customer in the store
		/**
		 * Constructor for VancouverServerImpl 
		 */
		public VancouverServerImpl(HashMap<Integer, Integer> itemHT,HashMap<Integer, Semaphore> semaphoreHM ) {
			this.itemHT = itemHT;
			this.semaphoreHM = semaphoreHM;			
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
		
		
		@Override
		
		/**
		 * It returns the item back to store if client has bought that item before and update the quantity in item list as well as customer file 
		 */
		public String itemReturned(String customerID, int itemID, int noOfReturnedItem){			
			String msg = "";
			File inputFile = null;
			FileReader fileReader = null;          
			boolean flag = false;
			int noOfBoughtItem = 0;
			int noOfAvailableItems = 0;
			Semaphore i1 = null, c1=null;
			boolean i1lockSet = false, c1lockSet = false;
			try 
			{
				if(semaphoreCM.containsKey(customerID))
				{
					c1 = semaphoreCM.get(customerID);
				}
				else
				{
					Semaphore lSema = new Semaphore(1, true);
					semaphoreCM.put(customerID, lSema);
					c1 = lSema;
					
				}
				
				if(c1.tryAcquire(500, TimeUnit.MILLISECONDS))
				{
					c1lockSet = true;
					inputFile = new File(".\\"+customerID+".txt");
					if(inputFile.exists())
					{
						try{
							if(semaphoreHM.containsKey(itemID))
							{
								i1 = semaphoreHM.get(itemID);
								if(i1.tryAcquire(500, TimeUnit.MILLISECONDS))
								{
									i1lockSet = true;
									fileReader = new FileReader(inputFile);            
									BufferedReader br = new BufferedReader(fileReader);
									String line = " ";
								    String [] data;			    
						            File f2 = new File(".\\temp.txt");
						            BufferedWriter out = new BufferedWriter(new FileWriter(f2));
								    while ((line = br.readLine())!= null)
								    { 			        			        
								        data = line.split("\t"); //split spaces	
								        if(data[0]!=null && !data[0].equalsIgnoreCase("") && Integer.parseInt(data[0]) == (itemID))
								        {
								        	noOfBoughtItem = Integer.parseInt(data[1].toString());
								        	if(noOfBoughtItem > noOfReturnedItem)
								        	{
								        		noOfBoughtItem = noOfBoughtItem - noOfReturnedItem;
								        		if(itemHT.containsKey(itemID))
								        		{
								        			noOfAvailableItems = itemHT.get(itemID);
													noOfAvailableItems = noOfAvailableItems + noOfReturnedItem;
													itemHT.remove(itemID);
													this.addItem(itemID, noOfAvailableItems);
								        		}
								        		else
								        		{
								        			this.addItem(itemID, noOfReturnedItem);
								        		}
												
												line = itemID + "\t" + noOfBoughtItem;
												out.write(line);
								                out.newLine();							
												// update customer File with item id and noOfBoughtItem						
								        		flag = true;
								        	}
								        	else if(noOfBoughtItem == noOfReturnedItem)
								        	{
								        		noOfAvailableItems = itemHT.get(itemID)!=null?itemHT.get(itemID):0;
												noOfAvailableItems = noOfAvailableItems + noOfReturnedItem;
												itemHT.remove(itemID);
												this.addItem(itemID, noOfAvailableItems);
												// delete that item from customer File
								        		flag = true;
								        	}			        	
								        }	
								        else
								        {
								        	out.write(line);
							                out.newLine();
								        }
								       
								    }
								    if(!flag)
								    {
								    	msg = "No of items returned ("+noOfReturnedItem+") cannot be more than item bought ("+noOfBoughtItem+")";
								    }
								    else
								    {
								    	msg = noOfReturnedItem+" Item of "+itemID+" returned successfully and data updated";						    	
								    }
								    br.close();
								    fileReader.close();	
								    out.close();
								    inputFile.delete();
							        f2.renameTo(inputFile);
								}
								else
								{
									msg = "Could not lock the item: " +itemID+".Please try again later";
								}
							}
						}
						finally
						{
							if(i1lockSet)
							{
								i1.release();
							}
						}
						
					}
					else
					{
						msg = "Customer "+customerID+" doesn't exist";
					}
				}
				else
				{
					msg = "Could not lock the customer:" + customerID;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(c1lockSet)
				{
					c1.release();
				}
			}
		logger.info(msg);
		return msg;
	  }

		@Override
		/**
		 * Checks in the current store if particular Item is available and in required quantity and returns value of flag accordingly
		 * @flag = 1 : if the item is available in required quantity
		 * @flag = 2 : if the item is available but less than required quantity
		 * @flag = 3: if the item is not available in the store
		 */
		public int isItemAvailableInCurrentStore(int itemID,int noOfReqItem)
		{
			int noOfAvailableStock = 0;		
			int flag = 0;
			if(itemHT.containsKey(itemID))  // if Item is available in the store
			{
				noOfAvailableStock = itemHT.get(itemID);
				if(noOfAvailableStock > noOfReqItem)
				{
					noOfAvailableStock = noOfAvailableStock - noOfReqItem;
					itemHT.remove(itemID);
					this.addItem(itemID, noOfAvailableStock);
					flag = 1;
				}
				else if(noOfAvailableStock == noOfReqItem)
				{
					itemHT.remove(itemID);
					flag = 1;
				}
				else
				{
					flag = 2;
				}
			}
			else
			{
				flag = 3;
			}
			
			return flag;
		}

		@Override
		public boolean createLock(int itemId) {
			Semaphore s1 = null;
			boolean flag = false;
			try
			{
				s1 = semaphoreHM.get(itemId);
				if(s1.tryAcquire(500, TimeUnit.MILLISECONDS))
				{
					flag = true;
				}
				else
				{
					System.out.println("unable to lock "+ itemId + " will re try again");
					flag = false;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
			return flag;
		}

		@Override
		public boolean releaseLock(int itemId) {
			Semaphore s1 = null;
			try
			{
				s1 = semaphoreHM.get(itemId);			
				s1.release();						
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		/**
		 * It buys the item for the customer based on customer Id,item Id and number of item required and updates the item in item table as well as customer file and returns an
		 * appropriate message to client. It also checks if the item required is not available in current store,buys it from the nearest store.
		 */
		public String buy(String customerID, int itemID, int numberOfItem)
		{
			String msg = "";
			boolean buyStatus = false;
			int flag = 0;
			int noOfAvailableItem = 0;
			int noOfReqItem = 0;
			Semaphore i1 = null,c1=null;
			boolean islocked = false;
			boolean islockedT = false;
			boolean islockedM = false;
			boolean c1lockSet = false;
			if(semaphoreCM.containsKey(customerID))
			{
				c1 = semaphoreCM.get(customerID);
			}
			else
			{
				Semaphore lSema = new Semaphore(1, true);
				semaphoreCM.put(customerID, lSema);
				c1 = lSema;
				
			}
			try
			{
				if(c1.tryAcquire(500, TimeUnit.MILLISECONDS))
				{
					c1lockSet = true;	
				
				if(semaphoreHM.containsKey(itemID))
				{
					i1 = semaphoreHM.get(itemID);
					try
					{
						if(i1.tryAcquire(500, TimeUnit.MILLISECONDS)) // locking the item to maintain concurrency
						{
							islocked = true;
						   try
						   {
								flag = isItemAvailableInCurrentStore(itemID,numberOfItem);
								if(flag == 1) //item available in the current store in required quantity
								{
									buyStatus = true;								
								}
								else if(flag == 2)//item available in the current store but is less than required quantity
								{
									islockedT = intimateOtherServerRegLock(itemID,0,5002) ;
									islockedM = intimateOtherServerRegLock(itemID,0,5001);
									if(islockedT && islockedM)
									{
										Hashtable<String ,HashMap<Integer,Integer>> serverItemMap = new Hashtable<String ,HashMap<Integer,Integer>>();;
										noOfAvailableItem = itemHT.get(itemID);
										noOfReqItem = numberOfItem - noOfAvailableItem; 
										int itemsAvailableWithTServer = 0;
										int itemsAvailableWithMServer = 0;
										int totalItemsAvailable = 0;
										serverItemMap = checkStockFromOtherServers(itemID,serverItemMap);
										 java.lang.Object key = null;             
							             for (Enumeration e = serverItemMap.keys(); e.hasMoreElements();) 
							             {
							                 key = e.nextElement();
							                 if(key.equals("Toronto"))
							                 {
								                 for(Integer keys : serverItemMap.get(key).keySet())
								                 {
								                	 
								                	 itemsAvailableWithTServer = itemsAvailableWithTServer + serverItemMap.get(key).get(keys) ;
								                 }
							                 }
							                 else if(key.equals("Montreal"))
							                 {
							                	 for(Integer keys : serverItemMap.get(key).keySet())
								                 {
								                	 
							                		 itemsAvailableWithMServer = itemsAvailableWithMServer + serverItemMap.get(key).get(keys) ;
								                 } 
							                 }
							             }
							             totalItemsAvailable = itemsAvailableWithTServer + itemsAvailableWithMServer;
							             if(totalItemsAvailable >= noOfReqItem)
							             {
							            	 itemHT.remove(itemID);
							            	 if(itemsAvailableWithTServer >= noOfReqItem)
							            	 {
							            		 orderItemFromOtherStore(itemID,noOfReqItem,5002);
							            	 }
							            	 else
							            	 {
							            		 noOfReqItem = noOfReqItem - itemsAvailableWithTServer;
							            		 orderItemFromOtherStore(itemID,itemsAvailableWithTServer,5002);
							            		 orderItemFromOtherStore(itemID,noOfReqItem,5001);
							            	 }
							            	 buyStatus = true;
								         }
							             else
							             {
							            	 buyStatus = false;
							             }
									}
									else
									{
										buyStatus = false;
									}														
								}
								else if(flag == 3) // item not available in the current store
								{
									islockedT = intimateOtherServerRegLock(itemID,0,5002) ;
									islockedM = intimateOtherServerRegLock(itemID,0,5001);
									if(islockedT && islockedM)							
									{
										Hashtable<String ,HashMap<Integer,Integer>> serverItemMap = new Hashtable<String ,HashMap<Integer,Integer>>();;			
										int itemsAvailableWithTServer = 0;
										int itemsAvailableWithMServer = 0;
										int totalItemsAvailable = 0;
										serverItemMap = checkStockFromOtherServers(itemID,serverItemMap);
										 java.lang.Object key = null;             
							             for (Enumeration e = serverItemMap.keys(); e.hasMoreElements();) 
							             {
							                 key = e.nextElement();
							                 if(key.equals("Toronto"))
							                 {
								                 for(Integer keys : serverItemMap.get(key).keySet())
								                 {
								                	 
								                	 itemsAvailableWithTServer = itemsAvailableWithTServer + serverItemMap.get(key).get(keys) ;
								                 }
							                 }
							                 else if(key.equals("Montreal"))
							                 {
							                	 for(Integer keys : serverItemMap.get(key).keySet())
								                 {
								                	 
							                		 itemsAvailableWithMServer = itemsAvailableWithMServer + serverItemMap.get(key).get(keys) ;
								                 } 
							                 }
						             }
						             totalItemsAvailable = itemsAvailableWithTServer + itemsAvailableWithMServer;
						             if(totalItemsAvailable >= numberOfItem)
						             {
						            	 itemHT.remove(itemID);
						            	 if(itemsAvailableWithTServer >= numberOfItem)
						            	 {
						            		 orderItemFromOtherStore(itemID,numberOfItem,5002);
						            	 }
						            	 else
						            	 {
						            		 int tempNo = 0;
						            		 tempNo = numberOfItem - itemsAvailableWithTServer;
						            		 orderItemFromOtherStore(itemID,itemsAvailableWithTServer,5002);
						            		 orderItemFromOtherStore(itemID,tempNo,5001);
						            	 }
						            	 buyStatus = true;
						             }
						             else
						             {
						            	 buyStatus = false;
						             }
									}
									else
									{
										buyStatus = false;
									}						
							    }				
							if(buyStatus)
							{
								File inputFile = null;
								FileReader fileReader = null;          			
								int noOfBoughtItem = 0;			
								try 
								{					
									inputFile = new File(".\\"+customerID+".txt");
									if(inputFile.exists())
									{
										fileReader = new FileReader(inputFile);            
										BufferedReader br = new BufferedReader(fileReader);
										String line = " ";
									    String [] data;			    
							            File f2 = new File(".\\temp.txt");
							            BufferedWriter out = new BufferedWriter(new FileWriter(f2));
							            boolean toInsert = true;
									    while ((line = br.readLine())!= null)
									    { 			        			        
									        data = line.split("\t"); //split spaces	
									        if(data[0]!=null && !data[0].equalsIgnoreCase("") && Integer.parseInt(data[0]) == (itemID))
									        {
									        	noOfBoughtItem = Integer.parseInt(data[1].toString());
									        	
									        		noOfBoughtItem = noOfBoughtItem + numberOfItem;								
													line = itemID + "\t" + noOfBoughtItem;
													out.write(line);
									                out.newLine();
									                toInsert = false;
													// update customer File with item id and noOfBoughtItem										        						        		        	
									        }	
									        else
									        {
									        	out.write(line);
								                out.newLine();
								               
									        }				       
									    }
									    if(toInsert)
									    {
									    	out.write(itemID + "\t" + numberOfItem);
									    	out.newLine();
									    }
									    br.close();
									    fileReader.close();	
									    out.close();
									    inputFile.delete();
								        f2.renameTo(inputFile);
									}
									else
									{
										String line = " ";
										File f2 = new File(".\\"+customerID+".txt");
								        BufferedWriter out = new BufferedWriter(new FileWriter(f2));					   					        	
						        		noOfBoughtItem = noOfBoughtItem + numberOfItem;								
										line = itemID + "\t" + numberOfItem;
										out.write(line);
						                out.newLine();																											        						        		        						       					       					    			   					 
										out.close();					  
									}
									msg = numberOfItem +" Item of "+itemID+" bought successfully";					
								}
							catch(Exception e)
							{
							  e.printStackTrace();	
							}
						}
						else
						{
							msg = "Sorry,Item "+itemID +" not available in required quantity("+numberOfItem+")";
						}
					}			
					finally
					{
						if(islocked)
						{
							i1.release();
						}
						if(islockedT)
						{
							intimateOtherServerRegLock(itemID, 1, 5002);	
						}
						if(islockedM)
						{
							intimateOtherServerRegLock(itemID, 1, 5001);
						}
					}
				}
				else
				{			
					msg = "unable to lock "+ itemID + ". Please try again later.";
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		 }
		}
	  }
	catch(Exception e)
	{
		e.printStackTrace();
	}
	finally
	{
		if(c1lockSet)
		{
			c1.release();
		}
	}   
	logger.info(msg);
	return msg;
	}
		
		public int orderItemFromOtherStore(int itemID,int noOfReqItem,int portNo)
		{			
	        int successVal = 0;
			try
			{
				 DatagramSocket clientSocket = new DatagramSocket();
			      InetAddress IPAddress = InetAddress.getByName("localhost");
			      byte[] sendData = new byte[1024];
			      byte[] receiveData = new byte[1024];		
			      String sendVal = "orderItem" + "~" + itemID + "~" + noOfReqItem +"~";
			      sendData = sendVal.getBytes();
			      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
			      clientSocket.send(sendPacket);
			      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket.receive(receivePacket);
			      String returnVal = new String(receiveData,0,receivePacket.getLength());
			      successVal = Integer.parseInt(returnVal);		      
			      clientSocket.close();		     
			}
			catch(Exception e)
			{
				e.printStackTrace();			
			}			
	      return successVal;          
		}

		@Override
		public int checkStockLocally(int itemID) {
			int noOfAvailableStock = 0;				
			if(itemHT.containsKey(itemID))  // if Item is available in the store
			{
				noOfAvailableStock = itemHT.get(itemID);
			}
			return noOfAvailableStock;
		}

		@Override
		public String exchange(String customerID, int boughtItemID,int boughtNumber, int desiredItemID, int desiredNumber) 
		{		
			String msg = "";
			File inputFile = null;
			FileReader fileReader = null;          
			int flag = 0;
			int noOfBoughtItem = 0;		
			Semaphore i1 = null,i2=null, c1=null;
			boolean i1lockSet = false,i2lockSet = false, c1lockSet = false;
			boolean canReturnFlag = false;
			boolean buyStatus = false;
			boolean islockedT = false;
			boolean islockedM = false;
			int noOfAvailableItem = 0;
			int noOfReqItem = 0;
			int noOfAvailableItems = 0;
			try 
			{
				if(semaphoreCM.containsKey(customerID))
				{
					c1 = semaphoreCM.get(customerID);
				}
				else
				{
					Semaphore lSema = new Semaphore(1, true);
					semaphoreCM.put(customerID, lSema);
					c1 = lSema;
					
				}
				
				if(c1.tryAcquire(500, TimeUnit.MILLISECONDS))
				{
					c1lockSet = true;
					inputFile = new File(".\\"+customerID+".txt");
					if(inputFile.exists())
					{
						try{
							if(semaphoreHM.containsKey(boughtItemID))
							{
								i1 = semaphoreHM.get(boughtItemID);
								if(i1.tryAcquire(500, TimeUnit.MILLISECONDS))
								{
									i1lockSet = true;
									fileReader = new FileReader(inputFile);            
									BufferedReader br = new BufferedReader(fileReader);
									String line = " ";
								    String [] data;			    					            					            
								    while ((line = br.readLine())!= null)
								    { 			        			        
								        data = line.split("\t"); //split spaces	
								        if(data[0]!=null && !data[0].equalsIgnoreCase("") && Integer.parseInt(data[0]) == (boughtItemID))
								        {
								        	noOfBoughtItem = Integer.parseInt(data[1].toString());
								        	if(noOfBoughtItem >= boughtNumber)
								        	{							        						
								        		canReturnFlag = true;
								        	}	
								        	else
								        	{
								        		msg = "No of items returned ("+boughtNumber+") cannot be more than item bought ("+noOfBoughtItem+")";
								        	}
								        }	
								        							       
								    }
								    
								    br.close();
								    fileReader.close();		
								    if(canReturnFlag)
								    {
								    	// if you can return the item then check for if you can buy the new item
								    	if(semaphoreHM.containsKey(desiredItemID))
								    	{
								    		i2 = semaphoreHM.get(desiredItemID);
								    		if(i2.tryAcquire(500, TimeUnit.MILLISECONDS))
								    		{
								    			i2lockSet = true;
								    			try
												   {
														flag = isItemAvailableInCurrentStore(desiredItemID,desiredNumber);
														if(flag == 1) //item available in the current store in required quantity
														{
															buyStatus = true;								
														}
														else if(flag == 2)//item available in the current store but is less than required quantity
														{
															islockedT = intimateOtherServerRegLock(desiredItemID,0,5002) ;
															islockedM = intimateOtherServerRegLock(desiredItemID,0,5001);
															if(islockedT && islockedM)							
															{
																Hashtable<String ,HashMap<Integer,Integer>> serverItemMap = new Hashtable<String ,HashMap<Integer,Integer>>();;
																noOfAvailableItem = itemHT.get(desiredItemID);
																noOfReqItem = desiredNumber - noOfAvailableItem; 
																int itemsAvailableWithTServer = 0;
																int itemsAvailableWithMServer = 0;
																int totalItemsAvailable = 0;
																serverItemMap = checkStockFromOtherServers(desiredItemID,serverItemMap);
																 java.lang.Object key = null;             
													             for (Enumeration e = serverItemMap.keys(); e.hasMoreElements();) 
													             {
													                 key = e.nextElement();
													                 if(key.equals("Toronto"))
													                 {
														                 for(Integer keys : serverItemMap.get(key).keySet())
														                 {
														                	 
														                	 itemsAvailableWithTServer = itemsAvailableWithTServer + serverItemMap.get(key).get(keys) ;
														                 }
													                 }
													                 else if(key.equals("Montreal"))
													                 {
													                	 for(Integer keys : serverItemMap.get(key).keySet())
														                 {
														                	 
													                		 itemsAvailableWithMServer = itemsAvailableWithMServer + serverItemMap.get(key).get(keys) ;
														                 } 
													                 }
													             }
													             totalItemsAvailable = itemsAvailableWithTServer + itemsAvailableWithMServer;
													             if(totalItemsAvailable >= noOfReqItem)
													             {
													            	 itemHT.remove(desiredItemID);
													            	 if(itemsAvailableWithTServer >= noOfReqItem)
													            	 {
													            		 orderItemFromOtherStore(desiredItemID,noOfReqItem,5002);
													            	 }
													            	 else
													            	 {
													            		 noOfReqItem = noOfReqItem - itemsAvailableWithTServer;
													            		 orderItemFromOtherStore(desiredItemID,itemsAvailableWithTServer,5002);												            				 												            			
													            		 orderItemFromOtherStore(desiredItemID,noOfReqItem,5001);
													            	 }
													            	 buyStatus = true;
														         }
													             else
													             {
													            	 buyStatus = false;
													             }
															}
															else
															{
																msg = "Could not acquire lock for item: "+desiredItemID;
																buyStatus = false;
															}														
														}
														else if(flag == 3) // item not available in the current store
														{
															islockedT = intimateOtherServerRegLock(desiredItemID,0,5002) ;
															islockedM = intimateOtherServerRegLock(desiredItemID,0,5001);
															if(islockedT && islockedM)							
															{
																Hashtable<String ,HashMap<Integer,Integer>> serverItemMap = new Hashtable<String ,HashMap<Integer,Integer>>();;			
																int itemsAvailableWithTServer = 0;
																int itemsAvailableWithMServer = 0;
																int totalItemsAvailable = 0;
																serverItemMap = checkStockFromOtherServers(desiredItemID,serverItemMap);
																java.lang.Object key = null;             
													             for (Enumeration e = serverItemMap.keys(); e.hasMoreElements();) 
													             {
													                 key = e.nextElement();
													                 if(key.equals("Toronto"))
													                 {
														                 for(Integer keys : serverItemMap.get(key).keySet())
														                 {
														                	 
														                	 itemsAvailableWithTServer = itemsAvailableWithTServer + serverItemMap.get(key).get(keys) ;
														                 }
													                 }
													                 else if(key.equals("Montreal"))
													                 {
													                	 for(Integer keys : serverItemMap.get(key).keySet())
														                 {
														                	 
													                		 itemsAvailableWithMServer = itemsAvailableWithMServer + serverItemMap.get(key).get(keys) ;
														                 } 
													                 }
												             }
												             totalItemsAvailable = itemsAvailableWithTServer + itemsAvailableWithMServer;
												             if(totalItemsAvailable >= desiredNumber)
												             {
												            	 itemHT.remove(desiredItemID);
												            	 if(itemsAvailableWithTServer >= desiredNumber)
												            	 {
												            		 orderItemFromOtherStore(desiredItemID,desiredNumber,5002);
												            	 }
												            	 else
												            	 {
												            		 int tempNo = 0;
												            		 tempNo = desiredNumber - itemsAvailableWithTServer;
												            		 orderItemFromOtherStore(desiredItemID,itemsAvailableWithTServer,5002);
												            		 orderItemFromOtherStore(desiredItemID,tempNo,5001);
												            	 }
												            	 buyStatus = true;
												             }
												             else
												             {
												            	 buyStatus = false;
												             }
															}
															else
															{
																msg = "Could not acquire lock for item: "+desiredItemID;
																buyStatus = false;
															}						
													    }
												   }
								    			catch(Exception e)
								    			{
								    				e.printStackTrace();
								    			}							    										    										    										    										    										    										    										    										
								    		}
								    		else
								    		{
								    			msg = "Could not lock the item: " +desiredItemID+".Please try again later";
								    		}
								    	}
								    	
								    	if(buyStatus)
								    	{
								    		//buy the item and also return the item
								    		try
								    		{							    			
													fileReader = new FileReader(inputFile);            
													br = new BufferedReader(fileReader);
													String ouputLine = " ";
												    String [] ouputData;			    
										            File f2 = new File(".\\temp.txt");
										            BufferedWriter out = new BufferedWriter(new FileWriter(f2));
										            boolean toInsert = true;
												    while ((ouputLine = br.readLine())!= null)
												    { 			        			        
												    	ouputData = ouputLine.split("\t"); //split spaces	
												        if(ouputData[0]!=null && !ouputData[0].equalsIgnoreCase("") && Integer.parseInt(ouputData[0]) == (desiredItemID)) // for buying
												        {
												        	noOfBoughtItem = Integer.parseInt(ouputData[1].toString());											        	
											        		noOfBoughtItem = noOfBoughtItem + desiredNumber;								
											        		ouputLine = desiredItemID + "\t" + noOfBoughtItem;
															out.write(ouputLine);
											                out.newLine();
											                toInsert = false;																					        						        		        
												        }
												        else if(ouputData[0]!=null && !ouputData[0].equalsIgnoreCase("") && Integer.parseInt(ouputData[0]) == (boughtItemID)) // for returning
												        {
												        	noOfBoughtItem = Integer.parseInt(ouputData[1].toString());
												        	if(noOfBoughtItem > boughtNumber)//if item to be returned are more than item bought
												        	{
												        		noOfBoughtItem = noOfBoughtItem - boughtNumber;
												        		if(itemHT.containsKey(boughtItemID))
												        		{
												        			noOfAvailableItems = itemHT.get(boughtItemID)!=null?itemHT.get(boughtItemID):0;
																	noOfAvailableItems = noOfAvailableItems + boughtNumber;
																	itemHT.remove(boughtItemID);
																	this.addItem(boughtItemID, noOfAvailableItems);
												        		}
												        		else
												        		{
												        			this.addItem(boughtItemID, boughtNumber);
												        		}
																
												        		ouputLine = boughtItemID + "\t" + noOfBoughtItem;
																out.write(ouputLine);
												                out.newLine();	
												                toInsert = false;
												        	}
												        	else if(noOfBoughtItem == boughtNumber)
												        	{
												        		noOfAvailableItems = itemHT.get(boughtItemID)!=null?itemHT.get(boughtItemID):0;
																noOfAvailableItems = noOfAvailableItems + boughtNumber;
																itemHT.remove(boughtItemID);
																this.addItem(boughtItemID, noOfAvailableItems);
																// delete that item from customer File		
																toInsert = false;
												        	}			 											        												        	
												        }
												        else
												        {
												        	out.write(ouputLine);
											                out.newLine();										               
												        }				       
												    }
												    if(toInsert)
												    {
												    	out.write(desiredItemID + "\t" + desiredNumber);
												    	out.newLine();
												    }
												    br.close();
												    fileReader.close();	
												    out.close();
												    inputFile.delete();
											        f2.renameTo(inputFile);
																							
												msg = desiredNumber +" Item of "+desiredItemID+" bought successfully and "+boughtNumber+" Item of "+boughtItemID+" returned successfully";	
								    		}
								    		catch(Exception e)
								    		{
								    			e.printStackTrace();
								    		}
								    		
								    	}
								    	else
								    	{
								    		msg ="Not enough quantity of item "+desiredItemID+" available to buy.";
								    	}
								    }
								}
								else
								{
									msg = "Could not lock the item: " +boughtItemID+".Please try again later";
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							if(i1lockSet)
							{
								i1.release();
							}
							if(i2lockSet)
							{
								i2.release();
							}
							if(islockedT)
							{
								intimateOtherServerRegLock(desiredItemID, 1, 5002);	
							}
							if(islockedM)
							{
								intimateOtherServerRegLock(desiredItemID, 1, 5001);
							}
						}
						
					}
					else
					{
						msg = "Customer "+customerID+" doesn't exist";
					}
				}
				else
				{
					msg = "Could not lock the customer:" + customerID;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(c1lockSet)
				{
					c1.release();
				}
			}
		logger.info(msg);
		return msg;		
		}


		@Override
		/**
		 * Checks the stock for item entered by the user in the current store and if the method is called by user, it checks item availability from other stores as well
		 * @itemID : item id entered by the user for which stock has to be checked
		 * @serverItemMap : server details if fetched from any other server
		 * @userInvoked : if the method is invoked by user then true else false
		 * @return Hashtable<String ,HashMap<String,Integer>> : serverMap which has item details from all the servers
		 */
		public String checkStock(int itemID) {
			Hashtable<String ,HashMap<Integer,Integer>> serverItemMap = new Hashtable<String, HashMap<Integer, Integer>>();
			String resultString = "";
			HashMap<Integer,Integer> hItemMap = null;		       
			int noOfAvailableStock = 0;				
			Semaphore i1 = null;
			boolean islocked = false;
			boolean islockedT = false;
			boolean islockedM = false;		
								
			if(itemHT.containsKey(itemID))  // if Item is available in the store
			{
				if(semaphoreHM.containsKey(itemID))
				{
					i1 = semaphoreHM.get(itemID);
					try
					{
						if(i1.tryAcquire(500, TimeUnit.MILLISECONDS)) // locking the item to maintain concurrency
						{
							islocked = true;						
						   try
						   {
							    noOfAvailableStock = itemHT.get(itemID);
								hItemMap = new HashMap<Integer,Integer>();
								hItemMap.put(itemID, noOfAvailableStock);
								serverItemMap.put("Vancouver", hItemMap);							
						   }
						   catch(Exception e)
						   {
							   e.printStackTrace();
						   }
						}
						else
						{
							logger.info("Could not acquire lock for item :"+itemID);
						}
					}
					catch(Exception e)
					   {
						   e.printStackTrace();
					   }
					finally
					{
						if(islocked)
						{
							i1.release();
						}					
					}
				}										
			}
			try
			{			
				islockedT = intimateOtherServerRegLock(itemID,0,5002) ;
				islockedM = intimateOtherServerRegLock(itemID,0,5001);
				if(islockedT && islockedM)
				{
					serverItemMap = checkStockFromOtherServers(itemID,serverItemMap);
				}
				else
				{
					logger.info("Could not acquire lock for item :"+itemID);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(islockedT)
				{
					intimateOtherServerRegLock(itemID, 1, 5002);
				}
				if(islockedM)
				{
					intimateOtherServerRegLock(itemID, 1, 5001);
				}
			}
							
			resultString = "--- BEGIN RMHashtable ---\n";
			java.lang.Object key = null;			                  
	        for (Enumeration e = serverItemMap.keys(); e.hasMoreElements();) 
	        {
	            key =  e.nextElement();
	            for(Integer keys : serverItemMap.get(key).keySet())
	            {
	           	 resultString = resultString + "[Server='" + key + "']" + "[Item='" + keys + "']" + "[Available Units='" + serverItemMap.get(key).get(keys) + "']" + "\n";
	            }
	        }
	        resultString = resultString + "--- END RMHashtable ---";
							
			return resultString;
		}
		
		/**
		 * Checks stock from other servers using UDP sockets
		 */
		public Hashtable<String ,HashMap<Integer,Integer>> checkStockFromOtherServers(int itemID,Hashtable<String ,HashMap<Integer,Integer>> serverItemMap) 
		{
			HashMap<Integer,Integer> itemList = null;					
			try
			{
				
				  DatagramSocket clientSocket1 = new DatagramSocket();
			      InetAddress IPAddress = InetAddress.getByName("localhost");
			      byte[] sendData = new byte[1024];
			      byte[] receiveData = new byte[1024];		
			      String sendVal = "checkStockLocally" + "~" + itemID +"~";
			      sendData = sendVal.getBytes();
			      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5002);
			      clientSocket1.send(sendPacket);
			      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket1.receive(receivePacket);
			      String returnVal = new String(receiveData,0,receivePacket.getLength());		      
			      clientSocket1.close();
			      itemList = new HashMap<Integer,Integer>();
			      itemList.put(itemID, Integer.parseInt(returnVal));
			      serverItemMap.put("Toronto",itemList);
				
				
			   
			      DatagramSocket clientSocket2 = new DatagramSocket();
			      IPAddress = InetAddress.getByName("localhost");
			      sendData = new byte[1024];
			      receiveData = new byte[1024];		
			      sendVal = "checkStockLocally" + "~" + itemID +"~";
			      sendData = sendVal.getBytes();
			      sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5001);
			      clientSocket2.send(sendPacket);
			      receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket2.receive(receivePacket);
			      returnVal = new String(receiveData,0,receivePacket.getLength());		      
			      clientSocket2.close();
			      itemList = new HashMap<Integer,Integer>();
			      itemList.put(itemID, Integer.parseInt(returnVal));
			      serverItemMap.put("Montreal",itemList);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
			}
			
			return serverItemMap;
		}
		
		/**
		 * This method intimates the other servers to acquire or release the lock on particular item,as and when required
		 * flag 0 is acquire lock and flag 1 is release lock
		 */
		public boolean intimateOtherServerRegLock(int itemID,int flag,int portNo)
		{				
	        boolean successFlag = false;
			try
			{				
			      DatagramSocket clientSocket = new DatagramSocket();
			      InetAddress IPAddress = InetAddress.getByName("localhost");
			      byte[] sendData = new byte[1024];
			      byte[] receiveData = new byte[1024];		
			      String sendVal = "lockStatus" + "~" + itemID + "~" + flag +"~";
			      sendData = sendVal.getBytes();
			      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portNo);
			      clientSocket.send(sendPacket);
			      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			      clientSocket.receive(receivePacket);
			      String returnVal = new String(receiveData,0,receivePacket.getLength());		      
			      clientSocket.close();
			      if(returnVal.contains("true"))
			      {
			    	  successFlag = true;
			      }
			      else
			      {
			    	  successFlag = false;
			      }		
			}
			catch(Exception e)
			{
				e.printStackTrace();			
			}			
	      return successFlag;          
		}
		
		public String getItemDetails()
		{
			String resultString = "";		
			for (Entry<Integer, Integer> entry : itemHT.entrySet()) 		     
	        {
	       	 resultString = resultString + entry.getKey() + "~" + entry.getValue() + "\n";
	        }              
							
			return resultString;
		}
		public String getItemLockDetails()
		{
			String resultString = "";		
			for (Entry<Integer, Semaphore> entry : semaphoreHM.entrySet()) 		     
	        {
	       	 resultString = resultString + entry.getKey() + "~" + entry.getValue().availablePermits() + "\n";
	        }              
							
			return resultString;
		}

	/**
	 * @param args
	 */
		/**
		 * @param args
		 */	
		public static void main(String[] args) 
		{
			try
		    {  						
				DatagramSocket rmSocket = new DatagramSocket(5007); // create a socket to bind with replica Manager using UDP
				byte[] receiveData = new byte[1024];	
				DatagramPacket receivePacket = null;			
		        ReplicaManagerThread rmThread = new ReplicaManagerThread(rmSocket,receivePacket,receiveData,args[0],true); // arg[0] has Montreal 2 host name
		        rmThread.start();		        
		        
	        	while(true)
	        	{                		            
	            	receivePacket = new DatagramPacket(receiveData, receiveData.length); // wait for packet from replica manager
	        		rmSocket.receive(receivePacket);
	        		if(rmThread.getServerSocketObject()!=null)
	        		{
	        			rmThread.getServerSocketObject().close();
	        		}
	        		if(rmThread.getMultiCastThreadObject().getMultiCastSocketObject()!=null)
	        		{
	        			rmThread.getMultiCastThreadObject().getMultiCastSocketObject().close();
	        		}
	        		if(rmThread.getMultiCastThreadObject()!=null)
	        		{
	        			rmThread.getMultiCastThreadObject().stop();
	        		}
	        		rmThread.stop(); // when packet arrives, stop the thread we started before
	        		rmThread = new ReplicaManagerThread(rmSocket,receivePacket,receiveData,args[0],false); // start another instance of that runnable object   
	        		rmThread.start();
	        	}          
		      }
		      catch (Exception e) 
		      {
		          System.out.println(e);
		          e.printStackTrace();
		      }                
		}

}
