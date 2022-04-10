package org.sysma.lqnexecutor.model;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("lqn")
public class LQN {
	
	@XStreamImplicit
	private Task[] tasks = new Task[0];

	@XStreamImplicit
	private Database[] databases = new Database[0];

	public Database[] getDatabases() {
		return databases;
	}

	private Clients clients;
	
	private Parameters params;

	public Task[] getTasks() {
		if(tasks == null)
			return new Task[0];
		return tasks;
	}

	public void setTasks(Task[] tasks) {
		this.tasks = tasks;
	}

	public Clients getClients() {
		return clients;
	}

	public void setClients(Clients clients) {
		this.clients = clients;
	}

	public Parameters getParameters() {
		return params;
	}

	public void setParameters(Parameters parameters) {
		this.params = parameters;
	}

	public void setDatabases(Database[] databases) {
		this.databases = databases;
	}
	
	/*public ArrayList<String> getEventList(){
		var s = Stream.concat(
					Arrays.stream(getTasks()).flatMap(t->t.getEventList()),
					clients.getEventList()
				).sorted().distinct();
		return s.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public ArrayList<String> getResponseTimeNames(){
		var s = Stream.concat(
					Arrays.stream(getTasks()).flatMap(t->t.getResponseTimeNames()),
					clients.getResponseTimeNames()
				).sorted().distinct();
		return s.collect(Collectors.toCollection(ArrayList::new));
	}*/
}
