package org.sysma.schedulerExecutor;

import java.util.function.Consumer;
import java.util.function.Function;

public class LogLine {
	protected final String kind;
	public final String taskName;
	public final String entryName;
	public final String client;
	public final long time;
	
	protected LogLine(String kind, String taskName, String entryName, String client, long time) {
		this.kind = kind;
		this.taskName = taskName;
		this.entryName = entryName;
		this.client = client;
		this.time = time;
	}
	
	public static LogLine fromString(String ln) {
		String[] parts = ln.strip().split(",");
		switch(parts[0]) {
		case "begin":
			return new Begin(parts[1], parts[2], parts[3], Long.parseLong(parts[4]));
		case "end":
			return new End(parts[1], parts[2], parts[3], Long.parseLong(parts[4]));
		case "resume":
			return new Resume(parts[1], parts[2], parts[3], Long.parseLong(parts[4]));
		case "call":
			return new Call(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "call_reg":
			return new CallWithReg(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "fwdcall_reg":
			return new ForwardCallWithReg(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "fwd_reg":
			return new ForwardReg(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]),
					 parts.length>=8 ? Long.parseLong(parts[7]) : -1L, 
						     parts.length>=9 ? Long.parseLong(parts[8]) : -1L,
						     parts.length>=10 ? Long.parseLong(parts[9]) : -1L,
						    parts.length>=11 ? Long.parseLong(parts[10]) : -1L);
		case "replied":
			return new Replied(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]),
					 parts.length>=8 ? Long.parseLong(parts[7]) : -1L, 
				     parts.length>=9 ? Long.parseLong(parts[8]) : -1L,
				     parts.length>=10 ? Long.parseLong(parts[9]) : -1L);
		case "wait":
			return new WaitFor(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "wait_reg":
			return new WaitForWithReg(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "query_call":
			return new QueryCall(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "query_resume":
			return new QueryResume(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]));
		case "freg":
			return new ForwardingReg(parts[1], parts[2], parts[3], parts[4], parts[5], Long.parseLong(parts[6]), 
					Long.parseLong(parts[7]),Long.parseLong(parts[8]),Long.parseLong(parts[9]),Long.parseLong(parts[10]),
					Long.parseLong(parts[11]),Long.parseLong(parts[12]),Long.parseLong(parts[13]),Long.parseLong(parts[14]));
		}
		
		return null;
	}
	
	public <X> X switch_(Function<Begin, X> fBegin,
			Function<End, X> fEnd,
			Function<Resume, X> fResume,
			Function<Call, X> fCall,
			Function<Replied, X> fReplied,
			Function<WaitFor, X> fWaitFor,
			Function<QueryCall, X> fQueryCall,
			Function<QueryResume, X> fQueryResume,
			Function<CallWithReg, X> fCallReg,
			Function<ForwardReg, X> fFwd,
			Function<WaitForWithReg, X> fWaitReg,
			Function<ForwardCallWithReg, X> fFwdCallReg,
			Function<ForwardingReg, X> fFwdReg) {
		switch(this.kind) {
		case "begin":
			return fBegin.apply((Begin)this);
		case "end":
			return fEnd.apply((End)this);
		case "resume":
			return fResume.apply((Resume)this);
		case "call":
			return fCall.apply((Call)this);
		case "call_reg":
			return fCallReg.apply((CallWithReg)this);
		case "fwd_reg":
			return fFwd.apply((ForwardReg)this);
		case "replied":
			return fReplied.apply((Replied)this);
		case "wait":
			return fWaitFor.apply((WaitFor)this);
		case "wait_reg":
			return fWaitReg.apply((WaitForWithReg)this);
		case "query_call":
			return fQueryCall.apply((QueryCall)this);
		case "query_resume":
			return fQueryResume.apply((QueryResume)this);
		case "fwdcall_reg":
			return fFwdCallReg.apply((ForwardCallWithReg)this);
		case "freg":
			return fFwdReg.apply((ForwardingReg)this);
		}
		return null;
	}
	
	public void switch_(Consumer<Begin> fBegin,
			Consumer<End> fEnd,
			Consumer<Resume> fResume,
			Consumer<Call> fCall,
			Consumer<Replied> fReplied,
			Consumer<WaitFor> fWaitFor,
			Consumer<QueryCall> fQueryCall,
			Consumer<QueryResume> fQueryResume,
			Consumer<CallWithReg> fCallReg,
			Consumer<ForwardReg> fFwd,
			Consumer<WaitForWithReg> fWaitReg,
			Consumer<ForwardCallWithReg> fFwdCallReg,
			Consumer<ForwardingReg> fFwdReg) {
		switch(this.kind) {
		case "begin":
			fBegin.accept((Begin)this);
			break;
		case "end":
			fEnd.accept((End)this);
			break;
		case "resume":
			fResume.accept((Resume)this);
			break;
		case "call":
			fCall.accept((Call)this);
			break;
		case "call_reg":
			fCallReg.accept((CallWithReg)this);
			break;
		case "replied":
			fReplied.accept((Replied)this);
			break;
		case "wait":
			fWaitFor.accept((WaitFor)this);
			break;
		case "fwd_reg":
			fFwd.accept((ForwardReg)this);
			break;
		case "wait_reg":
			fWaitReg.accept((WaitForWithReg)this);
			break;
		case "query_call":
			fQueryCall.accept((QueryCall)this);
			break;
		case "query_resume":
			fQueryResume.accept((QueryResume)this);
			break;
		case "fwdcall_reg":
			fFwdCallReg.accept((ForwardCallWithReg)this);
			break;
		case "freg":
			fFwdReg.accept((ForwardingReg)this);
			break;
		}
	}
	
	public String toString() {
		return String.format("%s,%s,%s,%s,%d",kind,taskName, entryName, client, time);
	}
	
	public static class Begin extends LogLine{
		public Begin(String taskName, String entryName, String client, long time){
			super("begin", taskName, entryName, client, time);
		}
	}
	
	public static class End extends LogLine{
		public End(String taskName, String entryName, String client, long time){
			super("end", taskName, entryName, client, time);
		}
	}

	public static class Resume extends LogLine{
		public Resume(String taskName, String entryName, String client, long time){
			super("resume", taskName, entryName, client, time);
		}
	}
	
	public static class Call extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public Call(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time){
			super("call", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time);
		}
	}
	
	public static class CallWithReg extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public CallWithReg(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time){
			super("call_reg", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time);
		}
	}
	
	public static class ForwardCallWithReg extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public ForwardCallWithReg(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time){
			super("fwdcall_reg", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time);
		}
	}
	
	public static class Replied extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public final long networkTimeRcv;
		public final long networkTimeSnd;
		
		public final long sbackTime;
		public final long sendTime;
		public final long rcvTime;
		
		public Replied(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time, 
				long sbackTime, long sendTime, long rcvTime){
			super("replied", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
			if(sbackTime < 0)
				networkTimeRcv = -1;
			else
				networkTimeRcv = time-sbackTime;
			if(sendTime<0 || rcvTime<0)
				networkTimeSnd = -1;
			else
				networkTimeSnd = rcvTime - sendTime;

			this.sbackTime = sbackTime;
			this.sendTime = sendTime;
			this.rcvTime = rcvTime;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d,%d,%d,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time, sbackTime, sendTime, rcvTime);
		}
	}
	
	public static class QueryCall extends LogLine{
		public final String database;
		public final String queryName;
		
		public QueryCall(String callerTaskName, String callerEntryName, 
				String database, String queryName, String client, long time){
			super("query_call", callerTaskName, callerEntryName, client, time);
			this.database = database;
			this.queryName = queryName;
		}
		
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					database, queryName, client, time);
		}
	}
	
	public static class QueryResume extends LogLine{
		public final String database;
		public final String queryName;
		
		public QueryResume(String callerTaskName, String callerEntryName, 
				String database, String queryName, String client, long time){
			super("query_resume", callerTaskName, callerEntryName, client, time);
			this.database = database;
			this.queryName = queryName;
		}
		
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					database, queryName, client, time);
		}
	}
	
	public static class WaitFor extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public WaitFor(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time){
			super("wait", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time);
		}
	}
	
	public static class WaitForWithReg extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		
		public WaitForWithReg(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time){
			super("wait_reg", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time);
		}
	}
	
	public static class ForwardReg extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;
		public final long networkTimeRcv;
		public final long networkTimeSnd;
		
		public final long sbackTime;
		public final long sendTime;
		public final long rcvTime;
		public final long sendToFinalDestTime;
		
		
		public ForwardReg(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long time,
				long sbackTime, long sendTime, long rcvTime, long sendToFinalDestTime){
			super("fwd_reg", callerTaskName, callerEntryName, client, time);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
			if(sbackTime < 0)
				networkTimeRcv = -1;
			else
				networkTimeRcv = time-sbackTime;
			if(sendTime<0 || rcvTime<0)
				networkTimeSnd = -1;
			else
				networkTimeSnd = rcvTime - sendTime;
			
			this.sbackTime = sbackTime;
			this.sendTime = sendTime;
			this.rcvTime = rcvTime;
			this.sendToFinalDestTime = sendToFinalDestTime;
		}
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time, sbackTime, sendTime, rcvTime, 
					sendToFinalDestTime);
		}
	}
	
	public static class ForwardingReg extends LogLine{
		public final String calledTaskName;
		public final String calledEntryName;

		public final long reg_sendToRegTime;
		public final long reg_rcvRegTime;
		public final long reg_sendToSrcTime;
		public final long reg_rcvAtSrcTime;
		// processing time
		public final long call_sendToDestTime;
		public final long call_rcvDestTime;
		public final long call_sendToSrcTime;
		public final long call_rcvAtSrcTime;
		// processing time
		// time = endTime
		
		public ForwardingReg(String callerTaskName, String callerEntryName, 
				String calledTaskName, String calledEntryName, String client, long endTime,
				long reg_sendToRegTime, long reg_rcvRegTime, long reg_sendToSrcTime,
				long reg_rcvAtSrcTime, long call_sendToDestTime, long call_rcvDestTime,
				long call_sendToSrcTime, long call_rcvAtSrcTime){
			super("freg", callerTaskName, callerEntryName, client, endTime);
			this.calledEntryName = calledEntryName;
			this.calledTaskName = calledTaskName;
			
			this.reg_sendToRegTime = reg_sendToRegTime;
			this.reg_rcvRegTime = reg_rcvRegTime;
			this.reg_sendToSrcTime = reg_sendToSrcTime;
			this.reg_rcvAtSrcTime = reg_rcvAtSrcTime;

			this.call_sendToDestTime = call_sendToDestTime;
			this.call_rcvDestTime = call_rcvDestTime;
			this.call_sendToSrcTime = call_sendToSrcTime;
			this.call_rcvAtSrcTime = call_rcvAtSrcTime;
		}
		
		public String toString() {
			return String.format("%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d",kind,taskName, entryName, 
					calledTaskName, calledEntryName, client, time, reg_sendToRegTime, 
					reg_rcvRegTime, reg_sendToSrcTime, reg_rcvAtSrcTime, call_sendToDestTime,
					call_rcvDestTime, call_sendToSrcTime, call_rcvAtSrcTime);
		}
	}
}
