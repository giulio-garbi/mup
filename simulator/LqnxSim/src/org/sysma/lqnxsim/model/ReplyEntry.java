package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("reply-entry")
public class ReplyEntry {
	@XStreamAsAttribute
	public String name;
	
	@XStreamImplicit
	public ReplyActivity[] replyActivities = new ReplyActivity[0];
}
