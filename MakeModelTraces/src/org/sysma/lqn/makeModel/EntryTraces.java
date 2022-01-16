package org.sysma.lqn.makeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sysma.lqn.xml.Activity;
import org.sysma.lqn.xml.Entry;
import org.sysma.lqn.xml.Precedence;
import org.sysma.lqnexecutor.model.Instruction;
import org.sysma.lqnexecutor.model.ProbChoice;
import org.sysma.schedulerExecutor.LogLine;

public class EntryTraces {
	private final HashMap<String, Trace> tracedb;
	private final HashMap<Trace, Integer> reps;
	private int repSum = 0;
	private final String name;
	
	public EntryTraces(String name) {
		this.tracedb = new HashMap<>();
		this.reps = new HashMap<>();
		this.name = name;
	}
	
	public void addExecutions(HashMap<String, ArrayList<LogLine>> hashMap, HashMap<String, HashMap<String, List<Long>>> dbQueryTimes) {
		for(var exec: hashMap.values()) {
			String traceDesc = Trace.getDesc(exec);
			Trace tr;
			if(tracedb.containsKey(traceDesc)) {
				tr = tracedb.get(traceDesc);
				tr.addExecution(exec);
				reps.put(tr, reps.get(tr)+1);
			} else {
				tr = Trace.from(exec, dbQueryTimes);
				tracedb.put(traceDesc, tr);
				reps.put(tr, 1);
			}
			repSum++;
		}
	}
	
	public Stream<Entry> getEntry() {
		var rsp = tracedb.values().stream().map(t->t.getLastAct())
				.collect(Collectors.toList());//.toArray(Activity[]::new);
		if(rsp.size() > 0)
			return Stream.of(new Entry(name, rsp.stream().filter(act->!act.isFwdAct)
					.collect(Collectors.toList()).toArray(Activity[]::new)));
		else
			return Stream.of();
	}
	
	public Instruction[] getLqneCode() {
		if(tracedb.size() == 0) {
			return new Instruction[0];
		} else if(tracedb.size() == 1) {
			Trace tr0 = tracedb.values().iterator().next();
			tr0.compile();
			return tr0.getLqneCode();
		} else {
			float[] probs = new float[reps.size()-1];
			ProbChoice[] pcs = new ProbChoice[probs.length];
			Instruction[][] branches = new Instruction[reps.size()][];
			var repsItr = reps.entrySet().iterator();
			int rSumRemain = this.repSum;
			for(int i=0; i<reps.size(); i++) {
				var r = repsItr.next();
				r.getKey().compile();
				branches[i] = r.getKey().getLqneCode();
				if(i<probs.length) {
					probs[i] = r.getValue()*1.0f/rSumRemain;
					rSumRemain -= r.getValue();
				}
			}
			for(int i=pcs.length-1; i>=0; i--) {
				pcs[i] = new ProbChoice();
				pcs[i].setProb(probs[i]);
				pcs[i].setThen(branches[i]);
				if(i == pcs.length-1)
					pcs[i].setElse(branches[i+1]);
				else
					pcs[i].setElse(new Instruction[] {pcs[i+1]});
			}
			return new Instruction[] {pcs[0]};
		}
	}
	
	public Stream<org.sysma.lqnexecutor.model.Entry> getLqneEntry() {
		var ent = new org.sysma.lqnexecutor.model.Entry();
		ent.setName(name);
		ent.setCode(getLqneCode());
		return Stream.of(ent);
	}
	
	public void collectItems(ArrayList<Activity> activities, ArrayList<Precedence> precedences) {
		if(tracedb.size() == 0) {
			
		} else if(tracedb.size() == 1) {
			Trace tr0 = tracedb.values().iterator().next();
			tr0.compile();
			tr0.activities.get(0).boundToEntry = name;
			activities.addAll(tr0.activities);
			precedences.addAll(tr0.precedences);
		} else {
			Activity startAct = new Activity(Trace.getFreshName(), 0f, 1f, 1, name, false);
			float[] probs = new float[reps.size()];
			Activity[] acts = new Activity[reps.size()];

			var repsItr = reps.entrySet().iterator();
			for(int i=0; i<probs.length; i++) {
				var r = repsItr.next();
				r.getKey().compile();
				probs[i] = r.getValue()*1.0f/this.repSum;
				acts[i] = r.getKey().activities.get(0);
				activities.addAll(r.getKey().activities);
				precedences.addAll(r.getKey().precedences);
			}
			
			activities.add(startAct);
			//activities.add(endAct);
			precedences.add(new Precedence().pre(startAct).postOr(acts, probs));
			//precedences.add(new Precedence().preOr(...).post(endAct));
			/*
			int repRim = repSum;
			float[] probs = new float[reps.size()-1];
			Activity[] actTrues = new Activity[reps.size()-1];
			Activity lastAct = null;
			var repsItr = reps.entrySet().iterator();
			String[] names = new String[probs.length];
			for(int i=0; i<probs.length; i++) {
				var r = repsItr.next();
				probs[i] = r.getValue()*1.0f/repRim;
				repRim-=r.getValue();
				actTrues[i] = r.getKey().activities.get(0);
				names[i] = "nm"+i;
			}
			lastAct = repsItr.next().getKey().activities.get(0);
			Precedence[] precsToAdd = new Precedence[actTrues.length];
			Activity actToAdd = new Activity("name", 1, 0, name);
			for(int i=probs.length-1; i>=0; i--) {
				String then = actTrues[i].name;
				String els = i==(probs.length-1)?lastAct.name:precsToAdd[i+1].preOrName;
				if(i==0)
					precsToAdd[i] = new Precedence("pre", new String[] {actToAdd.name}, 
							"post-OR", new String[] {then, els}, names[i], probs[i]);
				else
					precsToAdd[i] = new Precedence("pre", new String[] {precsToAdd[i-1].preOrName}, 
							"post-OR", new String[] {then, els}, names[i], probs[i]);
			}
			//TODO POST-OR
			activities.add(actToAdd);
			for(var p:precsToAdd)
				precedences.add(p);
			for(var t:reps.keySet()) {
				activities.addAll(t.activities);
				precedences.addAll(t.precedences);
			}
			// TODO no activities but pres in if!
			 * 
			 */
		}
	}
}
