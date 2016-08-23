package bgu.spl.app;

import bgu.spl.mics.Request;
/**
 * This class represnt a restock request for the store.
 */
public class RestockRequest implements Request {
	private String shoeType;
	private int tick;
	/**
	 * A cosntructor of a restock request.
	 * @param shoeType What shoe to manufacture.
	 * @param tick When did the request was made.
	 */
	public RestockRequest(String shoeType, int tick){
		this.shoeType = shoeType;
		this.tick = tick;
	}
	/**
	 * @return The shoe type to manufacture.
	 */
	public String getShoeType() {
		return shoeType;
	}
	/**
	 * @returnThe tick that the order has benn sent.
	 */
	public int getTick() {
		return tick;
	}
}
