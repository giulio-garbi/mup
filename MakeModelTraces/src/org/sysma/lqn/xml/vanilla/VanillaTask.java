package org.sysma.lqn.xml.vanilla;

import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.makeModel.NetTask;
import org.sysma.lqn.xml.Task;

public class VanillaTask {
	public final String name;
	public final String multiplicity;
	public final String replicas;
	public final List<VanillaEntry> entries;
	public final VanillaTaskActivities tact;
	public final boolean isReference;
	
	public VanillaTask(String name, String multiplicity, String replicas, List<VanillaEntry> entries, VanillaTaskActivities tact,
			boolean isReference) {
		this.name = name;
		this.multiplicity = multiplicity;
		this.replicas = replicas;
		this.entries = entries;
		this.tact = tact;
		this.isReference = isReference;
	}
	
	public String toXml() {
		return "<task name=\""+name+"\" multiplicity=\""+multiplicity+"\" replicas=\""+replicas+"\" "+
				(isReference?"scheduling=\"ref\"":"")+">"
				+ entries.stream().map(VanillaEntry::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ tact.toXml()+"\n"
				+ "</task>";
	}

	public static VanillaTask from(Task t, NetTask net) {
		VanillaTaskActivities tact = VanillaTaskActivities.from(t.tact, net);
		List<VanillaEntry> entries = tact.entries;
		
		return new VanillaTask(t.name, t.multiplicity, t.replicas, entries, tact, t.isReference);
	}
}
