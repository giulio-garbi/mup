package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("activity")
public class Activity {
	@XStreamAsAttribute
	@XStreamAlias("host-demand-mean")
	public Double hostDemandMean;
	
	@XStreamAsAttribute
	@XStreamAlias("host-demand-cvsq")
	public Double hostDemandCvsq;
	@XStreamAsAttribute
	public int phase;
	
	@XStreamAsAttribute
	@XStreamAlias("bound-to-entry")
	public String boundToEntry;
	@XStreamAsAttribute
	public String name;
	@XStreamImplicit
	public SyncCall[] call = new SyncCall[0];
}
