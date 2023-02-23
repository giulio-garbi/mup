package org.sysma.lqnxsim.exec;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.lqnxsim.model.Activity;
import org.sysma.lqnxsim.model.ForwardingActivity;
import org.sysma.lqnxsim.model.PreAnd;
import org.sysma.lqnxsim.model.Precedence;

public class Task {
	public static class Runtime {
		private Duration pastRuntime = Duration.ZERO;
		private int busyNow = 0;
		private Instant busySince = Instant.EPOCH;
		
		public void inc(Instant now) {
			pastRuntime = pastRuntime.plus(Duration.between(busySince, now).multipliedBy(busyNow));
			busyNow++;
			busySince = now;
		}
		
		public void dec(Instant now) {
			pastRuntime = pastRuntime.plus(Duration.between(busySince, now).multipliedBy(busyNow));
			busyNow--;
			busySince = now;
		}
		
		public Duration runtime(Instant now) {
			return pastRuntime.plus(Duration.between(busySince, now).multipliedBy(busyNow));
		}
		
		public double avgCpu(Instant now) {
			var rt = runtime(now);
			var rtNanos = rt.toNanos();
			var nowNanos = Duration.between(Instant.EPOCH, now).toNanos();
			return BigDecimal.valueOf(rtNanos).divide(BigDecimal.valueOf(nowNanos), MathContext.DECIMAL128).doubleValue();
		}
		
		public double avgCpuComplete(Instant now) {
			var rt = pastRuntime;
			var rtNanos = rt.toNanos();
			var nowNanos = Duration.between(Instant.EPOCH, now).toNanos();
			return BigDecimal.valueOf(rtNanos).divide(BigDecimal.valueOf(nowNanos), MathContext.DECIMAL128).doubleValue();
		}
	}
	
	public Task(org.sysma.lqnxsim.model.Task taskDesc, org.sysma.lqnxsim.exec.LqnModel model) {
		this.name = taskDesc.name;
		this.model = model;
		this.freeThreads = taskDesc.multiplicity.equals("inf")?
				Integer.MIN_VALUE:Integer.parseInt(taskDesc.multiplicity);
		this.amClient = "ref".equals(taskDesc.scheduling);
		this.activities = new HashMap<>();
		this.fwdactivities = new HashMap<>();
		this.replyingActivities = new HashSet<>();
		this.precedences = new HashMap<>();
		this.preAndCompleted = new HashMap<>();
		this.startActivity = new HashMap<>();
		
		for(var tact:taskDesc.tActivities) {
			if(tact.activity != null)
				for(var act:tact.activity) {
					activities.put(act.name, act);
					if(act.boundToEntry != null)
						this.startActivity.put(act.boundToEntry, act);
				}
			if(tact.forwarding_activity != null)
				for(var fact:tact.forwarding_activity) {
					fwdactivities.put(fact.name, fact);
				}
			if(tact.replyEntries != null)
				for(var re:tact.replyEntries) {
					for(var ract:re.replyActivities) {
						this.replyingActivities.add(ract.name);
					}
				}
			if(tact.precedences != null)
				for(var precs:tact.precedences) {
					if(precs.pre != null)
						for(var pre: precs.pre) 
							for(var ract:pre.activities)
								this.precedences.put(ract.name, precs);
					if(precs.preAnd != null)
						for(var pre: precs.preAnd) 
							for(var ract:pre.activities)
								this.precedences.put(ract.name, precs);
					if(precs.preOr != null)
						for(var pre: precs.preOr) 
							for(var ract:pre.activities)
								this.precedences.put(ract.name, precs);
				}
		}
	}
	
	public static class ExternalCall<X>{
		String entry; 
		X initiated;
		
		Instant beganServingAt;

		public ExternalCall(String entry, X initiated, Instant beganServingAt) {
			super();
			this.entry = entry;
			this.initiated = initiated;
			this.beganServingAt = beganServingAt;
		}
	}
	
	final LqnModel model;
	String name;
	int freeThreads;
	ArrayList<ExternalCall<?>> pendingCalls = new ArrayList<>();
	boolean amClient;
	Runtime cpuRuntime = new Runtime();
	
	HashMap<String, Activity> activities;
	HashMap<String, ForwardingActivity> fwdactivities;
	HashMap<String, Activity> startActivity;
	HashMap<String, ForwardingActivity> fwdStartActivity;
	HashSet<String> replyingActivities;
	HashMap<String, Precedence> precedences;
	HashMap<ExternalCall<?>, HashMap<PreAnd, HashSet<Activity>>> preAndCompleted;
	
	long nextToken = 0;
	
	public HashMap<String, ArrayList<Duration>> responseTimes = new HashMap<>();
	
	private static Duration expd(long nanos) {
		long nn = (long)(-Math.log(1-ThreadLocalRandom.current().nextDouble())*nanos);
	    return  Duration.ofNanos(nn);
	}
	
	public void call(String entry, CallEvent initiated) {
		var client = new ExternalCall<>(entry, initiated, null);
		if(freeThreads > 0) {
			freeThreads--;
			actuallyStartCall(client);
		} else if(freeThreads < -1000) {
			actuallyStartCall(client);
		} else {
			pendingCalls.add(client);
		}
	}
	
	public void call(String entry, FwdCallEvent initiated) {
		var client = new ExternalCall<>(entry, initiated, null);
		if(freeThreads > 0) {
			freeThreads--;
			actuallyStartCall(client);
		} else if(freeThreads < -1000) {
			actuallyStartCall(client);
		} else {
			pendingCalls.add(client);
		}
	}
	
	private void actuallyStartCall(ExternalCall<?> client) {
		var now = model.getClock();
		cpuRuntime.inc(now);
		client.beganServingAt = now;
		if(this.startActivity.containsKey(client.entry))
			this.doActivity(this.startActivity.get(client.entry), client);
		else
			this.doFwdActivity(this.fwdStartActivity.get(client.entry), client);
	}
	
	public void advance(BusyEvent evt) {
		if(evt.activity.call != null && evt.activity.call.length > 0) {
			var netSendTime = expd((long)((evt.activity.call[0].netSendTime) * 1000_000_000L));
			CallEvent call = new CallEvent(evt.task, evt.client, model.getClock().plus(netSendTime), evt.activity, 0);
			model.event(call);
		} else {
			terminated(evt.activity, evt.client);
		}
	}
	public void response(CallEvent evt) {
		if(evt.activity.call.length > evt.which+1) {
			var netSendTime = expd((long)((evt.activity.call[evt.which+1].netSendTime) * 1000_000_000L));
			CallEvent call = new CallEvent(evt.task, evt.client, model.getClock().plus(netSendTime), evt.activity, evt.which+1);
			model.event(call);
		} else {
			terminated(evt.activity, evt.client);
		}
	}
	private void newResponseTime(String entry, Duration d) {
		this.responseTimes.computeIfAbsent(entry, x->new ArrayList<>()).add(d);
	}
	public void response(FwdCallEvent evt) {
		newResponseTime(evt.client.entry, Duration.between(evt.client.beganServingAt, model.getClock()));
		var netRcvTime = getNetRcvTime(evt.client.initiated);
		var respond = new ResponseEvent<>(evt.client.initiated, model.getClock().plus(netRcvTime));
		model.event(respond);
	}
	
	public void startAllClients() {
		if(amClient)
			for(int i=0; i<this.freeThreads; i++){
				//System.out.println(startActivity.size());
				var client = new ExternalCall<>(startActivity.keySet().iterator().next(), null, null);
				this.actuallyStartCall(client);
			}
	}
	public void advance(FreeThreadEvent evt) {
		cpuRuntime.dec(model.getClock());
		if(this.amClient) {
			var client = new ExternalCall<>(startActivity.keySet().iterator().next(), null, null);
			this.actuallyStartCall(client);
		} else if(this.freeThreads > -1000) {
			this.freeThreads++;
			while(!pendingCalls.isEmpty() && this.freeThreads>0) {
				this.freeThreads--;
				this.actuallyStartCall(pendingCalls.remove(0));
			}
		}
	}
	
	private static Duration getNetRcvTime(Object x) {
		if(x == null)
			return Duration.ZERO;
		else if(x instanceof CallEvent) {
			var ce = (CallEvent)x;
			return expd((long)((ce.activity.call[ce.which].netRcvTime) * 1000_000_000L));
		}
		else if(x instanceof FwdCallEvent) {
			var ce = (FwdCallEvent)x;
			return expd((long)((ce.call.netRcvTime) * 1000_000_000L));
		}
		else {
			throw new Error();
		}
	}
	
	private void terminated(Activity act, ExternalCall<?> client) {
		if(replyingActivities.contains(act.name)) {
			newResponseTime(client.entry, Duration.between(client.beganServingAt, model.getClock()));
			var netRcvTime = getNetRcvTime(client.initiated);
			var revt = new ResponseEvent<>(client.initiated, model.getClock().plus(netRcvTime));
			model.event(revt);
			FreeThreadEvent fte = new FreeThreadEvent(this, model.getClock());
			model.event(fte);
		}
		if(precedences.containsKey(act.name)) {
			if(replyingActivities.contains(act.name))
				throw new Error();
			var pres = precedences.get(act.name);
			if(pres.preAnd != null && pres.preAnd.length > 0) {
				var preAnd = pres.preAnd[0];
				var alreadyCompleted = preAndCompleted.computeIfAbsent(client, x->new HashMap<>())
					.computeIfAbsent(preAnd, x->new HashSet<>());
				assert(!alreadyCompleted.contains(act));
				alreadyCompleted.add(act);
				if(alreadyCompleted.size()!=preAnd.activities.length)
					return;
				preAndCompleted.get(client).remove(preAnd);
			}
			if(pres.post != null && pres.post.length > 0) {
				this.doAct(pres.post[0].activities[0].name, client);
			} else if(pres.postOr != null && pres.postOr.length > 0) {
				double choice = model.getRandom().nextDouble();
				int idx = pres.postOr[0].activities.length-1;
				for(int i=0; i<pres.postOr[0].activities.length; i++) {
					if(choice < pres.postOr[0].activities[i].prob) {
						idx = i; break;
					} else {
						choice -= pres.postOr[0].activities[i].prob;
					}
				}
				this.doAct(pres.postOr[0].activities[idx].name, client);
			} else if (pres.postAnd != null && pres.postAnd.length > 0) {
				for(int i=0; i<pres.postAnd[0].activities.length; i++) {
					this.doAct(pres.postAnd[0].activities[i].name, client);
				}
			} else {
				//FreeThreadEvent fte = new FreeThreadEvent(this, model.getClock());
				//model.event(fte);
				//newResponseTime(client.entry, Duration.between(client.beganServingAt, model.getClock()));
			}
		} else {
			if(this.amClient) {
				FreeThreadEvent fte = new FreeThreadEvent(this, model.getClock());
				model.event(fte);
				newResponseTime(client.entry, Duration.between(client.beganServingAt, model.getClock()));
				/*if(this.name.equals("FE")) {
					var rts = this.responseTimes.get("catalog");
					System.out.println(rts.get(rts.size()-1).toMillis());
				}*/
			}
		}
	}
	
	private void doAct(String name, ExternalCall<?> client) {
		if(this.activities.containsKey(name))
			this.doActivity(this.activities.get(name), client);
		else
			this.doFwdActivity(this.fwdactivities.get(name), client);
	}
	
	private void doFwdActivity(ForwardingActivity fwdAct, ExternalCall<?> client) {
		FreeThreadEvent fte = new FreeThreadEvent(this, model.getClock());
		var netSendTime = expd((long)(((fwdAct.call[0].netSendTime)+fwdAct.fwdAfterTime) * 1000_000_000L));
		FwdCallEvent call = new FwdCallEvent(this, client, model.getClock().plus(netSendTime), fwdAct.call[0]);
		model.event(fte);
		model.event(call);
	}
	
	private void doActivity(Activity act, ExternalCall<?> client) {
		//System.out.println(act.name);
		var timeEndBusy = model.getClock().plus(ActivityUtil.getActivityDuration(model.getRandom(), act)); 
		BusyEvent startEvt = new BusyEvent(this, client, timeEndBusy, act);
		model.event(startEvt);
	}
}
