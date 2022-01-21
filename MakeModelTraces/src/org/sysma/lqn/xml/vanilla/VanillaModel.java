package org.sysma.lqn.xml.vanilla;

import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.makeModel.NetTask;
import org.sysma.lqn.xml.Model;

public class VanillaModel {
	public final List<VanillaProcessor> procs;
	public final String name;
	
	public VanillaModel(String name, List<VanillaProcessor> procs) {
		this.procs = procs;
		this.name = name;
	}
	
	public String toXml() {
		return "<?xml version=\"1.0\"?>\n<lqn-model name=\""+name+"\">"
				+ procs.stream().map(VanillaProcessor::toXml).collect(Collectors.joining("\n","\n","\n"))
				+ "</lqn-model>";
	}
	
	public static VanillaModel from(Model mdl) {
		NetTask net = new NetTask("Net", "net-", "Z");
		List<VanillaProcessor> procs = mdl.procs.stream()
				.map(x->VanillaProcessor.from(x, net)).collect(Collectors.toList());
		procs.add(net.getProcessor());
		return new VanillaModel(mdl.name, procs);
	}
}