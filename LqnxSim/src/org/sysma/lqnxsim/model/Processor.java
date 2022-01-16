package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("processor")
public class Processor {
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public String scheduling;
	@XStreamImplicit
	public Task[] tasks = new Task[0];
}
