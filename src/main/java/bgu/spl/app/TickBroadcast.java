package bgu.spl.app;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.Broadcast;


/**
 * This class represent a tick broadcast.
 */
public class TickBroadcast implements Broadcast{
	
	private AtomicInteger tick;
	private AtomicInteger duration;

	/**
	 * Instantiates a new tick broadcast.
	 *
	 * @param tick The tick
	 * @param duration The duration
	 */
	public TickBroadcast(AtomicInteger tick, AtomicInteger duration){
		this.tick = tick;
		this.duration = duration;
	}

	/**
	 * Gets the tick.
	 *
	 * @return the tick
	 */
	public AtomicInteger getTick() {
		return tick;
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public AtomicInteger getDuration() {
		return duration;
	}
}
