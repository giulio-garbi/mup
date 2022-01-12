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

public class TSMainHoriz {
	
	private static int portBase = 9100;
	
	public static void horiz(Class<? extends TaskDefinition> tdef, int rep, ArrayList<HttpTask> lst) throws IOException {
		for(int i=0; i<rep; i++) {
			int port = portBase++;
			System.out.println("Instantiate "+tdef.getName()+" #"+(i+1)+"/"+rep);
			var tk = TaskDefinition.instantiate(tdef, port,1);
			lst.add(tk);
			System.out.println("Start "+tdef.getName()+" #"+(i+1)+"/"+rep);
			tk.start();
			System.out.println("Register "+tdef.getName()+" #"+(i+1)+"/"+rep);
			TaskDirectory.register(tdef, port);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		var warmup = Duration.ofSeconds(50);
		var runtime = Duration.ofSeconds(500);
		//args = new String[]{"1"};
		for(var arg:args) {
			System.out.println("---------------------- C-W-I "+arg);
			
			var parts = arg.split("-");
			var ncli = Integer.parseInt(parts[0]);
			var nweb = Integer.parseInt(parts[1]);
			var nimg = Integer.parseInt(parts[2]);
			//var ncat = Integer.parseInt(parts[2]);

			var register = TaskDirectory.instantiateRegistry(9099);
			register.start();
			
			var tasks = new ArrayList<HttpTask>();
			
			horiz(AuthTask.class, 1, tasks);
			horiz(ImageTask.class, nimg, tasks);
			horiz(PersistenceTask.class, 1, tasks);
			horiz(RecommenderTask.class, 1, tasks);
			horiz(WebTask.class, nweb, tasks);
			
			ClientTask cli = new ClientTask();
			cli.startRegistry(warmup,Duration.ofSeconds(50),256, (i)->"user"+i);
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
			tasks.forEach(HttpTask::stop);
			register.stop();
		}
	}

}
