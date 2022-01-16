package org.sysma.lqnxsim.exec;

import java.time.Instant;

public abstract class Event implements Comparable<Event> {
	public abstract Instant getTime();
	
	@Override
	public int compareTo(Event o) {
		return this.getTime().compareTo(o.getTime());
	}
}
