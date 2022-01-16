package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("pre-AND")
public class PreAnd {
	@XStreamImplicit
	public ActivityRef[] activities = new ActivityRef[0];
}
