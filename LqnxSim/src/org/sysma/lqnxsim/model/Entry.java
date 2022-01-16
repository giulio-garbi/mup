package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("entry")
public class Entry {
	
	@XStreamAsAttribute
	public String name;
}
