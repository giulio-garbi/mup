package org.sysma.tmstwomongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.schedulerExecutor.HttpTask;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.tmsmongo.services.CmsTask;
import org.sysma.tmsmongo.services.EmsTask;
import org.sysma.tmsmongo.services.QmsTask;
import org.sysma.tmsmongo.services.UmsTask;

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
		if(args.length != 10) {
			System.out.println("Usage:\njava -jar tmstwo.jar <log.json> <n_clients> "+
				"<tp_cms> <rep_cms> " + 
				"<tp_ems> <rep_ems>"+
				"<tp_qms> <rep_qms> " + 
				"<tp_ums> <rep_ums> ");
			return;
		}
		String outfn = args[0];
		int ncli = Integer.parseInt(args[1]);
		int tp_cms = Integer.parseInt(args[2]);
		int rep_cms = Integer.parseInt(args[3]);
		int tp_ems = Integer.parseInt(args[4]);
		int rep_ems = Integer.parseInt(args[5]);
		int tp_qms = Integer.parseInt(args[6]);
		int rep_qms = Integer.parseInt(args[7]);
		int tp_ums = Integer.parseInt(args[8]);
		int rep_ums = Integer.parseInt(args[9]);
		
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		startTask(CmsTask.class, tp_cms, rep_cms, tasks);
		startTask(EmsTask.class, tp_ems, rep_ems, tasks);
		startTask(QmsTask.class, tp_qms, rep_qms, tasks);
		startTask(UmsTask.class, tp_ums, rep_ums, tasks);
		
		ClientTask cli = new ClientTask();
		cli.startRegistry(Duration.ofSeconds(50),ncli, (i)->null);
		var tdumps = cli.startRegistry(Duration.ofSeconds(500),ncli, (i)->null);
		
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
