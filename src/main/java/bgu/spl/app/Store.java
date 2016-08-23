package bgu.spl.app;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
import java.util.logging.Level;
/**
 * This class represent the storage of the store and the reciepts.
 */
public class Store {

	private ConcurrentHashMap<String, ShoeStorageInfo> stock;
	private LinkedList<Recipt> recipts;
	
	private Store(){
		this.stock = new ConcurrentHashMap<String, ShoeStorageInfo>();
		this.recipts = new LinkedList<Recipt>();		
	}	
	
	private static class StoreHolder{
		private static Store storeInstance = new Store();
	}
	/**
	 * This is a singleton constructor.
	 * @return An instance of the store's storage.
	 */
	public static Store getInstance() {
		return StoreHolder.storeInstance;
	}
	/**
	 * An enum class represent optional buying result.
	 */
	public enum BuyResult{
		NOT_IN_STOCK , 
		NOT_ON_DISCOUNT , 
		REGULAR_PRICE , 
		DISCOUNTED_PRICE
	}
	
	/**
	 * Initialize the store storage with the items provided.
	 * @param storage A list of the shoe supposed to be when the store opens. 
	 */
	public synchronized void load(ShoeStorageInfo[] storage){
		for(int i = 0; i < storage.length; i++){
			stock.put(storage[i].getName(), storage[i]);
		}
		ShoeStoreRunner.LOGGER.log(Level.INFO, "Store has been loaded");
	}
	
	/**
	 * Trying to take a shoe from the storage.
	 *
	 * @param shoeType The shoe type to take.
	 * @param onlyDiscount true if and only the buyer wants to buy this shoe on discount.
	 * @return the An enum of buy result.
	 */
	public synchronized BuyResult take(String shoeType, boolean onlyDiscount){	
		ShoeStoreRunner.LOGGER.log(Level.INFO, "sellers is trying to take an item");
		boolean isInStock = false;
		boolean itemDiscountAvailable = false;
		if(stock.get(shoeType) != null && stock.get(shoeType).getAmountOnStorage() > 0){
			isInStock = true;
			if(stock.get(shoeType).getDiscountedAmount() > 0)
				itemDiscountAvailable = true;
		}
		//Not in stock:
		if(!isInStock)
			return BuyResult.NOT_IN_STOCK;
		//In stock:
		if(onlyDiscount){
			if(itemDiscountAvailable){
				stock.get(shoeType).setAmountOnStorage(stock.get(shoeType).getAmountOnStorage()-1);
				stock.get(shoeType).setDiscountedAmount(stock.get(shoeType).getDiscountedAmount()-1);
				return BuyResult.DISCOUNTED_PRICE;
			}
			else return BuyResult.NOT_ON_DISCOUNT;
		}
		//Not "only discount":
		else{
			if(itemDiscountAvailable){
				stock.get(shoeType).setAmountOnStorage(stock.get(shoeType).getAmountOnStorage()-1);
				stock.get(shoeType).setDiscountedAmount(stock.get(shoeType).getDiscountedAmount()-1);
				return BuyResult.DISCOUNTED_PRICE;
			}
			else{
				stock.get(shoeType).setAmountOnStorage(stock.get(shoeType).getAmountOnStorage()-1);
				return BuyResult.REGULAR_PRICE;
			}
		}
	} 
	
	/**
	 * Adds a shoe to the storage.
	 *
	 * @param shoeType The shoe type to add.
	 * @param amount The amount to add.
	 */
	public synchronized void add(String shoeType, int amount){
		ShoeStoreRunner.LOGGER.log(Level.INFO, "manager is adding an item: "+shoeType);
		if(stock.containsKey(shoeType))
			stock.get(shoeType).setAmountOnStorage(stock.get(shoeType).getAmountOnStorage() + amount);
		else
			stock.put(shoeType, new ShoeStorageInfo(shoeType,amount,0));
	}
	
	/**
	 * Adds discount on a shoe type.
	 *
	 * @param shoeType The shoe type that's on discount.
	 * @param amount The amount to sell in discount.
	 */
	public synchronized void addDiscount(String shoeType ,int amount){
		ShoeStoreRunner.LOGGER.log(Level.INFO, "manager is adding a discount on item: "+shoeType);
		if(stock.containsKey(shoeType))
			stock.get(shoeType).setDiscountedAmount(stock.get(shoeType).getDiscountedAmount()+amount);
		else
			stock.put(shoeType, new ShoeStorageInfo(shoeType,0,amount));
	}
	
	/**
	 * File a receipt. 
	 *
	 * @param recipt The recipt.
	 */
	public void file(Recipt recipt){
		recipts.addFirst(recipt);
		ShoeStoreRunner.LOGGER.log(Level.INFO, "manager filed a recipt");
	}
	
	/**
	 * Prints the receipts.
	 */
	public void print(){
		ShoeStoreRunner.LOGGER.log(Level.INFO, "printing recipts: ");
		for (String key : stock.keySet()) 
			ShoeStoreRunner.LOGGER.log(Level.INFO, "Shoe type: "+stock.get(key).getName()+" Amount on storage: "+stock.get(key).getAmountOnStorage()+" Discounted amount: "+stock.get(key).getDiscountedAmount());
		ShoeStoreRunner.LOGGER.log(Level.INFO, "Num of Recipts: " +recipts.size());
	}
}
