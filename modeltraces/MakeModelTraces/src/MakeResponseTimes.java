import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import org.sysma.schedulerExecutor.TaskDump;

public class MakeResponseTimes {

	public static void main(String infn, String outfn) throws IOException {
		String log = Files.readString(Path.of(infn));
		var tds = TaskDump.fromJsons(log);
		
		HashMap<String, ArrayList<Long>> serviceTime = new HashMap<>();
		HashMap<String, Long> stTimeIn = new HashMap<>();
		HashMap<String, ArrayList<Long>> responseTime = new HashMap<>();
		HashMap<String, Long> rtTimeIn = new HashMap<>();
		
		HashMap<String, Long> HACK_dbTimeIn = new HashMap<>();
		HashMap<String, String> HACK_dbTimeInEnt = new HashMap<>();
		
		for(var td:tds) {
			td.log.stream().sorted((l1,l2)->Long.compare(l1.time, l2.time)).forEachOrdered(l->{
				//System.out.println(l.toString());
				l.switch_((begin)->{
					String key = begin.client+"-"+begin.taskName+"-"+begin.entryName;
					stTimeIn.put(key, begin.time);
				}, (end)->{
					String key = end.client+"-"+end.taskName+"-"+end.entryName;
					if(stTimeIn.containsKey(key)) { 
						long timein = stTimeIn.remove(key);
						String entry = end.taskName+"-"+end.entryName;
						var st = end.time-timein;
						if(end.taskName.equals("Client"))
							System.out.println(st);
						serviceTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(end.time-timein);
					}
				}, (resume)->{
					String key = resume.client+"-"+resume.taskName+"-"+resume.entryName;
					if(HACK_dbTimeIn.containsKey(key)) {
						long timein = HACK_dbTimeIn.remove(key);
						responseTime.computeIfAbsent(HACK_dbTimeInEnt.remove(key), 
								(z)->new ArrayList<>()).add(resume.time-timein);
					}
				}, (call)->{
					String key = call.client+"-"+call.calledTaskName+"-"+call.calledEntryName;
					rtTimeIn.put(key, call.time);
					if(call.calledTaskName.contains("db")) {
						HACK_dbTimeIn.put(key, call.time);
						HACK_dbTimeInEnt.put(key, call.taskName+"-"+call.entryName);
					}
				}, (replied)->{
					String key = replied.client+"-"+replied.calledTaskName+"-"+replied.calledEntryName;
					long timein = rtTimeIn.remove(key);
					String entry = replied.calledTaskName+"-"+replied.calledEntryName;
					responseTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(replied.time-timein);
				}, (waitfor)->{
				}, 
				(qcall)->{
					String key = qcall.client+"-"+qcall.database+"-"+qcall.queryName;
					rtTimeIn.put(key, qcall.time);
				}, (qresume)->{
					String key = qresume.client+"-"+qresume.database+"-"+qresume.queryName;
					long timein = rtTimeIn.remove(key);
					String entry = qresume.database+"-"+qresume.queryName;
					responseTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(qresume.time-timein);
				}, (call_reg)->{
					String key = call_reg.client+"-registry-registry-Query#"+
							call_reg.calledTaskName+"-"+call_reg.calledEntryName;
					rtTimeIn.put(key, call_reg.time);
				}, (fwd)->{
					String key = fwd.client+"-registry-registry-Query#"+
							fwd.calledTaskName+"-"+fwd.calledEntryName;
					//try {
					long timein = rtTimeIn.remove(key);
					String entry = "registry-registry-Query";
					responseTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(fwd.time-timein);
					//} catch(NullPointerException ex) {System.err.println(key+" not found in fwd!");}
					
					String keyC = fwd.client+"-"+fwd.calledTaskName+"-"+fwd.calledEntryName;
					rtTimeIn.put(keyC, fwd.time);
				}, (waitfor)->{}, 
				(fwdcall_reg)->{
					String key = fwdcall_reg.client+"-registry-registry-Query#"+
							fwdcall_reg.calledTaskName+"-"+fwdcall_reg.calledEntryName;
					rtTimeIn.put(key, fwdcall_reg.time);
					
					//String keyEnd = fwdcall_reg.client+"-"+fwdcall_reg.taskName+"-"+fwdcall_reg.entryName;
					//long timein = stTimeIn.remove(keyEnd);
					//String entry = fwdcall_reg.taskName+"-"+fwdcall_reg.entryName;
					//serviceTime.computeIfAbsent(entry, (z)->new ArrayList<>())
					//	.add(fwdcall_reg.time-timein);
				}, (freg)->{
					String key = freg.client+"-"+freg.taskName+"-"+freg.entryName;
					if(stTimeIn.containsKey(key)) { 
						long timein = stTimeIn.remove(key);
						String entry = freg.taskName+"-"+freg.entryName;
						serviceTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(freg.time-timein);
					}
				});
			});
		}
		
		var pw = new PrintWriter((outfn));
		pw.println("Entry; Service Time; Response Time; Service Time 95%; Response Time 95%");
		for(var ent:serviceTime.keySet()) {
			double stmean = 0;
			double stmean2 = 0;
			int skip = 0;
			int cnt = 0;
			for(var x:serviceTime.get(ent)) {
				if(skip>0) {
					skip--;
					continue;
				}
				cnt++;
				stmean += 1.0*x;
				stmean2 += (1.0*x)*x;
			}
			int sn = cnt;//serviceTime.get(ent).size();
			stmean /= 1000.0 * sn;
			stmean2 /= 1000.0 * 1000 * sn;
			double stErr95 = Math.pow((stmean2 - stmean*stmean)/sn, 0.5)*1.96;
			
			pw.print(ent+"; "+stmean+"; ");
			if(responseTime.containsKey(ent)) {
				double rtmean = 0;
				double rtmean2 = 0;
				for(var x:responseTime.get(ent)) {
					rtmean += 1.0*x;
					rtmean2 += 1.0*x*x;
				}
				int rn = serviceTime.get(ent).size();
				rtmean /= 1000.0 * rn;
				rtmean2 /= 1000.0 * 1000 * rn;
				double rtErr95 = Math.pow((rtmean2 - rtmean*rtmean)/rn, 0.5)*1.96;
				pw.println(rtmean+"; "+stErr95+";"+rtErr95);
			} else {
				pw.println("; "+stErr95+";");
			}
		}
		for(var ent:responseTime.keySet()) {
			if(serviceTime.containsKey(ent))
				continue;
			double rtmean = 0;
			double rtmean2 = 0;
			for(var x:responseTime.get(ent)) {
				rtmean += 1.0*x;
				rtmean2 += 1.0*x*x;
			}
			int rn = responseTime.get(ent).size();
			rtmean /= 1000.0 * rn;
			rtmean2 /= 1000.0 * 1000 * rn;
			double rtErr95 = Math.pow((rtmean2 - rtmean*rtmean)/rn, 0.5)*1.96;
			pw.println(ent+"; ; "+rtmean+"; ; "+rtErr95);
		}
		pw.close();
	}
}
