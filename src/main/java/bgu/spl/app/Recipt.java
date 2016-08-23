package bgu.spl.app;
/**
 * This class represent a reciept, for the client or the store. 
 */
public final class Recipt {

	private final String seller;
	private final String customer;
	private final String shoeType;
	private final boolean discount;
	private final int issuedTick;
	private final int requestTick;
	private final int amountSold;
	/**
	 * A constructor of a reciept. 
	 * @param seller Who sold the item (seller/factory).
	 * @param customer Who got the item (client/store).
	 * @param shoeType The kind of shoe that has benn sold. 
	 * @param discount Whether the shoe was on discount or not.
	 * @param issuedTick When (what tick) the reciept has been made.
	 * @param requestTick When the request was sended. 
	 * @param amountSold How many shoe sold.
	 */
	public Recipt(String seller,String customer,String shoeType,boolean discount,int issuedTick,int requestTick,int amountSold){
		this.seller=seller;
		this.customer=customer;
		this.shoeType=shoeType;
		this.discount=discount;
		this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;
	}
	/**
	 * @return The seller who sold the shoe.
	 */
	public String getSeller() {
		return seller;
	}
	/**
	 * @return Who order the shoe.
	 */
	public String getCustomer() {
		return customer;
	}
	/**
	 * @return What shoe has benn ordered.
	 */
	public String getShoeType() {
		return shoeType;
	}
	/**
	 * @return True if and only the shoe was on discount.
	 */
	public boolean isDiscount() {
		return discount;
	}
	/**
	 * @return When (what tick) the reciept has been made.
	 */
	public int getIssuedTick() {
		return issuedTick;
	}
	/**
	 * @return When the request was sended. 
	 */
	public int getRequestTick() {
		return requestTick;
	}
	/**
	 * @return How many shoe sold. 
	 */
	public int getAmountSold() {
		return amountSold;
	}
}
