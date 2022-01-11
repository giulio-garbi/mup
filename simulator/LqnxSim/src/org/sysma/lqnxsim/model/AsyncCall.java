package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("asynch-call")
public class AsyncCall {
	@XStreamAsAttribute
	public String dest;
	@XStreamAlias("calls-mean")
	public int callsMean;
}
