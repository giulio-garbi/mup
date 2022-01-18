package org.sysma.lqn.xml;

import java.util.List;
import java.util.stream.Collectors;

public class Task {
	public final String name;
	public final String multiplicity;
	public final String replicas;
	public final List<Entry> entries;
	public final TaskActivities tact;
	public final boolean isReference;
	
	public Task(String name, int multiplicity, int replicas, List<Entry> entries, TaskActivities tact,
			boolean isReference) {
		this.name = name;
		this.multiplicity = multiplicity < 0?"inf":multiplicity+"";
		this.replicas = replicas + "";
		this.entries = entries;
		this.tact = tact;
		this.isReference = isReference;
	}
	
	public String toXml() {
		return "<task name=\""+name+"\" multiplicity=\""+multiplicity+"\" replicas=\""+replicas+"\" "+
				(isReference?"scheduling=\"ref\"":"")+">"
				+ entries.stream().map(Entry::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ tact.toXml()+"\n"
				+ "</task>";
	}
}
