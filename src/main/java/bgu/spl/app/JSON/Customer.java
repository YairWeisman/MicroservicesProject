package bgu.spl.app.JSON;

import bgu.spl.app.PurchaseSchedule;

public class Customer {
	String name;
	String[] wishList;
	PurchaseSchedule[] purchaseSchedule;
	public String getName() {
		return name;
	}
	public String[] getWishList() {
		return wishList;
	}
	public PurchaseSchedule[] getPurchaseSchedule() {
		return purchaseSchedule;
	}
	
	
}
