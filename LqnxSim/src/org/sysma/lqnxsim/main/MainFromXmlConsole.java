package org.sysma.lqnxsim.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import org.sysma.lqnxsim.exec.LqnModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class MainFromXmlConsole {

	public static void main(String[] args) throws IOException {	
		var timeIn = Instant.now();
		boolean dostats = false;
		if(args.length == 2 && args[1].equals("stats"))
			dostats = true;
		if(!dostats && args.length != 4) {
			System.out.println("Usage:\njava -jar lqnexec.jar <model.lqnx> <simtime_s> <rt.csv> <util.csv>");
			System.out.println("java -jar lqnexec.jar <model.lqnx> stats");
			return;
		}
		String xmlFname = args[0];
		String xml = Files.readString(Paths.get(xmlFname));
		

		XStream xstream = new XStream(new StaxDriver());
		xstream.processAnnotations(org.sysma.lqnxsim.model.Activity.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.ActivityRef.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Entry.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.LqnModel.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Post.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.PostAnd.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.PostOr.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Pre.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.PreAnd.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.PreOr.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Precedence.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Processor.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.ReplyActivity.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.ReplyEntry.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.SyncCall.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.ForwardingActivity.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.Task.class);
		xstream.processAnnotations(org.sysma.lqnxsim.model.TaskActivities.class);
		
		org.sysma.lqnxsim.model.LqnModel lqnModel = (org.sysma.lqnxsim.model.LqnModel) xstream.fromXML(xml);
		if(dostats) {
			mstats(lqnModel);
			return;
		}
		Duration stopAt = Duration.ofSeconds(Integer.parseInt(args[1]));
		String outFname = args[2];
		String utilFname = args[3];
		var mdl = new LqnModel(lqnModel);
		mdl.startAllClients();
		Duration time = Duration.ZERO;
		System.out.println("Start");
		long lastSec = 0;
		while(time.compareTo(stopAt)<0) {
			if(lastSec < time.toSeconds()) {
				lastSec = time.toSeconds();
				//System.out.println(lastSec+" s");
			}
			mdl.advance();
			time = Duration.between(Instant.EPOCH, mdl.getClock());
			
		}
		System.out.println("Stop");
		if(outFname != null)
			mdl.debugRTs(outFname);
		if(utilFname != null)
			mdl.debugUtils(utilFname, Instant.EPOCH.plus(stopAt));
		var timeOut = Instant.now();
		
		System.out.println(xmlFname+" simulated in "+Duration.between(timeIn, timeOut));
	}
	
	private static void mstats(org.sysma.lqnxsim.model.LqnModel lqnModel) {
		ModelStats ms = new ModelStats(lqnModel);
		System.out.println("Tasks: "+ms.tasks);
		System.out.println("Entries: "+ms.entries);
		System.out.println("Activities: "+ms.activities);
		System.out.println("Nodes: "+ms.nodes);
		System.out.println("   of which OR nodes: "+ms.or_nodes);
		System.out.println("Arcs: "+ms.arcs);
		System.out.println("Paths: "+ms.paths);
	}
}
