package org.sysma.lqnexecutor.model;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.sysma.lqnexecutor.model.converters.TimeDistributionConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("busy")
public class Busy implements Instruction  {
	@XStreamAsAttribute
    @XStreamConverter(value=TimeDistributionConverter.class)	
	private TimeDistribution runtime;
	
	@XStreamAsAttribute
	private String name;	

	public Supplier<Duration> getRuntime() {
		return ()->(
			/*Duration.ofMillis(Double.valueOf(Math.log(1 - ThreadLocalRandom.current().nextDouble()) * (-runtime.toMillis()))
					.longValue())*/
			runtime.next(ThreadLocalRandom.current())
		);
	}
	
	@Override
	public String getName() {
		if(name == null)
			return String.format("busy(%s)",TimeDistributionConverter.convertToString(runtime));
		else
			return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setRuntime(TimeDistribution runtime) {
		this.runtime  = runtime;
	}
}
