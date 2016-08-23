package bgu.spl.app;
/**
 * This class represent the inventory of a single shoe in the store.
 */
public class ShoeStorageInfo {

	private final String shoeType;
	private int amountOnStorage;
	private int discountedAmount;
	/**
	 * A constructor for a single shoe storage information.
	 * @param shoeType The shoe type.
	 * @param amountOnStorage How many of this shoe type are in stock.
	 * @param discountedAmount How many of these shoes are in discount.
	 */
	public ShoeStorageInfo(String shoeType,int amountOnStorage,int discountedAmount){
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = discountedAmount;
	}
	/**
	 * @return The shoe type.
	 */
	public String getName(){
		return shoeType;
	}
	/**
	 * @return The amount of the shoe in storage.
	 */
	public int getAmountOnStorage(){
		return amountOnStorage;
	}
	/**
	 * @return The amount of the shoes that are on discount.
	 */
	public int getDiscountedAmount(){
		return discountedAmount;
	}
	/**
	 * Updateds the amount of a specific shoe in storage. 
	 * @param amount The updated amount.
	 */
	public void setAmountOnStorage(int amount){
		amountOnStorage = amount;
	}
	/**
	 * Updateds the amount of shoe in dicount of a specific shoe in storage. 
	 * @param amount The updated amount.
	 */
	public void setDiscountedAmount(int amount){
		discountedAmount = amount;
	}
}
