package bgu.spl.mics.impl;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import bgu.spl.app.ShoeStoreRunner;
import bgu.spl.mics.*;

/**
 * The framework that the program is based on. 
 */
public class MessageBusImpl implements MessageBus{
	
	private ConcurrentHashMap<String,ArrayList<MicroService>> broadcastsPool;
	private ConcurrentHashMap<String,RoundRobinList> requestsPool;
	private ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>> microServicesPool;
	private ConcurrentHashMap<Request,MicroService> requestToMicroService;
	private Object syncLocker = new Object();
	
	private static class MessageBusHolder{
		private static MessageBusImpl messageBusInstance = new MessageBusImpl();
	}
	
	private MessageBusImpl(){
		broadcastsPool = new ConcurrentHashMap<String,ArrayList<MicroService>>();
		requestsPool = new ConcurrentHashMap<String,RoundRobinList>();
		microServicesPool = new ConcurrentHashMap<MicroService,LinkedBlockingQueue<Message>>();
		requestToMicroService = new ConcurrentHashMap<Request,MicroService>();
	}
	
	/**
	 * This is a singleton constructor.
	 * @return An instance of the messagebus implimintation.
	 */
	public static MessageBusImpl getInstance() {
		return MessageBusHolder.messageBusInstance;
	}
	
	/* 
	 * @return True if and only all the services un-subscribed.
	 */
	public boolean isFinished() {
		return microServicesPool.isEmpty();
	}
	
	/**
	 * Make a service to get specific request.
	 * @param type The request type.
	 * @param m The service to subscribe.
	 */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) throws NullPointerException{
		synchronized (requestsPool){
			if(requestsPool.get(type.getName()) != null){
				requestsPool.get(type.getName()).add(m);
			}
			else {
				requestsPool.put(type.getName(), new RoundRobinList());
				requestsPool.get(type.getName()).add(m);
			}
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB subscribed request "+type.getName()+" to "+m.getName());
		}
		
	}

	/**
	 * Make a service to get specific broadcast.
	 * @param type The broadcast type.
	 * @param m The service to subscribe.
	 */
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) throws NullPointerException {
		synchronized (broadcastsPool){
			if(broadcastsPool.get(type.getName()) != null)
				broadcastsPool.get(type.getName()).add(m);
			else {
				broadcastsPool.put(type.getName(),new ArrayList<MicroService>());
				broadcastsPool.get(type.getName()).add(m);
			}
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB subscribed broadcast "+type.getName()+" to "+m.getName());
		}
	}

	/**
	 * Connects the service that sent the request with his result.
	 * @param r The request
	 * @param result The result (usually a receipt). 
	 */
	public <T> void complete(Request<T> r, T result) {
		MicroService m = requestToMicroService.get(r);
		//requestToMicroService.remove(r);
		RequestCompleted<T> resultMessage = new RequestCompleted<T>(r,result);
		if(microServicesPool.get(m)!=null){
			microServicesPool.get(m).add(resultMessage);
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB sent request completed to "+m.getName());
		}
	}
	
	/**
	 * @param req A request.
	 * @return The service by the request.
	 */
	public MicroService getRequester(Request req){
		MicroService m = requestToMicroService.get(req);
		return m;
	}

	/**
	 * Sends a broadcast the the ones subscribe to it.
	 * @param b A broadcast.
	 */
	public void sendBroadcast(Broadcast b) {
		if (broadcastsPool.get(b.getClass().getName()) == null)
			ShoeStoreRunner.LOGGER.log(Level.INFO, "No one cares about this broadcast");
		else{
			for(int i = 0; i < broadcastsPool.get(b.getClass().getName()).size(); i++){ 
				if(microServicesPool.get(broadcastsPool.get(b.getClass().getName()).get(i))!=null)
				microServicesPool.get(broadcastsPool.get(b.getClass().getName()).get(i)).add(b);
			}
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB sent broadcast of type "+b.getClass().getName());
		}
	}

	/**
	 * Sends a request the the ones subscribe to it.
	 * @param r A request.
	 * @param requester The service who sent the request.
	 */
	public boolean sendRequest(Request<?> r, MicroService requester) {
		requestToMicroService.put(r, requester);
		if(requestsPool.get(r.getClass().getName()).isEmpty()){
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB - no one is subscribed to this type of request");
			return false;
		}
		else {
			MicroService service = requestsPool.get(r.getClass().getName()).getService();
		if(microServicesPool.get(service)!=null)
				microServicesPool.get(service).add(r);
		ShoeStoreRunner.LOGGER.log(Level.INFO, "MB added request "+r.getClass().getName()+" to "+service.getName());
		return true;
		}
	}

	/**
	 * Register a service to the store.
	 * @param m The service to register.
	 */
	public void register(MicroService m) {
		synchronized (m) {
			microServicesPool.put(m, new LinkedBlockingQueue<Message>());
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB has registered "+m.getName());
		}
		
	}

	/**
	 * Unregister a service to the store.
	 * @param m The service to unregister.
	 */
	public synchronized void unregister(MicroService m) {
		synchronized (m) {
			requestsPool.remove(m);
			broadcastsPool.remove(m);
			microServicesPool.remove(m);
			ShoeStoreRunner.LOGGER.log(Level.INFO, "MB has un-registered "+m.getName());
		}
		
	}

	/**
	 * @return A message when it's arrived to the service queue.
	 */
	public Message awaitMessage(MicroService m) throws InterruptedException, IllegalStateException {
		if(microServicesPool.get(m) == null)
			throw new IllegalStateException();
		return microServicesPool.get(m).take(); 
	}
}
