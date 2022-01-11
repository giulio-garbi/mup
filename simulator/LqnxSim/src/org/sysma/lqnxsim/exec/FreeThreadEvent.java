package org.sysma.lqnxsim.exec;

import java.time.Instant;

public class FreeThreadEvent extends Event {
	public FreeThreadEvent(Task task, Instant time) {
		super();
		this.task = task;
		this.time = time;
	}

	public final Task task;
	public final Instant time;
	@Override
	public Instant getTime() {
		return time;
	}
}
