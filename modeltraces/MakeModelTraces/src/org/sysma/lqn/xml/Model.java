package org.sysma.lqn.xml;

import java.util.List;
import java.util.stream.Collectors;

public class Model {
	public final List<Processor> procs;
	public final String name;
	
	public Model(String name, List<Processor> procs) {
		this.procs = procs;
		this.name = name;
	}
	
	public String toXml() {
		return "<?xml version=\"1.0\"?>\n<lqn-model name=\""+name+"\">"
				+ procs.stream().map(Processor::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</lqn-model>";
	}
}
