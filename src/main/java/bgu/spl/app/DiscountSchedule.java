package bgu.spl.app;
/**
 * This class represent a future shoe discount. 
 */
public class DiscountSchedule {
	
	private final String shoeType;
	private final int amount;
	private int tick;
	
	/** 
	 * Constructor of a future shoe discount.
	 * @param shoeType What kind of shoe has the discount.
	 * @param tick In what tick to publish this discount.
	 * @param amount How many shoe to of this kind to make in discount.
	 */
	public DiscountSchedule(String shoeType, int tick, int amount){
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}
	
	/**
	 * @return The discount shoe type.
	 */
	public String getShoeType(){
		return shoeType;
	}
	
	/**
	 * @return The tick to broadcast the discount.
	 */
	public int getTick(){
		return tick;
	}

	/**
	 * @return The amount of shoes for discount.
	 */
	public int getAmount(){
		return amount;
	}
}
