package org.sysma.lqnexecutor.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("entry")
public class Entry{
	
	@XStreamAsAttribute
	private String name;
	
	@XStreamImplicit
	private Instruction[] code = new Instruction[0];
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Instruction[] getCode() {
		if(code == null)
			return new Instruction[0];
		return code;
	}
	public void setCode(Instruction[] code) {
		this.code = code;
	}
	/*public Stream<String> getEventList(){
		return Arrays.stream(code).filter(i->(i instanceof Log)).map(i-> ((Log)i).getEvent());
	}
	
	public Stream<String> getResponseTimeNames(){
		return Stream.concat(Arrays.stream(code).filter(i->(i instanceof ResponseTimeBegin)).map(i-> ((ResponseTimeBegin)i).getLabel()),
				Arrays.stream(code).filter(i->(i instanceof ResponseTimeEnd)).map(i-> ((ResponseTimeEnd)i).getLabel()));
	}*/
}
