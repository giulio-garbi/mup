package org.sysma.schedulerExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Stream;

class FileServer extends Entry {
	final String basePath;
	final int slowdownLoop;
	
	private final HashSet<String> allFilesEntries = new HashSet<>();
	private synchronized void registerEntry(String entName){
		allFilesEntries.add(entName);
	}
	
	synchronized Stream<String> getAllFilesEntries(){
		return allFilesEntries.stream();
	}

	public FileServer(String name, String basePath, int slowdownLoop) {
		super(name, "FileServer");
		this.basePath = basePath;
		this.slowdownLoop = slowdownLoop;
	}

	@Override
	void run(Communication comm, String threadName) {
		comm.threadName = threadName;
		String entName = this.entryName+comm.request.getRequestURI().getPath();
		registerEntry(entName);
		comm.setEntryName(entName);
		log.get().add(new LogLine.Begin(taskName, entName, comm.client, System.currentTimeMillis()));
		service(comm);
	}
	
	public void service(Communication comm) {
		//System.out.println("->1 "+comm.request.getRequestURI());
		int z = 1;
		for(int i=0; i<slowdownLoop; i++)
			z = 1 + (z * i)%Math.max(z + i, 1);
		//System.out.println("->2 "+comm.request.getRequestURI());
		Path fl = Path.of(basePath, comm.request.getRequestURI().getPath());
		if(Files.isDirectory(fl)) {
			fl = Path.of(fl.toString(), "index.html");
		}
		//System.out.println("? "+fl);
		boolean do404 = true;
		byte [] ans = null;
		try {
			ans = Files.readAllBytes(fl);
			do404 = false;
			//System.out.println("! "+fl);
		} catch (IOException e) {
		}
		
		if(do404) {
			try {
				comm.respond(404);
				System.out.println("404 "+fl);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				comm.respond(200, ans);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
