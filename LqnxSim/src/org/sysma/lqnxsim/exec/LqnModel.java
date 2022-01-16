package org.sysma.lqnxsim.exec;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LqnModel {
	private Instant clock = Instant.EPOCH;
	private Instant debug_clock = Instant.EPOCH;
	private PriorityQueue<Event> eventList = new PriorityQueue<>();
	
	private HashMap<String, List<Task>> entry2task;
	private HashSet<Task> tasks;
	
	public LqnModel(org.sysma.lqnxsim.model.LqnModel lqnModel) {
		entry2task = new HashMap<>();
		tasks = new HashSet<>();
		for(var pr:lqnModel.procs)
			for(var tsk:pr.tasks) {
				int replicas = (tsk.replicas==null)?1:Integer.parseInt(tsk.replicas);
				for(int i=0; i<replicas; i++) {
					var texec = new Task(tsk, this);
					tasks.add(texec);
					if(tsk.entries != null)
						for(var ent:tsk.entries) 
							entry2task.computeIfAbsent(ent.name, x->new ArrayList<Task>())
								.add(texec);
				}
			}
	}
	public void startAllClients() {
		for(var t:tasks)
			t.startAllClients();
	}
	public Instant getClock() {
		return clock;
	}
	public Random getRandom() {
		// TODO Auto-generated method stub
		return ThreadLocalRandom.current();
	}
	public void event(Event evt) {
		// TODO Auto-generated method stub
		eventList.add(evt);
	}
	
	public List<Duration> getClientResponseTimes() {
		return tasks.stream().filter(t->t.amClient).flatMap(t->t.responseTimes.values().stream()).flatMap(t->t.stream()).collect(Collectors.toList());
	}
	
	public void debugRTs(String outFname) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(outFname);
		for(var tlist:entry2task.values()) {
			var t0 = tlist.get(0);
			for(var tname:t0.responseTimes.keySet()) {
				var allRTs = tlist.stream().flatMap(t->t.responseTimes.get(tname).stream()).collect(Collectors.toList());
				var mean = allRTs.stream().collect(Collectors.averagingDouble(d->d.toNanos()/1000_000_000.0));
				var mean2 = allRTs.stream().map(d->d.toNanos()/1000_000_000.0)
						.map(d->d*d).collect(Collectors.averagingDouble(d->d));
				var devstd = Math.sqrt(Math.max(0,mean2 - mean*mean));
				var n = allRTs.stream().count()*1.0;
				var s = devstd/Math.sqrt(n) * 1.96;
				var up = mean+s;
				var down = mean-s;
				pw.println(tname+"; "+mean+"; "+down+"; "+up);
			}
		}
		pw.close();
	}
	
	public void debugUtils(String outFname, Instant stoppedAt) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(outFname);
		HashMap<String, Double> cpus = new HashMap<>();
		for(var tlist:entry2task.values()) {
			cpus.put(tlist.get(0).name,
					tlist.stream().map(t->t.cpuRuntime.avgCpu(stoppedAt)).collect(Collectors.summingDouble(x->x)));
		}
		cpus.keySet().stream().sorted().forEach(t->{
			pw.println(t+"; "+cpus.get(t));
		});
		pw.close();
	}
	
	public void advance() {
		var evt = eventList.poll();
		if(evt == null) {
			return;
		}
		clock = evt.getTime();
		if(debug_clock.isAfter(clock))
			throw new Error();
		debug_clock = clock.plus(Duration.ZERO);
		//System.out.println(evt.getClass());
		if(evt instanceof BusyEvent) {
			var busy = (BusyEvent) evt;
			busy.task.advance(busy);
		} else if(evt instanceof CallEvent) {
			var call = (CallEvent) evt;
			var entryName = call.activity.call[call.which].dest;
			var tasks = entry2task.get(entryName);
			var taskDest = tasks.get(ThreadLocalRandom.current().nextInt(tasks.size()));
			//System.out.println(entryName);
			taskDest.call(entryName, call);
		} else if(evt instanceof FreeThreadEvent) {
			var fte = (FreeThreadEvent) evt;
			//System.out.println(fte.task.name);
			fte.task.advance(fte);
		} else if(evt instanceof FwdCallEvent) {
			var call = (FwdCallEvent) evt;
			var entryName = call.call.dest;
			var tasks = entry2task.get(entryName);
			var taskDest = tasks.get(ThreadLocalRandom.current().nextInt(tasks.size()));
			taskDest.call(entryName, call);
		} else if(evt instanceof ResponseEvent) {
			var resp = (ResponseEvent<?>) evt;
			if(resp.initiated == null) {
			} else if(resp.initiated instanceof CallEvent) {
				var ce = (CallEvent) resp.initiated;
				ce.task.response(ce);
			} else if(resp.initiated instanceof FwdCallEvent) {
				var fce = (FwdCallEvent) resp.initiated;
				fce.task.response(fce);
			} else {
				throw new Error();
			}
		} else {
			throw new Error();
		}
	}
}
