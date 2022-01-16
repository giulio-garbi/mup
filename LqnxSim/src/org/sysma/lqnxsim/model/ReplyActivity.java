package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("reply-activity")
public class ReplyActivity {
	
	@XStreamAsAttribute
	public String name;
}
