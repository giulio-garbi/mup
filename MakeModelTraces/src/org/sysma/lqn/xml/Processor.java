package org.sysma.lqn.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Processor {
	public final String name;
	public final Task[] tasks;
	
	public Processor(String name, Task... tasks) {
		this.name = name;
		this.tasks = tasks;
	}
	
	public String toXml() {
		return "<processor name=\""+name+"\" scheduling=\"inf\">"
				+ Arrays.stream(tasks).map(Task::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</processor>";
	}
}
