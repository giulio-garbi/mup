package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("forward-activity")
public class ForwardingActivity {
	@XStreamAsAttribute
	public String name;
	@XStreamImplicit
	public SyncCall[] call = new SyncCall[0];
	@XStreamAsAttribute
	public float fwdAfterTime;
}
