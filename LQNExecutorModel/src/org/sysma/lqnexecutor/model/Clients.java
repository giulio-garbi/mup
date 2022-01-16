package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("clients")
public class Clients {
	@XStreamAsAttribute
	private int population;
	
	@XStreamImplicit
	private Instruction[] code = new Instruction[0];
	
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
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	public Instruction[] getCode() {
		if(code == null)
			return new Instruction[0];
		return code;
	}
	public void setCode(Instruction[] code) {
		this.code = code;
	}
	/*public Stream<String> getEventList() {
		return Arrays.stream(code).filter(i->(i instanceof Log)).map(i-> ((Log)i).getEvent());
	}
	public Stream<String> getResponseTimeNames(){
		return Stream.concat(Arrays.stream(code).filter(i->(i instanceof ResponseTimeBegin)).map(i-> ((ResponseTimeBegin)i).getName()),
				Arrays.stream(code).filter(i->(i instanceof ResponseTimeEnd)).map(i-> ((ResponseTimeEnd)i).getName()));
	}*/
	
}
