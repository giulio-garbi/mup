package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("task")
public class Task {
	
	@XStreamAsAttribute
	private String name;
	
	@XStreamAsAttribute
	private int port;
	
	@XStreamAsAttribute
	private int threadpoolSize;
	
	@XStreamImplicit
	private Entry[] entries = new Entry[0];
	

	@XStreamImplicit
	private Database[] databases = new Database[0];
	
	public Database[] getDatabases() {
		if(databases == null)
			return new Database[0];
		return databases;
	}

	public void setDatabases(Database[] databases) {
		this.databases = databases;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getThreadpoolSize() {
		return threadpoolSize;
	}
	public void setThreadpoolSize(int threadpoolSize) {
		this.threadpoolSize = threadpoolSize;
	}
	public Entry[] getEntries() {
		if(entries == null)
			return new Entry[0];
		return entries;
	}
	public void setEntries(Entry[] entries) {
		this.entries = entries;
	}
	
	/*public Stream<String> getEventList(String prev, String entryName){
		Entry[] ents = getEntries();
		for(Entry ent:ents) {
			if(ent.getName().equals(entryName)) {
				return ent.getEventList(prev+"->"+name+"."+entryName);
			}
		}
	}*/
	
	/*public Stream<String> getEventList(){
		return Arrays.stream(entries).flatMap(e->e.getEventList());
	}*/

	/*public Stream<String> getResponseTimeNames(){
		return Arrays.stream(entries).flatMap(e->e.getResponseTimeNames());
	}*/
}
