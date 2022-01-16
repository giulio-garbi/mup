package org.sysma.lqnxsim.exec;

import java.time.Instant;

import org.sysma.lqnxsim.exec.Task.ExternalCall;
import org.sysma.lqnxsim.model.Activity;

public class CallEvent extends Event {
	public CallEvent(Task task, ExternalCall<?> client, Instant startTime, Activity activity, int which) {
		super();
		this.task = task;
		this.client = client;
		this.startTime = startTime;
		this.activity = activity;
		this.which = which;
	}
	public final Task task;
	public final Task.ExternalCall<?> client;
	public final Instant startTime;
	public final Activity activity;
	public final int which;
	
	@Override
	public Instant getTime() {
		return startTime;
	}
}
