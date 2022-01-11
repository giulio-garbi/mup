package org.sysma.schedulerExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.core5.http.HttpHeaders;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpTask {
	public final int port;
	protected Map<String, Entry> entries;
	protected Map<String, BiConsumer<HttpExchange, HttpTask>> contexts = Map.of();
	protected final Scheduler scheduler;
	private HttpServer server;
	private final int nServers;
	private final FileServer fsEnt;
	protected final String name;
	
	public HttpTask(int port, String name, String fileServerPath, Scheduler scheduler, int slowdownLoop) {
		this.scheduler = scheduler;
		this.name = name;
		this.port = port;
		this.nServers = scheduler.getMaxWorkers();
		if(fileServerPath == null) {
			this.fsEnt = null;
		} else {
			this.fsEnt = new FileServer(name, fileServerPath, slowdownLoop);
		}
	}
	
	public void setEntries(Map<String, Entry> entries) {
		this.entries = entries;
	}
	
	void setContexts(Map<String, BiConsumer<HttpExchange, HttpTask>> contexts) {
		this.contexts = contexts;
	}
	
	private Set<String> getEntriesNames() {
		var entStream = entries.values().stream().map(x->x.entryName);
		var fileStream = Stream.ofNullable(fsEnt).flatMap(f->f.getAllFilesEntries());
		return Stream.concat(entStream, fileStream).collect(Collectors.toSet());
	}
	
	String getLogAndClear(){
		var log = Stream.concat(Stream.ofNullable(this.fsEnt), this.entries.values().stream())
				.flatMap((ent)->ent.getLog())
				.collect(Collectors.toCollection(ArrayList::new));
		Stream.concat(Stream.ofNullable(this.fsEnt), this.entries.values().stream()).forEach((e)->{e.clearLog();});
		try {
		return new TaskDump(name, getEntriesNames(), nServers, Queries.getAll(), log).toJson();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public void start() {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			for(var entry: this.entries.entrySet()) {
				String entryPath = entry.getKey();
				server.createContext(entryPath, makeHandler(entry.getValue()));
			}
			if(this.fsEnt != null) {
				server.createContext("/", makeHandler(this.fsEnt));
			}
			for(var kv:contexts.entrySet()) {
				server.createContext(kv.getKey(), (HttpExchange request)->{kv.getValue().accept(request, this);});
			}
			if(!contexts.containsKey("/log"))
				server.createContext("/log", (HttpExchange request)->{
					String ansS = getLogAndClear();
					byte[] ans = ansS.getBytes();
					request.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
					request.sendResponseHeaders(200, ans.length);
					request.getResponseBody().write(ans);
					request.getResponseBody().close();
				});
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		scheduler.stop();
		server.stop(Integer.MAX_VALUE);
	}

	private HttpHandler makeHandler(Entry job) {
		return (HttpExchange request)->{
			//System.out.println("R "+request.getRequestURI());
			scheduler.addJob(job, request, System.currentTimeMillis());
		};
	}

	public int getMultiplicity() {
		return nServers;
	}
	
	public InetSocketAddress getLocalhostAddress() {
		return new InetSocketAddress(port);
	}
}
