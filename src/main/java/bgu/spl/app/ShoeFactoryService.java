package bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;
/**
 * This class represent a shoes factory.
 */
public class ShoeFactoryService extends MicroService {
	private AtomicInteger tick;
	private LinkedBlockingQueue<ManufacturingOrderRequest> queue;	//A queue for follow the requests.
	private int amountCounter;
	private CountDownLatch cdl;
	private boolean workedToday;
	/**
	 * A constructor of a shoe factory.
	 * @param name The name of the factory in a form of "factory*number*" ("factory2" for example)/
	 * @param cdl CountDownLatch parameter for concurrency matters.
	 */
	public ShoeFactoryService(String name, CountDownLatch cdl){
		super(name);
		this.cdl = cdl;
		this.queue = new LinkedBlockingQueue<ManufacturingOrderRequest>();
		amountCounter = 0;
		this.workedToday = false;
	}
	/**
	 * Initialized the factory by subscribing it to the tick broadcast so it'll know when to manufacture
	 * a shoe, and when to teminate. Also subscribing to manufacturing order requests to know what to make.
	 */
	public void initialize() {
		subscribeBroadcast(TickBroadcast.class, timeBroadcast -> {
			this.tick = timeBroadcast.getTick();
			workedToday = false;
			manufacture();
			if(timeBroadcast.getDuration().get() < timeBroadcast.getTick().get()){
				MessageBus messageBus = MessageBusImpl.getInstance();
				messageBus.unregister(this);
				terminate();
			}
		});
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			try {
				queue.put(req);
				manufacture();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		cdl.countDown();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void manufacture(){
		ManufacturingOrderRequest manReq = queue.peek();
		if(manReq != null && !workedToday){
			if(amountCounter < manReq.getAmount()){
				amountCounter++;
			}
			if (amountCounter == manReq.getAmount()){
				amountCounter = 0;
				complete(manReq, new Recipt(this.getName(),"Store",manReq.getShoeType(),false,tick.get(),manReq.getRequestTick(),manReq.getAmount()));
				queue.remove();
			}
		}
		workedToday = true;
	}
}
