package org.sysma.lqn.xml;

import java.util.List;
import java.util.stream.Collectors;

public class TaskActivities {
	public final List<Activity> activities;
	public final List<Precedence> precedences;
	public final List<Entry> entries;
	
	public TaskActivities(List<Activity> activities, List<Precedence> precedences, List<Entry> entries) {
		this.activities = activities;
		this.precedences = precedences;
		this.entries = entries;
	}
	
	public String toXml() {
		return "<task-activities>"
				+ activities.stream().map(Activity::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ precedences.stream().map(Precedence::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ entries.stream().map(Entry::getRAXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</task-activities>";
	}
}
