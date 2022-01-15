package org.sysma.lqnexecutor.model;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.sysma.lqnexecutor.model.converters.DurationConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("think")
public class Think implements Instruction {
	@XStreamAsAttribute
    @XStreamConverter(value=DurationConverter.class)	
	private Duration runtime;
	
	@XStreamAsAttribute
	private String name;

	public Supplier<Duration> getRuntime() {
		return ()->(
				Duration.ofMillis(Double.valueOf(Math.log(1 - ThreadLocalRandom.current().nextDouble()) * (-runtime.toMillis()))
						.longValue()));
	}
	

	
	@Override
	public String getName() {
		if(name == null)
			return String.format("think(%dms)",runtime.toMillis());
		else
			return name;
	}
}
