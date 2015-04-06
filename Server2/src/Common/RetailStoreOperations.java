package Common;

public interface RetailStoreOperations {
	
	  String itemReturned (String customerID, int itemID, int noOfReturnedItem);
	  int isItemAvailableInCurrentStore (int itemID, int noOfReqItem);
	  boolean createLock (int itemId);
	  boolean releaseLock (int itemId);
	  String buy (String customerID, int itemID, int numberOfItem);
	  int checkStockLocally (int itemID);
	  String exchange (String customerID, int boughtItemID, int boughtNumber, int desiredItemID, int desiredNumber);
	  String checkStock (int itemID);
	  String getItemDetails();
	  String getItemLockDetails();
}
