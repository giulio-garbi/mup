package org.sysma.lqnxsim.exec;

import java.time.Instant;

import org.sysma.lqnxsim.exec.Task.ExternalCall;
import org.sysma.lqnxsim.model.SyncCall;

public class FwdCallEvent extends Event {
	public FwdCallEvent(Task task, ExternalCall<?> client, Instant startTime, SyncCall call) {
		super();
		this.task = task;
		this.client = client;
		this.startTime = startTime;
		this.call = call;
	}
	public final Task task;
	public final Task.ExternalCall<?> client;
	public final Instant startTime;
	public final SyncCall call;
	@Override
	public Instant getTime() {
		return startTime;
	}
}
