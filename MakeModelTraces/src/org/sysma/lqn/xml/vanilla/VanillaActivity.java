package org.sysma.lqn.xml.vanilla;

import java.util.ArrayList;
import java.util.List;

import org.sysma.lqn.makeModel.NetTask;
import org.sysma.lqn.xml.Activity;

public class VanillaActivity {
	public final String name;
	public float host_demand_mean;
	public float host_demand_cvsq;
	public final int phase;
	public String boundToEntry;
	public String whoCall;
	
	public VanillaActivity(String name, float host_demand_mean, float host_demand_cvsq, int phase, 
			String boundToEntry, String whoCall) {
		super();
		this.name = name;
		this.host_demand_mean = host_demand_mean;
		this.phase = phase;
		this.boundToEntry = boundToEntry;
		this.host_demand_cvsq = host_demand_cvsq;
		this.whoCall = whoCall;
	}
	
	public String toXml() {
		return "<activity host-demand-mean=\""+host_demand_mean
				+"\" host-demand-cvsq=\""+host_demand_cvsq
				+"\" phase=\""+phase+"\""
				+(boundToEntry!=null?" bound-to-entry=\""+boundToEntry+"\"":"")
				+" name=\""+name+"\""
				+(whoCall!=null?"><synch-call dest=\""+whoCall+"\" calls-mean=\"1\" /></activity>":"/>");
				
				
				//<synch-call dest="disk1write" calls-mean="1"/>
	}
	
	public static List<VanillaActivity> from(Activity act, NetTask net) {
		ArrayList<VanillaActivity> acts = new ArrayList<>();
		if(act.isFwdAct)
			throw new IllegalArgumentException("forward not supported");
		if(act.netSendTime != 0.0) {
			String sndEntry = net.newNetEntry(act.netSendTime);
			acts.add(new VanillaActivity(act.name+"-snd", 0, 0, 0, act.boundToEntry, sndEntry));
		}
		acts.add(new VanillaActivity(act.name, act.host_demand_mean, act.host_demand_cvsq, 0, 
				acts.size()==0?act.boundToEntry:null, act.whoCall));
		if(act.netRcvTime != 0.0) {
			String rcvEntry = net.newNetEntry(act.netSendTime);
			acts.add(new VanillaActivity(act.name+"-rcv", 0, 0, 0, null, rcvEntry));
		}
		return acts;
	}
}
