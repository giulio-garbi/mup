package org.sysma.tmstwomongo.main;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;
import org.sysma.tmsmongo.services.Util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ClientTask extends MainTaskDefinition<String[]> {
	
	static <E> E getRandomSetElement(Set<E> set) {
	    return set.stream().skip(ThreadLocalRandom.current().nextInt(set.size())).findFirst().orElse(null);
	}
	
	static JsonElement getRandomJAElement(JsonArray ja) {
		var i = ThreadLocalRandom.current().nextInt(ja.size());
		return ja.get(i);
	}
	
	private static int getTime(int msec) {
		return msec*95;
		//var u = 1.-ThreadLocalRandom.current().nextDouble();
		//return (int) (-Math.log(u)*msec);
	}

	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		var gson = new Gson();
		Thread.sleep(getTime(10));
		try {
			var users = gson.fromJson(
					Util.getAndClose(comm.asyncCallRegistry("cms", "GetAllUsers", (x)->{})), JsonArray.class);
			Thread.sleep(getTime(50));
			var userid = getRandomJAElement(users).getAsJsonObject().get("_id")
					.getAsJsonObject().get("$oid").getAsString();
			Util.getAndClose(comm.asyncCallRegistry("cms", "GetExamineeInfo", (x)->{}, "id", userid));
			Thread.sleep(getTime(50));
			var confs = gson.fromJson(
					Util.getAndClose(comm.asyncCallRegistry("cms", "GetConfigurations", (x)->{})), JsonArray.class);
			Thread.sleep(getTime(50));
			var confid = getRandomJAElement(confs).getAsJsonObject().get("_id")
					.getAsJsonObject().get("$oid").getAsString();
			var exam = gson.fromJson(
					Util.getAndClose(comm.asyncCallRegistry("cms", "CreateExam", (x)->{},
							"examinee","exmn", "configId", confid, "examDate", "foo")), JsonObject.class);
			Thread.sleep(getTime(50));
			for(int i=0; i<3; i++){
				Util.getAndClose(comm.asyncCallRegistry("ems", "GetQuestions", (x)->{}, "id", confid));
				Thread.sleep(getTime(50));
			}
			var examid = exam.get("_id")
					.getAsJsonObject().get("$oid").getAsString();
			Util.getAndClose(comm.asyncCallRegistry("cms", "DeleteExam", (x)->{}, "id", examid));
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		//System.out.println("end");
		
	}

}
