package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("post-OR")
public class PostOr {
	@XStreamImplicit
	public ActivityRef[] activities = new ActivityRef[0];
}
