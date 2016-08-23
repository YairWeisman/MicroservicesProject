package bgu.spl.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * This class represent a manager who constrolls the store's invantory.
 */
public class ManagementService extends MicroService {

	private AtomicInteger tick;
	private CountDownLatch cdl;
	private ConcurrentHashMap<String,Integer> shoeOrders;
	private ConcurrentHashMap<String,LinkedBlockingQueue<RestockRequest>> waitingForRestock;
	private DiscountSchedule[] discounts;
	
	/**
	 * Constructor for the manager service. Creates two new lists to follow the shoe orders and the sellers
	 * that are waiting for the shoe.
	 * @param discounts A list of future discounts to publish.
	 * @param cdl CountDownLatch parameter for concurrency matters.
	 */
	public ManagementService(ArrayList<DiscountSchedule> discounts, CountDownLatch cdl) {
		super("manager");
		this.cdl = cdl;
		shoeOrders = new ConcurrentHashMap<String,Integer>();
		waitingForRestock = new ConcurrentHashMap<String,LinkedBlockingQueue<RestockRequest>>();
		this.discounts = new DiscountSchedule[discounts.size()];
		for(int i = 0; i < discounts.size(); i++)
			this.discounts[i] = discounts.get(i);	
		
		for (int i = 0; i < this.discounts.length - 1; i++){
	            int index = i;
	            for (int j = i + 1; j < this.discounts.length; j++)
	                if (this.discounts[j].getTick() < this.discounts[index].getTick())
	                    index = j;
	            DiscountSchedule smallerNumber = this.discounts[index]; 
	            this.discounts[index] = this.discounts[i];
	            this.discounts[i] = smallerNumber;
		}
	}

	/**
	 * Initialize the manager by subscribing him to the timer so he'll know when to send a new discount message,
	 * when he shoukd terminate, also subscribe to the restock requests.
	 */
	@SuppressWarnings("unchecked")
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, timeBroadcast -> {
			this.tick = timeBroadcast.getTick();
			Store store = Store.getInstance();
			for(int i = 0; i < discounts.length; i++){
				if(discounts[i].getTick() == tick.get()){ //If there's a discount schedualed this tick - send a new discount broadcast.
					store.addDiscount(discounts[i].getShoeType(), discounts[i].getAmount());
					sendBroadcast(new NewDiscountBroadcast(discounts[i]));
				}
			}
			if(timeBroadcast.getDuration().get() < timeBroadcast.getTick().get()){
				MessageBus messageBus = MessageBusImpl.getInstance();
				messageBus.unregister(this);
				super.terminate();
			}
		});
		subscribeRequest(RestockRequest.class, req -> {
			//if there's no orders for this type of shoe
			if(!shoeOrders.contains(req.getShoeType())||shoeOrders.get(req.getShoeType()) < waitingForRestock.get(req.getShoeType()).size()){
				ShoeStoreRunner.LOGGER.log(Level.INFO, "manager is sending new manufactoring request for "+req.getShoeType());
				shoeOrders.put(req.getShoeType(),new Integer((tick.get()) % 5 + 1));
				boolean success = sendRequest(new ManufacturingOrderRequest<Recipt>(req.getShoeType(),(tick.get()) % 5 + 1,tick.get()), result -> {
					if(result != null && result instanceof Recipt){
						Store store = Store.getInstance();
						store.file((Recipt)result);
						//for all the first requesters
						if (!waitingForRestock.contains(((Recipt)result).getShoeType()))
							waitingForRestock.put(((Recipt)result).getShoeType(),new LinkedBlockingQueue<RestockRequest>());
						if(((Recipt)result).getAmountSold() <= waitingForRestock.get(((Recipt)result).getShoeType()).size()){
							for(int i = 0; i < ((Recipt)result).getAmountSold(); i++){
								RestockRequest request = waitingForRestock.get(((Recipt)result).getShoeType()).remove();
								complete(request, true);
							}
							shoeOrders.put(((Recipt)result).getShoeType(),shoeOrders.get(((Recipt)result).getShoeType()) - ((Recipt)result).getAmountSold());
						}
						else{
							int restockWaitingSize=waitingForRestock.get(((Recipt)result).getShoeType()).size();
							for(int i = 0; i < restockWaitingSize; i++){
								RestockRequest request = waitingForRestock.get(((Recipt)result).getShoeType()).remove();
								complete(request, true);
							}
							store.add(((Recipt)result).getShoeType(), ((Recipt)result).getAmountSold()-restockWaitingSize);
							shoeOrders.remove(((Recipt)result).getShoeType());
						}
						//removes from the "waitingForRestock" list and send him ResualtCompleted
					}
					
				});
				if(success){
					ShoeStoreRunner.LOGGER.log(Level.INFO, "manufactoring completed successfully");
				}
				else {
					while(!waitingForRestock.get(req.getShoeType()).isEmpty()){
						RestockRequest request = waitingForRestock.get(req.getShoeType()).remove();
						complete(request, false);
					}
				}
			}
		});
		cdl.countDown();
	}
}
