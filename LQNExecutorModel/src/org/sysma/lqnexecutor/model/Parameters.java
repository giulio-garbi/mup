package org.sysma.lqnexecutor.model;

import java.time.Duration;

import org.sysma.lqnexecutor.model.converters.DurationConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("params")
public class Parameters {
	@XStreamAsAttribute
    @XStreamConverter(value=DurationConverter.class)	
	private Duration length;
	//@XStreamAsAttribute
    //@XStreamConverter(value=DurationConverter.class)	
	//private Duration dt;
	//@XStreamAsAttribute
	//private int repetitions;
	
	public Duration getLength() {
		return length;
	}
	public void setLength(Duration length) {
		this.length = length;
	}
	/*public Duration getDt() {
		return dt;
	}
	public void setDt(Duration dt) {
		this.dt = dt;
	}
	public int getRepetitions() {
		return repetitions;
	}
	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}*/
}
