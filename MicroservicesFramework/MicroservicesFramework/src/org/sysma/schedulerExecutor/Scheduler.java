package org.sysma.schedulerExecutor;

import java.io.IOException;
import java.util.LinkedList;

import com.sun.net.httpserver.HttpExchange;

public abstract class Scheduler/*<T extends IncomingRequest>*/ {
	private int maxWorkers;
	private int activeWorkers;
	
	public final String coresToUse;
	

	
	private final LinkedList<Integer> freeThreads = new LinkedList<>();
	private int neverAssigned = 0;
	private int popThreadIdx() {
		if(freeThreads.isEmpty())
			return neverAssigned++;
		else
			return freeThreads.pop();
	}
	private void releaseThreadIdx(int t) {
		freeThreads.push(t);
	}
	
	public Scheduler(int maxWorkers) {
		this(maxWorkers, null);
	}
	
	public Scheduler(int maxWorkers, int[] coresToUse) {
		this.maxWorkers = maxWorkers;
		this.activeWorkers = 0;
		//this.active = true;
		this.coresToUse = coresToString(coresToUse);
	}
	
	private static String coresToString(int[] cores) {
		if(cores == null)
			return null;
		StringBuilder sb = new StringBuilder(",");
		for(int c:cores)
			sb.append(c).append(",");
		return sb.substring(0, sb.length()-1);
	}
	
	public void callTaskset() throws IOException, InterruptedException {
		if(coresToUse != null) {
			long pid = Thread.currentThread().getId();
			String command = "taskset -p "+pid+" -c "+this.coresToUse;
			Process process = Runtime.getRuntime().exec(command);
			if(process.waitFor() != 0) {
				int exit = process.exitValue();
				String err = new String(process.getErrorStream().readAllBytes());
				throw new Error("Taskset failed with error "+exit+": "+err);
			}
		}
	}

	public void addJob(Entry job, HttpExchange request, long time) {
		String client = ""+((int)(Math.random()*Integer.MAX_VALUE));
		Communication comm = new Communication(job, request, this, client, time);
		//System.out.println(System.currentTimeMillis()+" commStart "+System.identityHashCode(comm));
		comm.start();
	}
	
	public void jobHalted(Communication communication) {
		var tname = communication.threadName.split("-");
		int tIdx = Integer.parseInt(tname[tname.length-1]);
		//if(this.activeWorkers == 0)
		//	throw new Error("No job is active, but called jobHalted!");
		//var start = System.currentTimeMillis();
		synchronized(this) {
			//var started = System.currentTimeMillis();
			//System.out.println("Scheduler.jobHalted; "+(started-start));
			this.releaseThreadIdx(tIdx);
			this.activeWorkers--;
		}
		//System.out.println(System.currentTimeMillis()+" Job finished" );
		//if(this.activeWorkers == 0)
		//this.notifyAll();
		startJobs();
	}
	
	public void addWaitingJob(Communication comm) {
		//if(this.active) {
		//System.out.println(System.currentTimeMillis()+" New job "+System.identityHashCode(comm) );
		this._addWaitingJob(comm);
		startJobs();
		//}
	}
	
	public void stop() {
		//this.active = false;
		/*while(this.activeWorkers > 0 || !isEmpty()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
	
	protected abstract void _addWaitingJob(Communication comm);
	
	public abstract void updateClass(Communication comm);
	
	//public abstract double getClassWeight(Object class_);
	//public abstract void setClassWeight(Object class_, double w);
	
	private void startJobs() {
		while(true) {
			boolean cont = false;
			int awOld = -1;
			boolean emp = false;
			Communication job = null;
			int tIdx = -1;
			//var start = System.currentTimeMillis();
			synchronized(this) {
				//var started = System.currentTimeMillis();
				//System.out.println("Scheduler.startJobs; "+(started-start));
				emp = isEmpty();
				awOld = activeWorkers;
				cont = !emp && awOld < maxWorkers;
				if(cont) {
					activeWorkers++;
					job = popJob();
					tIdx = this.popThreadIdx();
				}
			}
			//System.out.print(System.currentTimeMillis()+" Active: "+awOld+"/"+maxWorkers + " empty:"+emp );
			if(!cont)
				break;
			job.threadName = this.hashCode()+"-"+tIdx;
			job.resume();
			//System.out.println(" Started job!");
		}
		//System.out.println(" No start!");
	}
	
	protected abstract Communication popJob();
	
	protected abstract boolean isEmpty();

	public void setMaxWorkers(int maxWorkers) {
		synchronized(this) {
			this.maxWorkers = maxWorkers;
		}
		startJobs();
	}

	public synchronized int getMaxWorkers() {
		return this.maxWorkers;
	}

	public synchronized int getActiveWorkers() {
		return this.activeWorkers;
	}

}
