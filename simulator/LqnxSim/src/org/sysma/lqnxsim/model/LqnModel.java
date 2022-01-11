package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("lqn-model")
public class LqnModel {
	@XStreamAsAttribute
	public String name;
	
	@XStreamImplicit
	public Processor[] procs = new Processor[0];
}
