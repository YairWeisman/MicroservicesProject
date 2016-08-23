package bgu.spl.app;

import bgu.spl.mics.Request;
/**
 * This class represnt a shoe order that a client sends to a seller.
 */
public class PurchaseOrderRequest implements Request{
	
	private PurchaseSchedule purchaseOrder;
	private boolean onlyDiscounted;
	/**
	 * A constructor of an order. 
	 * @param purchaseOrder What to order {@link #PurchaseSchedule} to sent the seller. 
	 * @param onlyDiscountedTrue if and only the client want to buy this shoe only on discount.
	 */
	public PurchaseOrderRequest(PurchaseSchedule purchaseOrder,Boolean onlyDiscounted){
		this.purchaseOrder = purchaseOrder;
		this.onlyDiscounted = onlyDiscounted;
	}
	/**
	 * @return The {@link #PurchaseSchedule}.
	 */
	public PurchaseSchedule getPurchaseSchedule(){
		return purchaseOrder;
	}
	/**
	 * @return Whether the client wants to bu this shoe only on discount.
	 */
	public boolean getOnDiscount(){
		return onlyDiscounted;
	}
}
