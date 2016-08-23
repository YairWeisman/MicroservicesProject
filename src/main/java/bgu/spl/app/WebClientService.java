package bgu.spl.app;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * This class represent a costumer.
 */
public class WebClientService extends MicroService {
	
	private AtomicInteger tick;
	private CountDownLatch cdl;
	private ConcurrentHashMap<Integer,ArrayList<PurchaseSchedule>> purchases;
	private HashSet<String> wishList;
	
	/**
	 * Instantiates a new web client service.
	 *
	 * @param name the name of the costumer.
	 * @param purchases A list of his future purchases.
	 * @param wishList A list of shoe the costumer wants to buy only on discount.
	 * @param cdl CountDownLatch parameter for concurrency matters.
	 */
	public WebClientService(String name, List<PurchaseSchedule> purchases, List<String> wishList, CountDownLatch cdl){
		super(name);
		this.cdl = cdl;
		this.purchases = new ConcurrentHashMap<Integer,ArrayList<PurchaseSchedule>>();
		this.wishList = new HashSet<String>();
		for(int i = 0; i < purchases.size(); i++){
			if(this.purchases.get(purchases.get(i).getTick()) == null)
				this.purchases.put(purchases.get(i).getTick(), new ArrayList<PurchaseSchedule>());
			PurchaseSchedule purchase = purchases.get(i);
			this.purchases.get(purchase.getTick()).add(purchase);
		}
		for(int i = 0; i < wishList.size(); i++){
			String wish = wishList.get(i);
			this.wishList.add(wish);
		}
	}

	/* 
	 * Initialize the costumer service with the subscription to the tick broadcasts, new discounts broadcasts
	 */
	@SuppressWarnings("unchecked")
	public void initialize() {
		subscribeBroadcast(TickBroadcast.class, timeBroadcast -> {
			this.tick = timeBroadcast.getTick();
			checkForRequest(tick.get());
			//checks if he's finished with his shopping OR the time is off
			if((timeBroadcast.getDuration().get() < timeBroadcast.getTick().get())){
				MessageBus messageBus = MessageBusImpl.getInstance();
				messageBus.unregister(this);
				super.terminate();
			}
		});
		subscribeBroadcast(NewDiscountBroadcast.class, discount ->{
			String shoeTypeOnDiscount = discount.getDiscount().getShoeType();
			boolean shoeInWishList = wishList.contains(shoeTypeOnDiscount);
			if(shoeInWishList){
				PurchaseSchedule order = new PurchaseSchedule(shoeTypeOnDiscount, tick.get());
				sendRequest(new PurchaseOrderRequest(order,true), result -> {
					if(result != null){
						wishList.remove(shoeTypeOnDiscount);
						ShoeStoreRunner.LOGGER.log(Level.INFO, "Buying the shoe from the wish list has succeded");
					}
					else{
						ShoeStoreRunner.LOGGER.log(Level.INFO, "Buying the shoe from the wish list has failed");
					}
				});
			}
		});
		cdl.countDown();
	}
	
	/**
	 * Checks if there are orders in this tick.
	 * @param tick The current tick
	 */
	@SuppressWarnings("unchecked")
	public void checkForRequest(int tick){
		ArrayList<PurchaseSchedule> requests = purchases.get(tick);
		if(requests != null){
			for(int i = 0; i < requests.size(); i++){
				PurchaseSchedule order = requests.get(i);
				sendRequest(new PurchaseOrderRequest(order, false), result -> {
					purchases.get(tick).remove(order);
					if(purchases.get(tick).isEmpty())
						purchases.remove(purchases.get(tick));
					if(result != null){
						String shoeType = order.getShoeType();
						wishList.remove(shoeType);	
					}
				});
			}
		}
	}
}
