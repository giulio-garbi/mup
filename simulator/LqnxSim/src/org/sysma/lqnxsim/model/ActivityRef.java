package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("activity")
public class ActivityRef {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public double prob = -1;
}
