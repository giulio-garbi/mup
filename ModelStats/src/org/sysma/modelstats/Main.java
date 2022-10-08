package org.sysma.modelstats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class Main {

	public static void main(String[] args) throws IOException {
		//String xmlFname = args[0];
		String xmlFname = "/Users/giulio/Downloads/icdcs2022-replication-data/tms/model.lqnx";
		
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
