package bgu.spl.app;

import bgu.spl.mics.Request;
/**
 *This class represent manufactoring order request. 
 * @param <Recipt> the result of the request is a reciept.
 */
public class ManufacturingOrderRequest<Recipt> implements Request {

	private String shoeType;
	private int amount;
	private int requestTick;
	/**
	 * A constructor of a manufactoring request.
	 * @param shoeType What kind of shoe to manufacture.
	 * @param amount How many of this shoe to manufacture. 
	 * @param requestTick In what tick did the order genarated.
	 */
	public ManufacturingOrderRequest(String shoeType,int amount,int requestTick){
		this.shoeType = shoeType;
		this.amount = amount;
		this.requestTick = requestTick;
	}
	/**
	 * @return The shoe type to manufacture. 
	 */
	public String getShoeType(){
		return shoeType;
	}
	/**
	 * @return  How many of this shoe to manufacture.
	 */
	public int getAmount(){
		return amount;
	}
	/**
	 * @return The tick the order created.
	 */
	public int getRequestTick(){
		return requestTick;
	}
}
