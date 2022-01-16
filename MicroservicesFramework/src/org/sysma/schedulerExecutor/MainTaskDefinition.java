package org.sysma.schedulerExecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public abstract class MainTaskDefinition<T> {
	public abstract void main(Communication comm, T arg) throws InterruptedException;
	
	public static class Worker implements Runnable{
		public final Communication comm;
		public final Instant until;
		public final Entry entry;
		public final int cidx;
		
		
		public Worker(int cidx, Communication comm, Instant until, Entry entry) {
			super();
			this.comm = comm;
			this.until = until;
			this.entry = entry;
			this.cidx = cidx;
		}


		@Override
		public void run() {
			int clCount=0;
			while(Instant.now().isBefore(until)) {
				//System.out.println(".");
				comm.client = "client-"+cidx+"-"+(clCount++);
				entry.run(comm, "client-"+cidx);
				entry.log.get().add(new LogLine.End(entry.taskName, entry.entryName, comm.client, System.currentTimeMillis()));
			}
			comm.close();
		}
		
	}
	
	private Function<T, Entry> makeEnt = (t)-> new Entry("Client", "main") {
		@Override
		public void service(Communication comm) {
			try {
				main(comm, t);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	public ArrayList<String> startRegistry(Duration length, int nClients, Function<Integer, T> fArgs) throws IOException {
		return startRegistry(length, Duration.ZERO, nClients, fArgs);
	}
	
	public ArrayList<String> startRegistry(Duration length, Duration waitBeforeCollect, int nClients, Function<Integer, T> fArgs) throws IOException {
		Entry[] entries = new Entry[nClients];
		
		for(int i=0; i<nClients; i++) {
			entries[i] = makeEnt.apply(fArgs.apply(i));
		}
		
		Thread[] workers = new Thread[nClients];
		var until = Instant.now().plus(length);
		
		for(int i=0; i<nClients; i++) {
			workers[i] = new Thread(new Worker(i, new Communication(entries[i], null, null, "", System.currentTimeMillis()), until, entries[i]));
			workers[i].start();
		}
		
		for(int i=0; i<nClients; i++) {
			while(true) {
				try {
					System.out.println("Stopping client "+i);
					workers[i].join();
					break;
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			Thread.sleep(waitBeforeCollect.toMillis());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TaskDump tdump = new TaskDump(entries[0].taskName, Set.of(entries[0].entryName),
				nClients, Queries.getAll(), 
				Arrays.stream(entries).flatMap(l->l.getLog()).collect(Collectors.toCollection(ArrayList::new)));
		tdump.isClient = true;
		ArrayList<String> dumps = new ArrayList<>();
		dumps.add(tdump.toJson());
		
		String oth = TaskDirectory.getAllLogs();
		var gson = new Gson();
		var othArr = gson.fromJson(oth, JsonArray.class);
		for(int i=0; i<othArr.size(); i++)
			dumps.add(gson.toJson(othArr.get(i)));
		return dumps;
	}
	
	private ArrayList<String> start(Duration length, int nClients, Function<Integer, T> fArgs) throws IOException {
		Entry[] entries = new Entry[nClients];
		
		for(int i=0; i<nClients; i++) {
			entries[i] = makeEnt.apply(fArgs.apply(i));
		}
		
		Thread[] workers = new Thread[nClients];
		var until = Instant.now().plus(length);
		
		for(int i=0; i<nClients; i++) {
			workers[i] = new Thread(new Worker(i, new Communication(entries[i], null, null, "", System.currentTimeMillis()), until, entries[i]));
			workers[i].start();
		}
		
		for(int i=0; i<nClients; i++) {
			while(true) {
				try {
					System.out.println("Stopping client "+i);
					workers[i].join();
					break;
				} catch (InterruptedException e) {
				}
			}
		}
		
		TaskDump tdump = new TaskDump(entries[0].taskName, Set.of(entries[0].entryName),
				nClients, Queries.getAll(), 
				Arrays.stream(entries).flatMap(l->l.getLog()).collect(Collectors.toCollection(ArrayList::new)));
		tdump.isClient = true;
		ArrayList<String> dumps = new ArrayList<>();
		dumps.add(tdump.toJson());
		CloseableHttpClient hclient = HttpClients.createDefault();
		for(var loguri: TaskDefinition.allLogsUri) {
			HttpPost httppost = new HttpPost(loguri);
			var httpresponse = hclient.execute(httppost);
			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    int nRead;
		    byte[] data = new byte[1024];
		    while ((nRead = httpresponse.getEntity().getContent().read(data, 0, data.length)) != -1) {
		        buffer.write(data, 0, nRead);
		    }

		    buffer.flush();
		    byte[] byteArray = buffer.toByteArray();
		        
		    String text = new String(byteArray, StandardCharsets.UTF_8);
		    dumps.add(text);
		    httpresponse.close();
		}
		hclient.close();
		return dumps;
	}
}
