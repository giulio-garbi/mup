package org.sysma.teastoremongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.schedulerExecutor.HttpTask;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.teastoremongo.services.AuthTask;
import org.sysma.teastoremongo.services.ImageTask;
import org.sysma.teastoremongo.services.PersistenceTask;
import org.sysma.teastoremongo.services.RecommenderTask;
import org.sysma.teastoremongo.services.WebTask;

public class MainConsole {
	
	private static int portBase = 9100;
	
	public static void startTask(Class<? extends TaskDefinition> tdef, int tpool, int rep, ArrayList<HttpTask> lst) throws IOException {
		for(int i=0; i<rep; i++) {
			int port = portBase++;
			System.out.println("Instantiate "+tdef.getName()+" #"+(i+1)+"/"+rep);
			var tk = TaskDefinition.instantiate(tdef, port,tpool);
			lst.add(tk);
			System.out.println("Start "+tdef.getName()+" #"+(i+1)+"/"+rep);
			tk.start();
			System.out.println("Register "+tdef.getName()+" #"+(i+1)+"/"+rep);
			TaskDirectory.register(tdef, port);
		}
	}

	public static void main(String[] args) throws Exception {
		if(args.length == 1 && args[0].equals("makedb")) {
			CreateDb.main();
			return;
		}
		if(args.length != 12) {
			System.out.println("Usage:\njava -jar ts.jar <log.json> <n_clients> "+
				"<tp_auth> <rep_auth> " + 
				"<tp_image> <rep_image>"+
				"<tp_persistence> <rep_persistence> " + 
				"<tp_recommender> <rep_recommender> " + 
				"<tp_web> <rep_web>\n" +
				"java -jar jps.jar makedb");
			return;
		}
		String outfn = args[0];
		int ncli = Integer.parseInt(args[1]);
		int tp_auth = Integer.parseInt(args[2]);
		int rep_auth = Integer.parseInt(args[3]);
		int tp_image = Integer.parseInt(args[4]);
		int rep_image = Integer.parseInt(args[5]);
		int tp_persistence = Integer.parseInt(args[6]);
		int rep_persistence = Integer.parseInt(args[7]);
		int tp_recommender = Integer.parseInt(args[8]);
		int rep_recommender = Integer.parseInt(args[9]);
		int tp_web = Integer.parseInt(args[10]);
		int rep_web = Integer.parseInt(args[11]);
		
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		startTask(AuthTask.class, tp_auth, rep_auth, tasks);
		startTask(ImageTask.class, tp_image, rep_image, tasks);
		startTask(PersistenceTask.class, tp_persistence, rep_persistence, tasks);
		startTask(RecommenderTask.class, tp_recommender, rep_recommender, tasks);
		startTask(WebTask.class, tp_web, rep_web, tasks);
		
		ClientTask cli = new ClientTask();
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
