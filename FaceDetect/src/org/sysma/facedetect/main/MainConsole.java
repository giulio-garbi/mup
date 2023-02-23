package org.sysma.facedetect.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;

import org.opencv.objdetect.CascadeClassifier;
import org.sysma.facedetect.services.Backend;
import org.sysma.facedetect.services.ClientTask;
import org.sysma.facedetect.services.Frontend;
import org.sysma.facedetect.services.Storage;
import org.sysma.schedulerExecutor.HttpTask;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;

import nu.pattern.OpenCV;

public class MainConsole {
	
	private static int portBase = 9100;
	
	public static void startTask(Class<? extends TaskDefinition> tdef, String name, int tpool, int rep, ArrayList<HttpTask> lst) throws IOException {
		TaskDef td = tdef.getAnnotation(TaskDef.class);
		String taskName = td.name();
		String nm;
		if(name != null) {
			nm = name;
		} else {
			nm = taskName;
		}
		for(int i=0; i<rep; i++) {
			int port = portBase++;
			System.out.println("Instantiate "+nm+" ("+nm+") #"+(i+1)+"/"+rep);
			var tk = TaskDefinition.instantiate(tdef, nm, port,tpool);
			lst.add(tk);
			System.out.println("Start "+nm+" ("+nm+") #"+(i+1)+"/"+rep);
			tk.start();
			System.out.println("Register "+nm+" ("+nm+") #"+(i+1)+"/"+rep);
			TaskDirectory.register(tdef, nm, port);
		}
	}
	
	public static void startTask(Class<? extends TaskDefinition> tdef, int tpool, int rep, ArrayList<HttpTask> lst) throws IOException {
		startTask(tdef, null, tpool, rep, lst);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length != 12) {
			System.out.println("Usage:\njava -jar fd.jar <log.json> <n_clients> "+
				"<tp_fe> <rep_fe> <tp_be0> <rep_be0> <tp_be1> <rep_be1> <tp_st> <rep_st> <p0> <runtime>");
			return;
		}
		OpenCV.loadLocally();
		Storage.load();
		
		String outfn = args[0];
		int ncli = Integer.parseInt(args[1]);
		int tp_fe = Integer.parseInt(args[2]);
		int rep_fe = Integer.parseInt(args[3]);
		int tp_be0 = Integer.parseInt(args[4]);
		int rep_be0 = Integer.parseInt(args[5]);
		int tp_be1 = Integer.parseInt(args[6]);
		int rep_be1 = Integer.parseInt(args[7]);
		int tp_st = Integer.parseInt(args[8]);
		int rep_st = Integer.parseInt(args[9]);
		float p0 = Float.parseFloat(args[10]);
		int rtsec = Integer.parseInt(args[11]);
		
		System.out.println("I&R Registry ");
		var registry = TaskDirectory.instantiateRegistry(9099);
		registry.start();
		
		var tasks = new ArrayList<HttpTask>();
		
		startTask(Frontend.class, tp_fe, rep_fe, tasks);
		startTask(Storage.class, tp_st, rep_st, tasks);
		startTask(Backend.class, "backend0", tp_be0, rep_be0, tasks);
		startTask(Backend.class, "backend1", tp_be1, rep_be1, tasks);
		
		ClientTask cli = new ClientTask();
		cli.startRegistry(Duration.ofSeconds(100),ncli, (i)->new String[] {String.valueOf(p0), String.valueOf(1-p0)});
		var tdumps = cli.startRegistry(Duration.ofSeconds(rtsec),ncli, (i)->new String[] {String.valueOf(p0), String.valueOf(1-p0)});
		
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
		System.exit(0);
	}
}