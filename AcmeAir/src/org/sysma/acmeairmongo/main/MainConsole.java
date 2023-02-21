package org.sysma.acmeairmongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.acmeairmongo.services.AuthTask;
import org.sysma.acmeairmongo.services.ClientTask;
import org.sysma.acmeairmongo.services.MainTask;
import org.sysma.schedulerExecutor.HttpTask;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;

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

	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length != 6) {
			System.out.println("Usage:\njava -jar aair.jar <log.json> <n_clients> "+
				"<tp_main> <rep_main> <tp_auth> <rep_auth>");
			return;
		}
		String outfn = args[0];
		int ncli = Integer.parseInt(args[1]);
		int tp_main = Integer.parseInt(args[2]);
		int rep_main = Integer.parseInt(args[3]);
		int tp_auth = Integer.parseInt(args[4]);
		int rep_auth = Integer.parseInt(args[5]);
		
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		startTask(AuthTask.class, tp_auth, rep_auth, tasks);
		startTask(MainTask.class, tp_main, rep_main, tasks);
		
		ClientTask cli = new ClientTask();
		cli.startRegistry(Duration.ofSeconds(100),ncli, (i)->new String[] {"uid"+(i+1)+"@email.com", "password"});
		var tdumps = cli.startRegistry(Duration.ofSeconds(1200),ncli, (i)->new String[] {"uid"+(i+1)+"@email.com", "password"});
		
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
