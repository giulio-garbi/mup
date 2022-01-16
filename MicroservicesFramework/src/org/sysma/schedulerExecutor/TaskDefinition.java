package org.sysma.schedulerExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;

public abstract class TaskDefinition {
	private class Ent extends Entry{
		private final Consumer<Communication> srv;

		public Ent(String taskName, String entryName, Consumer<Communication> srv) {
			super(taskName, entryName);
			this.srv = srv;
		}

		@Override
		public void service(Communication comm) {
			srv.accept(comm);
		}
		
	}
	private static final HashMap<String, Map<String, URI>> tasksDir = new HashMap<>();
	private static final HashMap<String, URI> tasksDirBase = new HashMap<>();
	public static final ArrayList<URI> allLogsUri = new ArrayList<>();
	
	public static void registerExternal(Class<? extends TaskDefinition> tdef, URI baseUri) {
		TaskDef td = tdef.getAnnotation(TaskDef.class);
		String taskName = td.name();
		HashMap<String, String> entriesPath = new HashMap<>();
		for(Method m: tdef.getMethods()) {
			EntryDef annEntry = m.getAnnotation(EntryDef.class);
			if(annEntry != null) {
				String entryName = m.getName();
				entriesPath.put(entryName, annEntry.value());
			}
		}
		allLogsUri.add(baseUri.resolve("/log"));
		registerExternal(taskName, entriesPath, baseUri);
	}
	
	public static void registerExternal(String taskName, Map<String, String> entriesPath, URI baseUri) {
		HashMap<String, URI> entries = new HashMap<>();
		for(var e:entriesPath.entrySet())
			entries.put(e.getKey(), baseUri.resolve(e.getValue()));
		tasksDirBase.put(taskName, baseUri);
		tasksDir.put(taskName, entries);
	}
	
	public static HttpTask instantiateAndRegister(Class<? extends TaskDefinition> tdef, int port, int mult) {
		return instantiateAndRegister(tdef, port, 0, mult);
	}
	public static HttpTask instantiateAndRegister(Class<? extends TaskDefinition> tdef, int port, int slowdownLoop, int mult) {
		URI baseUri = URI.create("http://localhost:"+port);
		registerExternal(tdef, baseUri);
		try {
			return tdef.getConstructor().newInstance().instantiate(port, slowdownLoop, mult);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static HttpTask instantiate(Class<? extends TaskDefinition> tdef, int port, int mult) {
		return instantiate(tdef, port, 0, mult);
	}
	public static HttpTask instantiate(Class<? extends TaskDefinition> tdef, int port, int slowdownLoop, int mult) {
		try {
			return tdef.getConstructor().newInstance().instantiate(port, slowdownLoop, mult);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected HttpTask instantiate(int port, int slowdownLoop, int mult) {
		Class<? extends TaskDefinition> tdef = this.getClass();
		TaskDef td = tdef.getAnnotation(TaskDef.class);
		String taskName = td.name();
		FIFO fifo = new FIFO(mult);
		String fsPath = null;
		if(td.filePath().length() > 0)
			fsPath = td.filePath();
		HttpTask task = new HttpTask(port, taskName, fsPath, fifo, slowdownLoop);
		
		HashMap<String, Entry> entries = new HashMap<>();
		HashMap<String, BiConsumer<HttpExchange, HttpTask>> contexts = new HashMap<>();
		for(Method m: tdef.getMethods()) {
			EntryDef annEntry = m.getAnnotation(EntryDef.class);
			if(annEntry != null) {
				String entryName = m.getName();
				String path = annEntry.value();
				Consumer<Communication> service = (c)->{
					int z = 1;
					for(int i=0; i<slowdownLoop; i++)
						z = 1 + (z * i)%Math.max(z + i, 1);
					try {
						m.invoke(this, c);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				};
				entries.put(path, new Ent(taskName, entryName, service));
			}
			ContextDef annCtx = m.getAnnotation(ContextDef.class);
			if(annCtx != null) {
				String path = annCtx.value();
				BiConsumer<HttpExchange, HttpTask> service = (x,t)->{
					try {
						m.invoke(this, x,t);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				};
				contexts.put(path, service);
			}
		}
		task.setEntries(entries);
		task.setContexts(contexts);
		return task;
	}
	
	public static URI getURI(String task, String entry) {
		return tasksDir.get(task).get(entry);
	}
	
	public static URI getURIPage(String task, String page) {
		String p = page;
		if(!page.startsWith("/"))
			p = "/"+p;
		return tasksDirBase.get(task).resolve(p);
	}
}
