package org.sysma.teastoremongo.services;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name="recommender")
public class RecommenderTask extends TaskDefinition {
	//TODO
	@EntryDef("/recommend/")
	public void Recommend(Communication comm) throws IOException {
		var rnd = ThreadLocalRandom.current();
		int prod1 = (int) (rnd.nextFloat()*500);
		int prod2 = (int) (rnd.nextFloat()*500);
		int prod3 = (int) (rnd.nextFloat()*500);
		comm.respond(200, ("["+prod1+","+prod2+","+prod3+"]").toString().getBytes());
	}
}
