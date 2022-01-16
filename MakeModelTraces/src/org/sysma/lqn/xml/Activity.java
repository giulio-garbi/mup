package org.sysma.lqn.xml;

public class Activity {
	public final String name;
	public float host_demand_mean;
	public float host_demand_cvsq;
	public final int phase;
	public final boolean isFwdAct;
	public String boundToEntry;
	
	public float netSendTime=0, netRcvTime=0;
	
	public String whoCall;
	public float fwdAfterTime;
	
	public Activity(String name, float host_demand_mean, float host_demand_cvsq, int phase, String boundToEntry,
			boolean isFwdAct) {
		super();
		this.name = name;
		this.host_demand_mean = host_demand_mean;
		this.phase = phase;
		this.boundToEntry = boundToEntry;
		this.host_demand_cvsq = host_demand_cvsq;
		this.isFwdAct = isFwdAct;
	}
	
	public String toXml() {
		return "<"+(isFwdAct?"forward-activity fwdAfterTime=\""+this.fwdAfterTime+"\"":"activity")+" host-demand-mean=\""+host_demand_mean
				+"\" host-demand-cvsq=\""+host_demand_cvsq
				+"\" phase=\""+phase+"\""
				+(boundToEntry!=null?" bound-to-entry=\""+boundToEntry+"\"":"")
				+" name=\""+name+"\""
				+(whoCall!=null?"><synch-call dest=\""+whoCall+"\" calls-mean=\"1\" "
						+ "netSendTime=\""+netSendTime+"\" "
						+ "netRcvTime=\""+netRcvTime+"\"/></"+(isFwdAct?"forward-activity":"activity")+">":"/>");
				
				
				//<synch-call dest="disk1write" calls-mean="1"/>
	}
}
