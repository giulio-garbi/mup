package org.sysma.lqnxsim.exec;

import java.time.Instant;

import org.sysma.lqnxsim.exec.Task.ExternalCall;
import org.sysma.lqnxsim.model.Activity;

public class BusyEvent extends Event {
	public BusyEvent(Task task, ExternalCall<?> client, Instant endTime, Activity activity) {
		super();
		this.task = task;
		this.client = client;
		this.endTime = endTime;
		this.activity = activity;
	}
	public final Task task;
	public final Task.ExternalCall<?> client;
	public final Instant endTime;
	public final Activity activity;
	
	public Instant getTime() {
		return this.endTime;
	}
}
