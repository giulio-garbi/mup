package org.sysma.lqn.xml.vanilla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.makeModel.NetTask;
import org.sysma.lqn.xml.Activity;
import org.sysma.lqn.xml.Precedence;
import org.sysma.lqn.xml.TaskActivities;

public class VanillaTaskActivities {
	public final List<VanillaActivity> activities;
	public final List<VanillaPrecedence> precedences;
	public final List<VanillaEntry> entries;
	
	public VanillaTaskActivities(List<VanillaActivity> activities, List<VanillaPrecedence> precedences, List<VanillaEntry> entries) {
		this.activities = activities;
		this.precedences = precedences;
		this.entries = entries;
	}
	
	public String toXml() {
		return "<task-activities>"
				+ activities.stream().map(VanillaActivity::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ precedences.stream().map(VanillaPrecedence::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ entries.stream().map(VanillaEntry::getRAXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</task-activities>";
	}
	
	public static VanillaTaskActivities from(TaskActivities tact, NetTask net) {
		HashMap<String, List<VanillaActivity>> convertedActs = new HashMap<>();
		ArrayList<VanillaActivity> activities = new ArrayList<>();
		ArrayList<VanillaPrecedence> precedences = new ArrayList<>();
		
		for(Activity act: tact.activities) {
			List<VanillaActivity> convAct = VanillaActivity.from(act, net);
			convertedActs.put(act.name, convAct);
			activities.addAll(convAct);
			precedences.addAll(VanillaPrecedence.internalPrecedences(convAct));
		}
		for(Precedence p: tact.precedences) {
			precedences.add(VanillaPrecedence.from(p, convertedActs));
		}
		List<VanillaEntry> entries = tact.entries.stream()
				.map(e->VanillaEntry.from(e, convertedActs)).collect(Collectors.toList());
		return new VanillaTaskActivities(activities, precedences, entries);
	}
}
