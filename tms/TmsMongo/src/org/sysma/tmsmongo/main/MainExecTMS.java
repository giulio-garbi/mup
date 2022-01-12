package org.sysma.tmsmongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.tmsmongo.services.CmsTask;
import org.sysma.tmsmongo.services.EmsTask;
import org.sysma.tmsmongo.services.QmsTask;
import org.sysma.tmsmongo.services.UmsTask;

public class MainExecTMS {

	public static void main(String[] args) throws IOException, InterruptedException {
		//args=new String[] {"t1-1-1-1-1"};
		for(String arg:args) {
			boolean testOnly = false;
			if(arg.charAt(0)=='t') {
				arg = arg.substring(1);
				testOnly = true;
			}
			var parts = arg.split("-");
			int ncli = Integer.parseInt(parts[0]);
			int nCms = Integer.parseInt(parts[1]);
			int nEms = Integer.parseInt(parts[2]);
			int nQms = Integer.parseInt(parts[3]);
			int nUms = Integer.parseInt(parts[4]);
			
			
			
			var registry = TaskDirectory.instantiateRegistry(9099);
			registry.start();
			
			System.out.println("---------------------- Cli-Cms-Ems-Qms-Ums"+arg);
			System.out.println("I&R Cms ");
			var ctk = TaskDefinition.instantiate(CmsTask.class, 9081,nCms);
			System.out.println("Start Cms ");
			ctk.start();
			
			System.out.println("I&R Ems ");
			var etk = TaskDefinition.instantiate(EmsTask.class, 9082,nEms);
			System.out.println("Start Ems ");
			etk.start();
			
			System.out.println("I&R Qms ");
			var qtk = TaskDefinition.instantiate(QmsTask.class, 9083, nQms);
			System.out.println("Start Qms ");
			qtk.start();
			
			System.out.println("I&R Ums ");
			var utk = TaskDefinition.instantiate(UmsTask.class, 9084,nUms);
			System.out.println("Start Ums ");
			utk.start();
			
			TaskDirectory.register(CmsTask.class, 9081);
			TaskDirectory.register(EmsTask.class, 9082);
			TaskDirectory.register(QmsTask.class, 9083);
			TaskDirectory.register(UmsTask.class, 9084);
			
			
			ClientTask cli = new ClientTask();
			ArrayList<String> tdumps;
			if(testOnly) {
				tdumps = cli.startRegistry(Duration.ofSeconds(300),ncli, (i)->null);
			}
			else {
				cli.startRegistry(Duration.ofSeconds(50),512, (i)->null);
				tdumps = cli.startRegistry(Duration.ofSeconds(500),ncli, (i)->null);
			}
			
			String outfn = "example"+arg+".json";
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
			ctk.stop();
			etk.stop();
			qtk.stop();
			utk.stop();
			registry.stop();
		}
	}

}
