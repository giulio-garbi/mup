package org.sysma.lqnexecutor.model;

import org.sysma.lqnexecutor.model.converters.TimeDistributionConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("query")
public class Query {
	
	@XStreamAsAttribute
	private String name;

	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)	
	private TimeDistribution runtime;

	public TimeDistribution getRuntime() {
		return runtime;
	}

	public void setRuntime(TimeDistribution runtime) {
		this.runtime = runtime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}