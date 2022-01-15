package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("write_query")
public class WriteQuery implements Instruction {
	@XStreamAsAttribute
	private String db;
	@XStreamAsAttribute
	private String sql;
	
	@XStreamAsAttribute
	private String name;
	
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	

	
	@Override
	public String getName() {
		if(name == null)
			return String.format("writeQuery(%s, %s)",db,sql);
		else
			return name;
	}
}
