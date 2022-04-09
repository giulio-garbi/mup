package org.sysma.lqn.makeModel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.xml.Model;
import org.sysma.lqn.xml.Task;
import org.sysma.lqn.xml.TaskActivities;
import org.sysma.lqnexecutor.model.TimeDistribution;
import org.sysma.lqn.xml.Activity;
import org.sysma.lqn.xml.Entry;
import org.sysma.lqn.xml.Precedence;
import org.sysma.lqn.xml.Processor;
import org.sysma.schedulerExecutor.LogLine;
import org.sysma.schedulerExecutor.TaskDump;

public class LQNTraces {
	private final HashMap<String, HashMap<String, EntryTraces>> etraces;
	private final HashMap<String, HashMap<String, List<Long>>> dbQueries;
	private final HashMap<String, Integer> taskMult;
	private final HashMap<String, Integer> taskReplicas;
	private final String mainTask;
	
	private LQNTraces(HashMap<String, HashMap<String, EntryTraces>> etraces,
			HashMap<String, HashMap<String, List<Long>>> dbQueries,
			HashMap<String, Integer> taskMult,
			HashMap<String, Integer> taskReplicas,
			String mainTask) {
		this.etraces = etraces;
		this.dbQueries = dbQueries;
		this.taskMult = taskMult;
		this.taskReplicas = taskReplicas;
		this.mainTask = mainTask;
	}
	
	/*private Processor getFakeRefProcess() {
		ArrayList<Activity> activities = new ArrayList<>();
		ArrayList<Precedence> precedences = new ArrayList<>();
		etraces.entrySet().stream().forEach((tsk)->{
			String tskName = tsk.getKey();
			tsk.getValue().keySet().forEach((entName)->{
				Activity ac = new Activity("e"+activities.size(), 0, 0, null);
				ac.whoCall = tskName+"-"+entName;
				activities.add(ac);
			});
		});
		activities.get(0).boundToEntry = "fake-fake";
		for(int i=0; i<activities.size()-1; i++)
			precedences.add(new Precedence().pre(activities.get(i)).post(activities.get(i+1)));
		Entry ent = new Entry("fake-fake");
		Task task = new Task("fake", 1, List.of(ent), new TaskActivities(activities, precedences, List.of()), true);
		Processor p = new Processor("fake", task);
		return p;
	}*/
	
	private static Processor makeDbProcessor(String dbName, HashMap<String, List<Long>> queries) {
		ArrayList<Activity> activities = new ArrayList<>();
		ArrayList<Entry> entries = new ArrayList<>();
		queries.forEach((qryName, times)->{
			String entryName = dbName+"-"+qryName;
			String actName = "dbact-"+dbName+"-"+qryName;
			float meanTime = 0;
			float mean2Time = 0;
			for(var x:times) {
				meanTime+=x;
				mean2Time+=1f*x*x;
			}
			meanTime/=1000f*Math.max(1, times.size());
			mean2Time/=1000f*1000f*Math.max(1, times.size());
			float variance = mean2Time - meanTime*meanTime;
			float cvsq = 0;// variance/meanTime/meanTime;
			Activity act = new Activity(actName, meanTime, cvsq, 1, entryName, false);
			Entry ent = new Entry(entryName, act);
			activities.add(act);
			entries.add(ent);
		});
		Task task = new Task(dbName, -1, 1, entries, new TaskActivities(activities, List.of(), entries), false);
		Processor p = new Processor(dbName, task);
		return p;
	}
	
	private Processor makeMainTask(String newEntryName, int mult, String mainEntryName) {
		Activity actCall = new Activity("behaviour", 0f, 1f, 1, newEntryName, false);
		actCall.whoCall = mainEntryName;
		TaskActivities tAct = new TaskActivities(List.of(actCall), List.of(), List.of());
		
		Entry ent = new Entry(newEntryName);
		Task t = new Task("Start", mult, 1, List.of(ent), tAct, true);
		Processor p = new Processor("Start", t);
		return p;
	}
	
	public Model getModel() {
		String[] mainEntryName = {null};
		int[] mainEntryMult = {-1};
		
		ArrayList<Processor> procs = etraces.entrySet().stream()
				.map((tkv)->{ 
					ArrayList<Activity> activities = new ArrayList<>();
					ArrayList<Precedence> precedences = new ArrayList<>();
					for(var et:tkv.getValue().values()) {
						et.collectItems(activities, precedences);
					}
					var entries = tkv.getValue().values().stream().flatMap(EntryTraces::getEntry).collect(Collectors.toList());
					boolean isMainTask = tkv.getKey().equals(mainTask);
					if(isMainTask) {
						mainEntryName[0] = entries.get(0).name;
						mainEntryMult[0] = taskMult.get(tkv.getKey());
					}
					Task t = new Task(tkv.getKey(), isMainTask?-1:taskMult.get(tkv.getKey()), 
							taskReplicas.get(tkv.getKey()), entries,
							new TaskActivities(activities, precedences, entries),false);
					Processor p = new Processor(tkv.getKey(), t);
					return p;
				}).collect(Collectors.toCollection(ArrayList::new));
		procs.add(makeMainTask("Start-main", mainEntryMult[0], mainEntryName[0]));
		dbQueries.forEach((dbName, queries)->{
			procs.add(makeDbProcessor(dbName, queries));
		});
		return new Model("mdl", procs);
	}
	
	public org.sysma.lqnexecutor.model.LQN getLqneModel() {
		String[] mainEntryName = {null};
		int[] mainEntryMult = {-1};
		int[] portLast = new int[] {8080};
		
		ArrayList<org.sysma.lqnexecutor.model.Task> tasks = etraces.entrySet().stream()
				.map((tkv)->{ 
					org.sysma.lqnexecutor.model.Task t = new org.sysma.lqnexecutor.model.Task();
					t.setName(tkv.getKey());
					t.setPort(portLast[0]);
					portLast[0]++;
					t.setThreadpoolSize(taskMult.get(tkv.getKey()));
					
					ArrayList<Activity> activities = new ArrayList<>();
					ArrayList<Precedence> precedences = new ArrayList<>();
					for(var et:tkv.getValue().values()) {
						et.collectItems(activities, precedences);
					}
					var entries = tkv.getValue().values().stream().flatMap(EntryTraces::getLqneEntry)
							.toArray(org.sysma.lqnexecutor.model.Entry[]::new);
					boolean isMainTask = tkv.getKey().equals(mainTask);
					if(isMainTask) {
						mainEntryName[0] = entries[0].getName();
						mainEntryMult[0] = taskMult.get(tkv.getKey());
					}
					
					t.setEntries(entries);
					
					/*Task t = new Task(tkv.getKey(), isMainTask?-1:taskMult.get(tkv.getKey()), 
							entries,
							new TaskActivities(activities, precedences, entries),false);*/
					//Processor p = new Processor(tkv.getKey(), t);
					return t;
				}).collect(Collectors.toCollection(ArrayList::new));

		org.sysma.lqnexecutor.model.Clients clients = new org.sysma.lqnexecutor.model.Clients();
		org.sysma.lqnexecutor.model.Call callMain = new org.sysma.lqnexecutor.model.Call();
		callMain.setTaskName(mainTask);
		callMain.setEntryName(mainEntryName[0]);
		callMain.setSaveVar("main");
		org.sysma.lqnexecutor.model.WaitFor waitMain = new org.sysma.lqnexecutor.model.WaitFor();
		waitMain.setCallVar("main");
		clients.setPopulation(mainEntryMult[0]);
		clients.setCode(new org.sysma.lqnexecutor.model.Instruction[] {callMain, waitMain});
		
		ArrayList<org.sysma.lqnexecutor.model.Database> databases = new ArrayList<>();
		dbQueries.forEach((dbName, queries)->{
			databases.add(makeDbTasksLqne(dbName, queries, portLast[0]++));
		});
		
		org.sysma.lqnexecutor.model.LQN lqn = new org.sysma.lqnexecutor.model.LQN();
		lqn.setClients(clients);
		lqn.setTasks(tasks.stream().toArray(org.sysma.lqnexecutor.model.Task[]::new));
		lqn.setDatabases(databases.stream().toArray(org.sysma.lqnexecutor.model.Database[]::new));
		return lqn;
	}
	
	private static TimeDistribution getBusyTime(List<Long> s) {
		float[] mean = {0f, 0f};
		int[] n = {0};
		s.stream().forEach(x->{
			n[0]++;
			mean[0]+=1f*x;
			mean[1]+=1f*x*x;
		});
		mean[0] /= n[0];
		mean[1] /= n[0];
		float variance = mean[1] - mean[0]*mean[0];
		/*return (new TimeDistribution.PositiveNormal(
				Duration.ofNanos((long)(mean[0]*1000_000)), 
				Duration.ofNanos((long)(Math.sqrt(variance)*1000_000))));*/
		return new TimeDistribution.Deterministic(
				Duration.ofNanos((long)(mean[0]*1000_000)));
		
	}
	
	/*private static org.sysma.lqnexecutor.model.Task makeDbTasksLqne(String dbName, HashMap<String, List<Long>> queries, int port) {
		org.sysma.lqnexecutor.model.Task t = new org.sysma.lqnexecutor.model.Task();
		t.setName(dbName);
		t.setPort(port);
		t.setThreadpoolSize(1);
		var entries = queries.entrySet().stream().map(e->{
			var ent = new org.sysma.lqnexecutor.model.Entry();
			ent.setName(dbName+"-"+e.getKey());
			var busy = new org.sysma.lqnexecutor.model.Busy();
			busy.setRuntime(getBusyTime(e.getValue()));
			ent.setCode(new org.sysma.lqnexecutor.model.Instruction[] {busy});
			return ent;
		}).toArray(org.sysma.lqnexecutor.model.Entry[]::new);
		t.setEntries(entries);
		return t;
	}*/
	
	private static org.sysma.lqnexecutor.model.Database makeDbTasksLqne(String dbName, HashMap<String, List<Long>> queries, int port) {
		org.sysma.lqnexecutor.model.Database t = new org.sysma.lqnexecutor.model.Database();
		t.setName(dbName);
		var entries = queries.entrySet().stream().map(e->{
			var qry = new org.sysma.lqnexecutor.model.Query();
			qry.setName(dbName+"-"+e.getKey());
			qry.setRuntime(getBusyTime(e.getValue()));
			return qry;
		}).toArray(org.sysma.lqnexecutor.model.Query[]::new);
		t.setQueries(entries);
		return t;
	}

	public static LQNTraces from(TaskDump... tds) {
		HashMap<String, HashMap<String, EntryTraces>> etr = new HashMap<>();
		HashMap<String, Integer> taskMultz = new HashMap<>();
		HashMap<String, Integer> taskRepz = new HashMap<>();
		String mainTask = null;
		HashMap<String, HashMap<String, HashMap<String, ArrayList<LogLine>>>> logs = new HashMap<>();
		HashMap<String, HashMap<String, List<Long>>> dbQueriesTimes = new HashMap<>();
		
		for(var td:tds) {
			taskMultz.put(td.taskName, td.mult);
			taskRepz.put(td.taskName, taskRepz.getOrDefault(td.taskName, 0)+1);
			if(td.isClient)
				mainTask = td.taskName;
			td.queries.forEach((dbName,queries)->{
				var qs = dbQueriesTimes.computeIfAbsent(dbName, (x)->new HashMap<>());
				queries.forEach(q->{
					qs.computeIfAbsent(q, (x)->new ArrayList<>());
				});
			});
			
			var thisetr = etr.computeIfAbsent(td.taskName, x->new HashMap<>());
			td.entries.forEach(ename->{
				thisetr.computeIfAbsent(ename, x->new EntryTraces(td.taskName+"-"+ename));
			});
		}
		
		for(var td:tds) {
			td.log.stream().sorted((l1,l2)->Long.compare(l1.time, l2.time)).forEachOrdered(l->{
				logs.computeIfAbsent(l.taskName, (x)->new HashMap<>())
					.computeIfAbsent(l.entryName, (x)->new HashMap<>())
					.computeIfAbsent(l.client, (x)->new ArrayList<>())
					.add(l);
			});
		}
		for(var kv1: logs.entrySet()) {
			for(var kv2: kv1.getValue().entrySet()) {
				etr.get(kv1.getKey()).get(kv2.getKey()).addExecutions(kv2.getValue(), dbQueriesTimes);
			}
		}
		return new LQNTraces(etr, dbQueriesTimes, taskMultz, taskRepz, mainTask);
	}
}
