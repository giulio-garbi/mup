package org.sysma.lqn.makeModel;

import java.util.ArrayList;
import java.util.List;

import org.sysma.lqn.xml.vanilla.VanillaActivity;
import org.sysma.lqn.xml.vanilla.VanillaEntry;
import org.sysma.lqn.xml.vanilla.VanillaProcessor;
import org.sysma.lqn.xml.vanilla.VanillaTask;
import org.sysma.lqn.xml.vanilla.VanillaTaskActivities;

public class NetTask {
	public final String name, pfxEntry,  pfxActivities;
	protected int progr = 0;
	public final ArrayList<VanillaActivity> activities = new ArrayList<>();
	public final ArrayList<VanillaEntry> entries = new ArrayList<>();
	
	public NetTask(String name, String pfxEntry, String pfxActivities) {
		this.name = name;
		this.pfxEntry = pfxEntry;
		this.pfxActivities = pfxActivities;
	}
	
	public String newNetEntry(float serviceTime) {
		String actName = pfxActivities+progr;
		String entName = pfxEntry+progr;
		VanillaActivity act = new VanillaActivity(actName,
				serviceTime, 0, 0, entName, null);
		VanillaEntry ent = new VanillaEntry(entName, act);
		activities.add(act);
		entries.add(ent);
		progr++;
		return entName;
	}
	
	public VanillaProcessor getProcessor() {
		VanillaTaskActivities tact = new VanillaTaskActivities(activities, List.of(), entries);
		VanillaTask task = new VanillaTask(name, "inf", "1", entries, tact, false);
		VanillaProcessor proc = new VanillaProcessor(name, task);
		return proc;
	}
}
