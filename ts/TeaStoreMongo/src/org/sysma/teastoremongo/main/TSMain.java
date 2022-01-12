package org.sysma.teastoremongo.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;

import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.teastoremongo.services.AuthTask;
import org.sysma.teastoremongo.services.ImageTask;
import org.sysma.teastoremongo.services.PersistenceTask;
import org.sysma.teastoremongo.services.RecommenderTask;
import org.sysma.teastoremongo.services.WebTask;

public class TSMain {

	public static void main(String[] args) throws IOException, InterruptedException {
		var slowdown = 0;
		var warmup = Duration.ofSeconds(50);
		var runtime = Duration.ofSeconds(500);
		//args = new String[]{"1"};
		for(var arg:args) {
			System.out.println("---------------------- C-W-I "+arg);
			System.out.println("I&R ");
			
			var parts = arg.split("-");
			var ncli = Integer.parseInt(parts[0]);
			var nweb = Integer.parseInt(parts[1]);
			var nimg = Integer.parseInt(parts[2]);
			//var ncat = Integer.parseInt(parts[2]);

			var register = TaskDirectory.instantiateRegistry(9099);
			register.start();
			
			var atk = TaskDefinition.instantiate(AuthTask.class, 9081, slowdown, 1);
			atk.start();
			TaskDirectory.register(AuthTask.class, 9081);
			
			var itk = TaskDefinition.instantiate(ImageTask.class, 9082, slowdown, nimg);
			itk.start();
			TaskDirectory.register(ImageTask.class, 9082);
			
			var ptk = TaskDefinition.instantiate(PersistenceTask.class, 9083, slowdown, 1);
			ptk.start();
			TaskDirectory.register(PersistenceTask.class, 9083);
			
			var rtk = TaskDefinition.instantiate(RecommenderTask.class, 9084, slowdown, 1);
			rtk.start();
			TaskDirectory.register(RecommenderTask.class, 9084);
			
			var wtk = TaskDefinition.instantiate(WebTask.class, 9080, slowdown, nweb);
			wtk.start();
			TaskDirectory.register(WebTask.class, 9080);
			
			ClientTask cli = new ClientTask();
			cli.startRegistry(warmup,Duration.ofSeconds(50),128, (i)->"user"+i);
			//cli = new ClientTask();
			var tdumps = cli.startRegistry(runtime,Duration.ofSeconds(50),ncli, (i)->"user"+i);
			
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

			atk.stop();
			itk.stop();
			ptk.stop();
			rtk.stop();
			wtk.stop();
			register.stop();
		}
	}

}
