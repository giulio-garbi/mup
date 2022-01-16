package org.sysma.lqnxsim.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

import org.sysma.lqnxsim.exec.LqnModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class MainFromXml {

	public static void main(String[] args) throws IOException {	
		String[] parts = ("256-8-1-2-1").split(" ");
		for(var idx:parts) {
			var timeIn = Instant.now();
		//if(args.length != 2) {
			//System.out.println("Usage:\njava -jar lqnexec.jar <lqn.xml> <outfile.json>");
			//return;
			args = new String[] {"/Users/giulio/SynologyDrive/tms_mongo/h/full-h-"+idx+".lqnx" , 
					"/Users/giulio/SynologyDrive/tms_mongo/h/full-h-"+idx+".sim.csv",
					"/Users/giulio/SynologyDrive/tms_mongo/h/full-h-"+idx+".sim.util.csv"};
		//}
		String xmlFname = args[0];
		String outFname = args[1];
		String utilFname = args[2];
		
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
		var mdl = new LqnModel(lqnModel);
		mdl.startAllClients();
		Duration time = Duration.ZERO;
		//System.out.println("Start");
		long lastSec = 0;
		Duration stopAt = Duration.ofSeconds(6000);
		while(time.compareTo(stopAt)<0) {
			if(lastSec < time.toSeconds()) {
				lastSec = time.toSeconds();
				System.out.println(lastSec+" s");
			}
			mdl.advance();
			time = Duration.between(Instant.EPOCH, mdl.getClock());
			
		}
		System.out.println("Stop");
		//var rtimes = mdl.getClientResponseTimes();
		//System.out.println(rtimes.size());
		//System.out.println(rtimes.stream()
		//		.collect(Collectors.averagingDouble(d->d.toNanos()/1000_000_000.0)));
		if(outFname != null)
			mdl.debugRTs(outFname);
		if(utilFname != null)
			mdl.debugUtils(utilFname, Instant.EPOCH.plus(stopAt));
		var timeOut = Instant.now();
		
		System.out.println(idx+" : "+Duration.between(timeIn, timeOut));
		
		/*LQN instance = new LQN(lqnModel);
		instance.startTasks();

		System.out.println("Start");
		String json = "["+ 
				instance.startSim(lqnModel.getParameters().getLength()).stream().collect(Collectors.joining(","))
				+ "]";
		instance.stopTasks();
		Files.writeString(Path.of(outFname), json);*/
		}
	}

}
