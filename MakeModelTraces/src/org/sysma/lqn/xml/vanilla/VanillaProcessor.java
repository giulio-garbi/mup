package org.sysma.lqn.xml.vanilla;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.sysma.lqn.makeModel.NetTask;
import org.sysma.lqn.xml.Processor;

public class VanillaProcessor {
	public final String name;
	public final VanillaTask[] tasks;
	
	public VanillaProcessor(String name, VanillaTask... tasks) {
		this.name = name;
		this.tasks = tasks;
	}
	
	public String toXml() {
		return "<processor name=\""+name+"\" scheduling=\"inf\">"
				+ Arrays.stream(tasks).map(VanillaTask::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</processor>";
	}

	public static VanillaProcessor from(Processor p, NetTask net) {
		return new VanillaProcessor(p.name, 
				Arrays.stream(p.tasks).map(t->VanillaTask.from(t, net)).toArray(VanillaTask[]::new));
	}
}
