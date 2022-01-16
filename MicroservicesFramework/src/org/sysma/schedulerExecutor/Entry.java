package org.sysma.schedulerExecutor;

import java.util.ArrayList;
import java.util.stream.Stream;

public abstract class Entry {
	private final ArrayList<ArrayList<LogLine>> allLogs = new ArrayList<>();
	private ArrayList<LogLine> getFreshLog(){
		var al = new ArrayList<LogLine>();
		synchronized(allLogs) {
			allLogs.add(al);
		}
		return al;
	}
	
	public final ThreadLocal<ArrayList<LogLine>> log = ThreadLocal.withInitial(()-> getFreshLog());
	public final String taskName;
	public final String entryName;
	
	public Entry(String taskName, String entryName) {
		super();
		this.taskName = taskName;
		this.entryName = entryName;
	}

	void run(Communication comm, String threadName) {
		log.get().add(new LogLine.Begin(taskName, entryName, comm.client, System.currentTimeMillis()));
		comm.threadName = threadName;
		service(comm);
	}
	
	public abstract void service(Communication comm);
	
	public Stream<LogLine> getLog(){
		Stream<LogLine> ans;
		synchronized(allLogs) {
			ans = allLogs.stream().flatMap((al)->al.stream());
		}
		return ans;
	}
	

	public void clearLog(){
		synchronized(allLogs) {
			for(var al:allLogs)
				al.clear();
		}
	}
}
