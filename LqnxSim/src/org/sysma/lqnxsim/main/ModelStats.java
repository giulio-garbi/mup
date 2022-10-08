package org.sysma.lqnxsim.main;

import org.sysma.lqnxsim.model.*;

public class ModelStats {
	public int tasks=2, entries, activities, nodes, or_nodes, arcs, paths;
	public ModelStats(LqnModel model) {
		if(model.procs != null)
			for (var p: model.procs)
				visit(p);
	}
	
	private void visit(Processor p) {
		if(p.tasks != null)
			for(var t: p.tasks)
				visit(t);
	}
	
	private void visit(Task t) {
		if(t.name.equals("registry") || t.name.equals("Client") || t.name.equals("Start"))
			return;
		tasks++;

		if(t.tActivities != null)
			for(var a:t.tActivities) visit(a);
		if(t.entries != null)
			for(var e:t.entries) visit(e);
	}
	
	private void visit(Entry e) {
		entries++;
	}

	private void visit(TaskActivities ta) {
		if(ta.activity != null)
			for(var a: ta.activity) visit(a);
		if(ta.precedences != null)
			for(var p: ta.precedences) visit(p);
		if(ta.replyEntries != null)
			for(var re: ta.replyEntries) visit(re);
	}

	private void visit(Activity a) {
		if((a.boundToEntry != null && a.boundToEntry.length() == 0) && a.hostDemandMean < Double.MIN_VALUE)
			return;
		activities++;
		if(a.call != null)
			for(var c:a.call) visit(c);
	}
	
	private void visit(SyncCall c) {
		if(c.dest.equals("registry-Query"))
			return;
		arcs+=2;
		entries++;
	}

	private void visit(ReplyEntry re) {
		var pcount = re.replyActivities.length;
		arcs += pcount+1;
		paths += pcount;
		or_nodes++;
		nodes++;
	}

	private void visit(Precedence p) {
		if(p.pre != null) {
			if(p.post != null) {
				arcs++;
			} else if (p.postAnd != null) {
				nodes++;
				arcs += 1 + p.postAnd.length;
			}
		} else if(p.preAnd != null) {
			nodes++;
			arcs+=p.preAnd.length;
			
			if(p.post != null) {
				arcs++;
			} else if (p.postAnd != null) {
				nodes++;
				arcs += 1 + p.postAnd.length;
			}
		}
	}
}
