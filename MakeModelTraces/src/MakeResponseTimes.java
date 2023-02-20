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
		
		for(var td:tds) {
			td.log.stream().sorted((l1,l2)->Long.compare(l1.time, l2.time)).forEachOrdered(l->{
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
				}, (call)->{
					String key = call.client+"-"+call.calledTaskName+"-"+call.calledEntryName;
					rtTimeIn.put(key, call.time);
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

					long timein = rtTimeIn.remove(key);
					String entry = "registry-registry-Query";
					responseTime.computeIfAbsent(entry, (z)->new ArrayList<>()).add(fwd.time-timein);

					String keyC = fwd.client+"-"+fwd.calledTaskName+"-"+fwd.calledEntryName;
					rtTimeIn.put(keyC, fwd.time);
				}, (waitfor)->{}, 
				(fwdcall_reg)->{
					String key = fwdcall_reg.client+"-registry-registry-Query#"+
							fwdcall_reg.calledTaskName+"-"+fwdcall_reg.calledEntryName;
					rtTimeIn.put(key, fwdcall_reg.time);
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
		
		PrintWriter pw = outfn.equals("-")? new PrintWriter(System.out) : new PrintWriter((outfn));
		
		double stmean = 0;
		int cnt = 0;
		for(var x:serviceTime.get("Client-main")) {
			cnt++;
			stmean += 1.0*x;
		}
		int sn = cnt;
		stmean /= 1000.0 * sn;
		pw.println(stmean);
		pw.close();
	}
}
