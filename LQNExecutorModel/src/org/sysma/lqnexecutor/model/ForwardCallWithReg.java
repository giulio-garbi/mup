package org.sysma.lqnexecutor.model;

import org.sysma.lqnexecutor.model.converters.TimeDistributionConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("fwd_call_reg")
public class ForwardCallWithReg implements Instruction  {	
	@XStreamAsAttribute
	private String taskName;
	
	@XStreamAsAttribute
	private String entryName;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)
	public TimeDistribution netSendTimeReg;
	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)
	public TimeDistribution netRcvTimeReg;


	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)
	public TimeDistribution netSendTimeCall;
	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)
	public TimeDistribution netRcvTimeCall;
	
	@Override
	public String getName() {
		if(name == null)
			return String.format("fwd_call_reg(%s.%s)",taskName, entryName);
		else
			return name;
	}	
	
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getEntryName() {
		return entryName;
	}
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public void setName(String name) {
		this.name = name;
	}
}
