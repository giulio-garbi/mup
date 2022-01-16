package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("synch-call")
public class SyncCall {
	@XStreamAsAttribute
	public String dest;
	@XStreamAsAttribute
	@XStreamAlias("calls-mean")
	public int callsMean;

	@XStreamAsAttribute
	@XStreamAlias("netSendTime")
	public float netSendTime;
	@XStreamAsAttribute
	@XStreamAlias("netRcvTime")
	public float netRcvTime;
}
