import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.sysma.schedulerExecutor.TaskDump;

public class MakeUtilization {

	public static void main(String infn, String outfn) throws IOException {
		
		HashMap<String, Double> outUData = new HashMap<>();
		String log = Files.readString(Path.of(infn));
		var tds = TaskDump.fromJsons(log);
		
		HashMap<String, Long> uTimeSum = new HashMap<>();
		long[] firstTime = {-1};
		long[] lastTime = {-1};
		var seen = new HashSet<String>();
		
		for(var td:tds) {
			td.log.stream().sorted((l1,l2)->Long.compare(l1.time, l2.time)).forEachOrdered(l->{
				
				if(firstTime[0] < 0)
					firstTime[0] = l.time;
				lastTime[0] = l.time;
				l.switch_(
				(begin)->{
					String key = begin.client+"-"+begin.taskName+"-"+begin.entryName;
					uTimeSum.computeIfAbsent(begin.taskName, (x)->0L);
					uTimeSum.compute(begin.taskName, (k,v)->v-begin.time);
					seen.remove(key);
				}, (end)->{
					String key = end.client+"-"+end.taskName+"-"+end.entryName;
					if(!seen.contains(key)) {
						uTimeSum.computeIfAbsent(end.taskName, (x)->0L);
						uTimeSum.compute(end.taskName, (k,v)->v+end.time);
						seen.add(key);
					}
				}, (resume)->{}, (call)->{
					
				}, (replied)->{
					
				}, (waitfor)->{}, 
				(qcall)->{
				}, (qresume)->{
				}, (call_reg)->{
					
				}, (fwd)->{
					
				}, (waitfor_reg)->{}, 
				(fwdcall_reg)->{
					String key = fwdcall_reg.client+"-"+fwdcall_reg.taskName+"-"+fwdcall_reg.entryName;
					if(!seen.contains(key)) {
						uTimeSum.computeIfAbsent(fwdcall_reg.taskName, (x)->0L);
						uTimeSum.compute(fwdcall_reg.taskName, (k,v)->v+fwdcall_reg.time);
						seen.add(key);
					}
				}, (forwarding)->{});
			});
		}
		
		for(var ent:uTimeSum.keySet()) {
			var U = uTimeSum.get(ent).doubleValue()/(lastTime[0]-firstTime[0]);
			outUData.put(ent, U);
		}
		
		PrintWriter pw = outfn.equals("-")? new PrintWriter(System.out) : new PrintWriter((outfn));
		pw.println("Task; Utilization");
		for(var ent:outUData.keySet().stream().sorted().collect(Collectors.toList())) {
			if(ent.equals("Client"))
				continue;
			pw.print(ent);
			pw.print("; "+outUData.get(ent));
			pw.println();
		}
		pw.close();
	}
}
