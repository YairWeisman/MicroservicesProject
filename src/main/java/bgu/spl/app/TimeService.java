package bgu.spl.app;

import bgu.spl.mics.MicroService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * This class represent a timer who sends tick broadcasts.
 */
public class TimeService extends MicroService {
	
	private final static String name = "timer";
	private Timer timer;
	private AtomicInteger speed;
	private AtomicInteger duration;
	private AtomicInteger tick;
	private CountDownLatch cdl;
	
	/**
	 * Instantiates a new time service.
	 *
	 * @param speed the speed of each tick.
	 * @param duration How many ticks before termination.
	 * @param cdl CountDownLatch parameter for concurrency matters.
	 */
	public TimeService(AtomicInteger speed, AtomicInteger duration, CountDownLatch cdl) {
		super(name);
		this.timer = new Timer();
		this.speed = speed;
		this.duration = duration;
		this.tick = new AtomicInteger(1);
		this.cdl = cdl;
	}
	
	/**
	 * A TimerTask class.
	 */
	public class BroadcastTheTime extends TimerTask{
		
		/* 
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			ShoeStoreRunner.LOGGER.log(Level.INFO, "tick number "+tick);
			sendBroadcast(new TickBroadcast(tick,duration));
			tick.incrementAndGet();
			if(tick.get() > duration.get()){
				timer.cancel();
				ShoeStoreRunner.LOGGER.log(Level.INFO, "timer canceled");
				Store store = Store.getInstance();
				store.print();
			}
		};
	}
	
	/* 
	 * Initialize the timer when the count down comes to 0.
	 */
	protected void initialize(){
		try{
			cdl.await();
		} catch(InterruptedException e) {
			return;
		}
		timer.scheduleAtFixedRate(new BroadcastTheTime(), 0, speed.get());
	}
}
