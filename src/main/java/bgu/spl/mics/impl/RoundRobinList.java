package bgu.spl.mics.impl;
import java.util.concurrent.*;
import java.util.*;
import java.lang.*;
import bgu.spl.mics.*;

/**
 * A round robin class implemintation.
 */
public class RoundRobinList extends LinkedList<MicroService> {
	
	private int index;
	
	/**
	 * Instantiates a new round robin list.
	 */
	public RoundRobinList(){
		super();
		/*this.currentService = null;*/
		this.index = 0;
	}

	/**
	 * Gets the service.
	 * @return the service
	 */
	public synchronized MicroService getService(){
		MicroService ans = null;
		/*if(size() > 0){
			ans = get(index);*/
			//if the list is not empty after taking one off
			if(size() != 0){
				ans = get(index);
				index = (index + 1) % size();
			}
		return ans;
	}
	
	/**
	 * Removes the service.
	 * @param m the service.
	 */
	public synchronized void remove(MicroService m) {
		//if the list isn't empty and 'm' is in the list
		if(size() != 0 && indexOf(m) != (-1)){
			int mSIndex = indexOf(m);
			remove(m);
			if(index > mSIndex)
				index--;
		}
	}
	
	/**
	 * Adds the service to the round robin list.
	 * @param m The service.
	 * @return True if the adding was successful.
	 */
	public synchronized boolean add(MicroService m){
		return super.add(m);
	}
}
