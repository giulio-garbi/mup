package org.sysma.lqnexecutor.model;

import org.sysma.lqnexecutor.model.converters.TimeDistributionConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("call")
public class Call implements Instruction  {
	@XStreamAsAttribute
	private String saveVar;
	
	@XStreamAsAttribute
	private String taskName;
	
	@XStreamAsAttribute
	private String entryName;

	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)	
	public TimeDistribution netSendTime;
	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)	
	public TimeDistribution netRcvTime;
	
	@Override
	public String getName() {
		if(name == null)
			return String.format("call(%s.%s)",taskName, entryName);
		else
			return name;
	}	
	
	public String getSaveVar() {
		return saveVar;
	}
	public void setSaveVar(String saveVar) {
		this.saveVar = saveVar;
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
