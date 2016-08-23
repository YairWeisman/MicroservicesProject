package bgu.spl.app;

/**
 * This class represnt a future shoe order.
 */
public class PurchaseSchedule {

	private final String shoeType;
	private int tick;
	/**
	 * A constructor of a future shoe order.
	 * @param shoeType What shoe to order.
	 * @param tick When to order this shoe.
	 */
	public PurchaseSchedule(String shoeType,int tick){
		this.shoeType = shoeType;
		this.tick = tick;
	}
	/**
	 * @return The shoe to order.
	 */
	public String getShoeType(){
		return shoeType;
	}
	/**
	 * @return The tick that the order should be made.
	 */
	public int getTick(){
		return tick;
	}
}
