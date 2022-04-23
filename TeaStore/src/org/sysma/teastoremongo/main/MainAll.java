package org.sysma.teastoremongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.schedulerExecutor.HttpTask;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.teastoremongo.services.AllTask;

public class MainAll {

	public static void main(String outfn, int ncli, int tp) throws IOException {
		int rep = 1;
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		MainConsole.startTask(AllTask.class, tp, rep, tasks);
		
		ClientLoginTask cli = new ClientLoginTask();
		cli.startRegistry(Duration.ofSeconds(50),ncli, (i)->"user"+i);
		var tdumps = cli.startRegistry(Duration.ofSeconds(500),ncli, (i)->"user"+i);
		
		var pw = new PrintWriter(outfn);
		
		pw.print("[");
		for(int i=0; i<tdumps.size(); i++) {
			pw.print(tdumps.get(i));
			if(i<tdumps.size()-1) {
				pw.println(",");
			} else {
				pw.println("]");
			}
		}
		pw.close();
		tasks.forEach(HttpTask::stop);
		registry.stop();
	}

}
