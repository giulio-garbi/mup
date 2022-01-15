package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("response_time_end")
public class ResponseTimeEnd implements Instruction {
	
	@XStreamAsAttribute
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	
	@Override
	public String getName() {
		return String.format("rtEnd(%s)",label);
	}
}
