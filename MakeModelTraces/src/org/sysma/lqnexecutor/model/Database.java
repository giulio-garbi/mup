package org.sysma.lqnexecutor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
/*
@XStreamAlias("database")
public class Database {
	
	@XStreamAsAttribute
	private String source;
	@XStreamAsAttribute
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
*/
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("database")
public class Database {
	
	@XStreamAsAttribute
	private String name;
	
	@XStreamImplicit
	private Query[] queries = new Query[0];

	public Query[] getQueries() {
		return queries;
	}

	public void setQueries(Query[] queries) {
		this.queries = queries;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}