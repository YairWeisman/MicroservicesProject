package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import bgu.spl.app.Store.BuyResult;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * This class represnt a seller of the store.
 */
public class SellingService extends MicroService {
	
	private AtomicInteger tick;
	private CountDownLatch cdl;
	/**
	 * A constructor of a seller.
	 * @param name The name of the seller in the form of: "seller*number*" (for exampel: seller2).
	 * @param cdl CountDownLatch parameter for concurrency matters.
	 */
	public SellingService(String name, CountDownLatch cdl){
		super(name);
		this.cdl = cdl;
	}
	/**
	 * Initialize the seller by subscribing him to the tick broadcast so he'll know when to terminate, 
	 * also to the purchase order requests to serve the costumer. 
	 */
	@SuppressWarnings({ "unchecked"})
	public void initialize() {
		subscribeBroadcast(TickBroadcast.class, timeBroadcast -> {
			this.tick = timeBroadcast.getTick();
			if(timeBroadcast.getDuration().get() < timeBroadcast.getTick().get()){
				MessageBus messageBus = MessageBusImpl.getInstance();
				messageBus.unregister(this);
				super.terminate();
			}
		});
		subscribeRequest(PurchaseOrderRequest.class, req -> {
			String shoeType = req.getPurchaseSchedule().getShoeType();
			boolean onlyDiscount = req.getOnDiscount();
			Store store = Store.getInstance();
			BuyResult buyResult = store.take(shoeType, onlyDiscount);
			;
			switch (buyResult) {
				case NOT_IN_STOCK:
					ShoeStoreRunner.LOGGER.log(Level.INFO, "service "+this.getName()+" sent restock order");
					RestockRequest r = new RestockRequest(shoeType,tick.get());
					sendRequest(r , result -> {
						if (((RequestCompleted<Boolean>)result).getResult() == false){
							this.complete(req, null);
						}
						else {
							MessageBus messageBus = MessageBusImpl.getInstance();
							this.complete(req, new Recipt(this.getName(),messageBus.getRequester(req).getName(),req.getPurchaseSchedule().getShoeType(),onlyDiscount,tick.get(),req.getPurchaseSchedule().getTick(),1));
						}
					});
					break;
				case NOT_ON_DISCOUNT:
					this.complete(req, null);
					break;
				case REGULAR_PRICE:
					MessageBus messageBus = MessageBusImpl.getInstance();
					String seller = this.getName();
					String costumer = messageBus.getRequester(req).getName();
					String shoeType1 = req.getPurchaseSchedule().getShoeType();
					boolean dicount = false;
					int issuedTick = tick.get();
					int requestTick = req.getPurchaseSchedule().getTick();
					int amountSold = 1;
					Recipt recipt= new Recipt(seller,costumer,shoeType1,dicount,issuedTick,requestTick,amountSold);
					store.file(recipt);
					this.complete(req, new Recipt(seller,costumer,shoeType1,dicount,issuedTick,requestTick,amountSold));
					break;
				case  DISCOUNTED_PRICE:
					MessageBus messageBus2 = MessageBusImpl.getInstance();
					String costumer2 = messageBus2.getRequester(req).getName();
					Recipt recipt1 = new Recipt(this.getName(),costumer2,req.getPurchaseSchedule().getShoeType(),true,tick.get(),req.getPurchaseSchedule().getTick(),1);
					store.file(recipt1);
					this.complete(req, new Recipt(this.getName(),costumer2,req.getPurchaseSchedule().getShoeType(),true,tick.get(),req.getPurchaseSchedule().getTick(),1));
					break;
			}
		});
		cdl.countDown();
	}
}
