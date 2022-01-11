package org.sysma.schedulerExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TaskDump {
	private static final Gson GSON;
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(LogLine.class, new LogLineDeserializer());
		GSON = gsonBuilder.create();
	}
	
	public String taskName;
	public Set<String> entries;
	public int mult;
	public HashMap<String, ArrayList<String>> queries;
	public ArrayList<LogLine> log;
	public boolean isClient = false;
	
	public TaskDump(String taskName, Set<String> entries, int mult, HashMap<String, ArrayList<String>> queries, ArrayList<LogLine> log) {
		this.taskName = taskName;
		this.entries = entries;
		this.log = log;
		this.mult = mult;
		this.queries = queries;
	}
	
	public String toJson() {
		return GSON.toJson(this);
	}
	
	public static TaskDump[] fromJsons(String json) {
		return GSON.fromJson(json, TaskDump[].class);
	}

	public static TaskDump fromJson(String json) {
		return GSON.fromJson(json, TaskDump.class);
	}
}
