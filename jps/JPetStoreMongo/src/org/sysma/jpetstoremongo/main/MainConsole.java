package org.sysma.jpetstoremongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.jpetstoremongo.services.AccountTask;
import org.sysma.jpetstoremongo.services.CartTask;
import org.sysma.jpetstoremongo.services.CatalogTask;
import org.sysma.jpetstoremongo.services.FrontendTask;
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

	public static void main(String[] args) throws Exception {
		if(args.length == 1 && args[0].equals("makedb")) {
			CreateDb.main();
			return;
		}
		if(args.length != 10) {
			System.out.println("Usage:\njava -jar jps.jar <log.json> <n_clients> "+
				"<tp_account> <rep_account> " + 
				"<tp_cart> <rep_cart>"+
				"<tp_catalog> <rep_catalog> " + 
				"<tp_frontend> <rep_frontend>\n" +
				"java -jar jps.jar makedb");
			return;
		}
		String outfn = args[0];
		int ncli = Integer.parseInt(args[1]);
		int tp_acc = Integer.parseInt(args[2]);
		int rep_acc = Integer.parseInt(args[3]);
		int tp_cart = Integer.parseInt(args[4]);
		int rep_cart = Integer.parseInt(args[5]);
		int tp_cat = Integer.parseInt(args[6]);
		int rep_cat = Integer.parseInt(args[7]);
		int tp_fe = Integer.parseInt(args[8]);
		int rep_fe = Integer.parseInt(args[9]);
		
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		startTask(AccountTask.class, tp_acc, rep_acc, tasks);
		startTask(CartTask.class, tp_cart, rep_cart, tasks);
		startTask(CatalogTask.class, tp_cat, rep_cat, tasks);
		startTask(FrontendTask.class, tp_fe, rep_fe, tasks);
		
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
