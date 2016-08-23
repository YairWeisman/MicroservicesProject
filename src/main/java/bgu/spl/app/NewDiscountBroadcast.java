package bgu.spl.app;

import bgu.spl.mics.Broadcast;
/**
 * This class represent a discount broadcast. 
 */
public class NewDiscountBroadcast implements Broadcast{

	private DiscountSchedule discount;
	
	/**
	 * @param discount The discount to publish.
	 */
	public NewDiscountBroadcast(DiscountSchedule discount){
		this.discount = discount;
	}

	/**
	 * @return What is the published discount.
	 */
	public DiscountSchedule getDiscount(){
		return discount;
	}
}
