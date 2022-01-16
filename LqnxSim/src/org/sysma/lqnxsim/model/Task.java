package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("task")
public class Task {
	
	@XStreamAsAttribute
	public String name;
	
	@XStreamAsAttribute
	public String multiplicity;
	
	@XStreamAsAttribute
	public String replicas;

	@XStreamAsAttribute
	public String scheduling;
	
	@XStreamImplicit
	public Entry[] entries = new Entry[0];
	
	@XStreamImplicit
	public TaskActivities[] tActivities = new TaskActivities[0];
}
