package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("task-activities")
public class TaskActivities {
	
	@XStreamImplicit
	public Activity[] activity = new Activity[0];

	@XStreamImplicit
	public ForwardingActivity[] forwarding_activity = new ForwardingActivity[0];

	@XStreamImplicit
	public Precedence[] precedences = new Precedence[0];

	@XStreamImplicit
	public ReplyEntry[] replyEntries = new ReplyEntry[0];
}
