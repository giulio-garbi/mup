package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("waitfor")
public class WaitFor  implements Instruction {
	
	@XStreamAsAttribute
	private  String callVar;
	
	@XStreamAsAttribute
	private String name;

	public String getCallVar() {
		return callVar;
	}

	public void setCallVar(String callVar) {
		this.callVar = callVar;
	}
	

	
	@Override
	public String getName() {
		if(name == null)
			return String.format("waitfor(%s)",callVar);
		else
			return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
