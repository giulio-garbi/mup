package org.sysma.lqnxsim.exec;

import java.time.Instant;

public class ResponseEvent<X>  extends Event{
	public ResponseEvent(X initiated, Instant responseTime) {
		super();
		this.initiated = initiated;
		this.responseTime = responseTime;
	}
	public final X initiated;
	public final Instant responseTime;
	@Override
	public Instant getTime() {
		return responseTime;
	}
}
