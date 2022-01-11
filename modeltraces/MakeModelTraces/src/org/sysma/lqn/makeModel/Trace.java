package org.sysma.lqn.makeModel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.xml.Activity;
import org.sysma.lqn.xml.Precedence;
import org.sysma.lqnexecutor.model.Instruction;
import org.sysma.lqnexecutor.model.TimeDistribution;
import org.sysma.schedulerExecutor.LogLine;
import org.sysma.schedulerExecutor.LogLine.ForwardReg;
import org.sysma.schedulerExecutor.LogLine.Replied;

public class Trace {
	private static int progName = 0;
	static String getFreshName() {
		return "N"+(progName++);
	}
	
	private static String loglinedescint(LogLine ll) {
		return ll.switch_(
				(begin)->"b",
				(end)->"e",
				(resume)->"r",
				(calx)->"c"+calx.calledTaskName+"."+calx.calledEntryName,
				(replied)->"",
				(waitfor)->"w"+waitfor.calledTaskName+"."+waitfor.calledEntryName,
				(query_call)->"qc."+query_call.database+"."+query_call.queryName,
				(query_resume)->"qr."+query_resume.database+"."+query_resume.queryName,
				(calxReg)->"cr."+calxReg.calledTaskName+"."+calxReg.calledEntryName,
				(fwdReg)->"",
				(waitforReg)->"wr."+waitforReg.calledTaskName+"."+waitforReg.calledEntryName,
				(fwdcalxReg)->"fcr."+fwdcalxReg.calledTaskName+"."+fwdcalxReg.calledEntryName,
				(freg)->"fr."+freg.calledTaskName+"."+freg.calledEntryName
			);
	}
	public static String getDesc(List<LogLine> trace) {
		return trace.stream().filter(ll->!(ll instanceof Replied) && !(ll instanceof ForwardReg))
				.map(Trace::loglinedescint).collect(Collectors.joining(","));
	}
	
	@SafeVarargs
	private static <X> ArrayList<X> listof(X... xs){
		ArrayList<X> al = new ArrayList<>();
		for(var xx : xs)
			al.add(xx);
		return al;
	}
	
	public Activity getLastAct() {
		return this.activities.get(this.activities.size()-1);
	}
	
	private Trace(ArrayList<Activity> activities, 
			ArrayList<Precedence> precedences, 
			HashMap<Activity, List<Long>> actTimes, 
			ArrayList<Activity> sequence,
			HashMap<String, HashMap<String, List<Long>>> dbQueryTimes,
			HashMap<Activity, List<org.sysma.lqnexecutor.model.Instruction>> activitiesToLqne,
			HashMap<String, List<Long>> netSend,
			HashMap<String, List<Long>> netRcv,
			ArrayList<Long> timeBetweenRegCalls,
			HashMap<Activity, List<Long>> actFwdAfterTimes) {
		this.activities = activities;
		this.precedences = precedences;
		this.actTimes = actTimes;
		this.sequence = sequence;
		this.dbQueryTimes = dbQueryTimes;
		this.activitiesToLqne = activitiesToLqne;
		this.netSend = netSend;
		this.netRcv = netRcv;
		this.timeBetweenRegCalls = timeBetweenRegCalls;
		this.actFwdAfterTimes = actFwdAfterTimes;
	}
	
	public void addExecution(List<LogLine> trace) {
		long[] lastTime = {-1};
		int[] idx = {0};
		for(var ll:trace) {
			ll.switch_(
				(begin)->{},
				(end)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(resume)->{},
				(calx)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(replied)->{
					String dest = replied.calledTaskName+"-"+replied.calledEntryName;
					netSend.computeIfAbsent(dest, x->listof()).add(replied.networkTimeSnd);
					netRcv.computeIfAbsent(dest, x->listof()).add(replied.networkTimeRcv);
				},
				(waitfor)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(query_call)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(query_resume)->{
					actTimes.get(this.sequence.get(idx[0]++)).add(0L);
					dbQueryTimes.get(query_resume.database)
					.get(query_resume.queryName).add(ll.time - lastTime[0]);},
				
				(calxReg)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(fwdReg)->{
					String dest = "registry-Query";
					timeBetweenRegCalls.add(fwdReg.sendToFinalDestTime-fwdReg.rcvTime);
					netSend.computeIfAbsent(dest, x->listof()).add(fwdReg.networkTimeSnd);
					netRcv.computeIfAbsent(dest, x->listof()).add(fwdReg.networkTimeRcv);
				},
				(waitforReg)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				
				(fwdcalxReg)->{actTimes.get(this.sequence.get(idx[0]++)).add(ll.time-lastTime[0]);},
				(freg)->{
					String dest = freg.calledTaskName+"-"+freg.calledEntryName;
					
					actTimes.get(this.sequence.get(idx[0]++)).add(freg.reg_sendToRegTime-lastTime[0]);//prev
					idx[0]++;//callReg
					actTimes.get(this.sequence.get(idx[0]++)).add(freg.call_sendToDestTime-freg.reg_rcvAtSrcTime);//callDest
					actFwdAfterTimes.get(this.sequence.get(idx[0]-1)).add(freg.time-freg.call_rcvAtSrcTime);//callDest
					//if(dest.equals("web-web-History"))
					String src = freg.taskName+"-"+freg.entryName;
					System.out.println(src+"   "+(freg.time-freg.call_rcvAtSrcTime));
					
					netSend.computeIfAbsent(dest, x->listof()).add(freg.call_rcvDestTime-freg.call_sendToDestTime);
					netRcv.computeIfAbsent(dest, x->listof()).add(freg.call_rcvAtSrcTime-freg.call_sendToSrcTime);
					netSend.computeIfAbsent("registry-Query", x->listof()).add(freg.reg_rcvRegTime-freg.reg_sendToRegTime);
					netRcv.computeIfAbsent("registry-Query", x->listof()).add(freg.reg_rcvAtSrcTime-freg.reg_sendToSrcTime);
				}
			);
			if(!(ll instanceof ForwardReg))
				lastTime[0] = ll.time;
		}
	}
	
	public void compile() {
		var regSend = (float) (this.netSend.getOrDefault("registry-Query", listof(0L)).stream()
				.collect(Collectors.averagingDouble(x->x/1000.0))/1.0);
		var regRcv = (float) (this.netRcv.getOrDefault("registry-Query", listof(0L)).stream()
				.collect(Collectors.averagingDouble(x->x/1000.0))/1.0);
		//System.out.println();
		for(var act:activities) {
			float[] mean = {0f, 0f};
			int[] n = {0};
			actTimes.get(act).stream().forEach(x->{
				n[0]++;
				mean[0]+=1f*x;
				mean[1]+=1f*x*x;
			});
			mean[0] /= 1000f * n[0];
			mean[1] /= 1000f * 1000f * n[0];
			float variance = mean[1] - mean[0]*mean[0];
			if(variance < 0 && variance>-1e-6)
				variance = 0;
			float cvsq = variance/n[0]/n[0];
			act.host_demand_mean = mean[0];
			act.host_demand_cvsq = cvsq;
			if(act.whoCall != null && this.netSend.containsKey(act.whoCall)) {
				act.netSendTime = (float) (this.netSend.get(act.whoCall).stream()
						.collect(Collectors.averagingDouble(x->x/1000.0))/1.0);
			}
			if(act.whoCall != null && this.netRcv.containsKey(act.whoCall)) {
				act.netRcvTime = (float) (this.netRcv.get(act.whoCall).stream()
						.collect(Collectors.averagingDouble(x->x/1000.0))/1.0);
			}
			if(this.actFwdAfterTimes.containsKey(act)) {
				act.fwdAfterTime = (float) (this.actFwdAfterTimes.get(act).stream()
						.collect(Collectors.averagingDouble(x->x/1000.0))/1.0);
			}
			List<Instruction> actLqne;
			if((actLqne = activitiesToLqne.get(act))!=null) {
				if(actLqne.size()>0 && 
						actLqne.get(actLqne.size()-1) instanceof org.sysma.lqnexecutor.model.Busy) {
					var busy = (org.sysma.lqnexecutor.model.Busy) actLqne.get(actLqne.size()-1);
					/*busy.setRuntime(new TimeDistribution.PositiveNormal(
							Duration.ofNanos((long)(mean[0]*1000_000_000L)), 
							Duration.ofNanos((long)(Math.sqrt(variance)*1000_000_000L))));*/
					busy.setRuntime(new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(mean[0]*1000_000_000L))));
				}
				if(actLqne.size()>0 && 
						actLqne.get(0) instanceof org.sysma.lqnexecutor.model.Call) {
					var call = (org.sysma.lqnexecutor.model.Call) actLqne.get(0);
					call.netSendTime = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netSendTime*1000_000_000L)));
					call.netRcvTime = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netRcvTime*1000_000_000L)));
				}
				if(actLqne.size()>0 && 
						actLqne.get(0) instanceof org.sysma.lqnexecutor.model.CallWithReg) {
					var call = (org.sysma.lqnexecutor.model.CallWithReg) actLqne.get(0);
					
					call.netSendTimeReg = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(regSend*1000_000_000L)));
					call.netRcvTimeReg = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(regRcv*1000_000_000L)));
					
					call.netSendTimeCall = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netSendTime*1000_000_000L)));
					call.netRcvTimeCall = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netRcvTime*1000_000_000L)));
				}
				if(actLqne.size()>0 && 
						actLqne.get(0) instanceof org.sysma.lqnexecutor.model.ForwardCallWithReg) {
					var call = (org.sysma.lqnexecutor.model.ForwardCallWithReg) actLqne.get(0);
					
					call.netSendTimeReg = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(regSend*1000_000_000L)));
					call.netRcvTimeReg = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(regRcv*1000_000_000L)));
					
					call.netSendTimeCall = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netSendTime*1000_000_000L)));
					call.netRcvTimeCall = new TimeDistribution.Deterministic(
							Duration.ofNanos((long)(act.netRcvTime*1000_000_000L)));
				}
			}
		}
	}
	
	public Instruction[] getLqneCode() {
		return sequence.stream()
			.flatMap(i->activitiesToLqne.get(i).stream())
			.toArray(Instruction[]::new);
	}
	
	public final ArrayList<Activity> activities;
	public final ArrayList<Precedence> precedences;
	private final HashMap<Activity, List<Long>> actTimes;
	private final ArrayList<Activity> sequence;
	private final HashMap<String, HashMap<String, List<Long>>> dbQueryTimes;
	private final HashMap<Activity, List<org.sysma.lqnexecutor.model.Instruction>> activitiesToLqne;
	private final HashMap<String, List<Long>> netSend;
	private final HashMap<String, List<Long>> netRcv;
	private final ArrayList<Long> timeBetweenRegCalls;
	private final HashMap<Activity, List<Long>> actFwdAfterTimes;
	
	public static Trace from(List<LogLine> trace, HashMap<String, HashMap<String, List<Long>>> dbQueryTimes) {
		
		
		Activity[] first = {null};
		Activity[] last = {null};
		ArrayList<Activity> sequence = new ArrayList<>();
		ArrayList<Activity> activities = new ArrayList<>();
		ArrayList<Precedence> precedences = new ArrayList<>();
		HashMap<Activity, List<Long>> actTimes = new HashMap<>();
		HashMap<Activity, List<Long>> actFwdAfterTimes = new HashMap<>();
		HashMap<String, List<Long>> netSend = new HashMap<>();
		HashMap<String, List<Long>> netRcv = new HashMap<>();
		HashMap<String, Activity> whoCalled = new HashMap<>();
		
		HashMap<Activity, List<org.sysma.lqnexecutor.model.Instruction>> activitiesToLqne = new HashMap<>();
		
		ArrayList<Long> timeBetweenRegCalls = new ArrayList<>();
		long[] lastTime = {-1};
		for(var ll:trace) {
			ll.switch_(
				(begin)->{
					last[0] = first[0] = new Activity(getFreshName(), -1,-1, 1, null, false);
					activities.add(first[0]);
					
					var busy = new org.sysma.lqnexecutor.model.Busy();
					busy.setName(first[0].name);
					activitiesToLqne.put(first[0], List.of(busy));
				},
				(end)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
				},
				(resume)->{},
				(calx)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					Activity then = new Activity(getFreshName(), -1,-1, 1, null, false);
					Activity call = new Activity(getFreshName(), 0,1, 1, null, false);
					actTimes.put(call, listof(0L));
					String dest = calx.calledTaskName+"-"+calx.calledEntryName;
					call.whoCall = dest;
					/*precedences.add(new Precedence("pre", new String[] {last.name}, 
							"post-AND", new String[] {then.name, call.name}, null));*/
					precedences.add(new Precedence().pre(last[0]).postAnd(then, call));
					activities.add(then);
					activities.add(call);
					whoCalled.put(dest, call);
					last[0] = then;

					var lecall = new org.sysma.lqnexecutor.model.Call();
					lecall.setSaveVar(dest);
					lecall.setTaskName(calx.calledTaskName);
					lecall.setEntryName(dest);
					lecall.setName(call.name);
					var lethen = new org.sysma.lqnexecutor.model.Busy();
					lethen.setName(then.name);
					activitiesToLqne.put(last[0], List.of(lecall, lethen));
				},
				(replied)->{
					String dest = replied.calledTaskName+"-"+replied.calledEntryName;
					netSend.computeIfAbsent(dest, x->listof()).add(replied.networkTimeSnd);
					netRcv.computeIfAbsent(dest, x->listof()).add(replied.networkTimeRcv);
				},
				(waitfor)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					String dest = waitfor.calledTaskName+"-"+waitfor.calledEntryName;
					Activity thenW = new Activity(getFreshName(), -1,-1, 1, null, false);
					/*precedences.add(new Precedence("pre-AND", new String[] {last.name, whoCalled.get(ll.argument).name}, 
							"post", new String[] {thenW.name}, null));*/
					precedences.add(new Precedence().preAnd(last[0], whoCalled.get(dest)).post(thenW));
					activities.add(thenW);
					last[0] = thenW;
					
					var lewf = new org.sysma.lqnexecutor.model.WaitFor();
					lewf.setCallVar(dest);
					lewf.setName("join-"+thenW.name);
					var lethenW = new org.sysma.lqnexecutor.model.Busy();
					lethenW.setName(thenW.name);
					activitiesToLqne.put(last[0], List.of(lewf, lethenW));
				
				},
				(query_call)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					Activity qCall = new Activity(getFreshName(), 0,1, 1, null, false);
					String dest = query_call.database+"-"+query_call.queryName;
					qCall.whoCall = dest;
					precedences.add(new Precedence().pre(last[0]).post(qCall));
					activities.add(qCall);
					last[0] = qCall;

					var lecall = new org.sysma.lqnexecutor.model.Call();
					lecall.setSaveVar(dest);
					lecall.setTaskName(query_call.database);
					lecall.setEntryName(dest);
					lecall.setName(qCall.name);
					activitiesToLqne.put(last[0], List.of(lecall));
				},
				(query_resume)->{
					String toJoinName = last[0].name;
					actTimes.put(last[0], listof(0L));
					sequence.add(last[0]);
					dbQueryTimes.get(query_resume.database)
						.get(query_resume.queryName).add(ll.time - lastTime[0]);
					Activity thenQC = new Activity(getFreshName(), -1,-1, 1, null, false);
					precedences.add(new Precedence().pre(last[0]).post(thenQC));
					activities.add(thenQC);
					last[0] = thenQC;
					
					var lewf = new org.sysma.lqnexecutor.model.WaitFor();
					lewf.setCallVar(query_resume.database+"-"+query_resume.queryName);
					lewf.setName("join-"+toJoinName);
					var lethenW = new org.sysma.lqnexecutor.model.Busy();
					lethenW.setName(thenQC.name);
					activitiesToLqne.put(last[0], List.of(lewf, lethenW));
				},
				(calxReg)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					Activity then = new Activity(getFreshName(), -1,-1, 1, null, false);
					Activity regcall = new Activity(getFreshName(), 0,1, 1, null, false);
					Activity call = new Activity(getFreshName(), -1,1, 1, null, false);
					actTimes.put(regcall, listof(0L));
					actTimes.put(call, timeBetweenRegCalls);
					String dest = calxReg.calledTaskName+"-"+calxReg.calledEntryName;
					regcall.whoCall = "registry-Query";
					call.whoCall = dest;
					/*precedences.add(new Precedence("pre", new String[] {last.name}, 
							"post-AND", new String[] {then.name, call.name}, null));*/
					precedences.add(new Precedence().pre(last[0]).postAnd(then, regcall));
					precedences.add(new Precedence().pre(regcall).post(call));
					activities.add(then);
					activities.add(regcall);
					activities.add(call);
					whoCalled.put(dest, call);
					last[0] = then;

					var lecall = new org.sysma.lqnexecutor.model.CallWithReg();
					lecall.setSaveVar(dest);
					lecall.setTaskName(calxReg.calledTaskName);
					lecall.setEntryName(dest);
					lecall.setName(call.name);
					var lethen = new org.sysma.lqnexecutor.model.Busy();
					lethen.setName(then.name);
					activitiesToLqne.put(last[0], List.of(lecall, lethen));
				},
				(fwdReg)->{
					String dest = "registry-Query";
					timeBetweenRegCalls.add(fwdReg.sendToFinalDestTime-fwdReg.rcvTime);
					netSend.computeIfAbsent(dest, x->listof()).add(fwdReg.networkTimeSnd);
					netRcv.computeIfAbsent(dest, x->listof()).add(fwdReg.networkTimeRcv);
				},
				(waitforReg)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					String dest = waitforReg.calledTaskName+"-"+waitforReg.calledEntryName;
					Activity thenW = new Activity(getFreshName(), -1,-1, 1, null, false);
					/*precedences.add(new Precedence("pre-AND", new String[] {last.name, whoCalled.get(ll.argument).name}, 
							"post", new String[] {thenW.name}, null));*/
					precedences.add(new Precedence().preAnd(last[0], whoCalled.get(dest)).post(thenW));
					activities.add(thenW);
					last[0] = thenW;
					
					var lewf = new org.sysma.lqnexecutor.model.WaitForWithReg();
					lewf.setCallVar(dest);
					lewf.setName("join-"+thenW.name);
					var lethenW = new org.sysma.lqnexecutor.model.Busy();
					lethenW.setName(thenW.name);
					activitiesToLqne.put(last[0], List.of(lewf, lethenW));
				
				},
				(fwdcalxReg)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					//Activity then = new Activity(getFreshName(), 0,0, 1, null);
					Activity regcall = new Activity(getFreshName(), 0,1, 1, null, false);
					Activity call = new Activity(getFreshName(), -1,1, 1, null, true);
					//actTimes.put(then, listof(0L));
					actTimes.put(regcall, listof(0L));
					actTimes.put(call, timeBetweenRegCalls);
					String dest = fwdcalxReg.calledTaskName+"-"+fwdcalxReg.calledEntryName;
					regcall.whoCall = "registry-Query";
					call.whoCall = dest;
					/*precedences.add(new Precedence("pre", new String[] {last.name}, 
							"post-AND", new String[] {then.name, call.name}, null));*/
					precedences.add(new Precedence().pre(last[0]).post(regcall));
					precedences.add(new Precedence().pre(regcall).post(call));
					//activities.add(then);
					activities.add(regcall);
					activities.add(call);
					whoCalled.put(dest, call);
					last[0] = null;//then;

					var lecall = new org.sysma.lqnexecutor.model.ForwardCallWithReg();
					lecall.setTaskName(fwdcalxReg.calledTaskName);
					lecall.setEntryName(dest);
					lecall.setName(call.name);
					//var lethen = new org.sysma.lqnexecutor.model.Busy();
					//lethen.setName(then.name);
					activitiesToLqne.put(last[0], List.of(lecall/*, lethen*/));
				},
				(freg)->{
					actTimes.put(last[0], listof(ll.time-lastTime[0]));
					sequence.add(last[0]);
					Activity regcall = new Activity(getFreshName(), 0,1, 1, null, false);
					Activity call = new Activity(getFreshName(), -1,1, 1, null, true);
					activities.add(regcall);
					activities.add(call);
					actTimes.put(regcall, listof(0L));
					actTimes.put(call, listof(freg.call_sendToDestTime-freg.reg_rcvAtSrcTime));
					actFwdAfterTimes.put(call, listof(freg.time-freg.call_rcvAtSrcTime));
					String src = freg.taskName+"-"+freg.entryName;
					System.out.println(src+"   "+(freg.time-freg.call_rcvAtSrcTime));
					
					String dest = freg.calledTaskName+"-"+freg.calledEntryName;
					regcall.whoCall = "registry-Query";
					call.whoCall = dest;
					precedences.add(new Precedence().pre(last[0]).post(regcall));
					precedences.add(new Precedence().pre(regcall).post(call));
					sequence.add(regcall);
					sequence.add(call);
					last[0] = call;
					netSend.computeIfAbsent(dest, x->listof()).add(freg.call_rcvDestTime-freg.call_sendToDestTime);
					netRcv.computeIfAbsent(dest, x->listof()).add(freg.call_rcvAtSrcTime-freg.call_sendToSrcTime);
					netSend.computeIfAbsent("registry-Query", x->listof()).add(freg.reg_rcvRegTime-freg.reg_sendToRegTime);
					netRcv.computeIfAbsent("registry-Query", x->listof()).add(freg.reg_rcvAtSrcTime-freg.reg_sendToSrcTime);
					
					var lecall = new org.sysma.lqnexecutor.model.ForwardCallWithReg();
					lecall.setTaskName(freg.calledTaskName);
					lecall.setEntryName(dest);
					lecall.setName(call.name);
					activitiesToLqne.put(last[0], List.of(lecall/*, lethen*/));
				}
				);
			lastTime[0] = ll.time;
		}
		return new Trace(activities, precedences, actTimes, sequence, dbQueryTimes, activitiesToLqne,
				netSend, netRcv, timeBetweenRegCalls, actFwdAfterTimes);
	}
}
