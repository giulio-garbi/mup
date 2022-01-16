package org.sysma.tmsmongo.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@TaskDef(name = "ems")
public class EmsTask extends TaskDefinition{
	
	@EntryDef("/createExam")
	public void CreateExam(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		
		var mdb = comm.getMongo().getDatabase("tms");
		var exams = mdb.getCollection("exams");
		
		var exam = new Document("_id", new ObjectId())
				.append("examinee", params.get("examinee"))
				.append("configId", new ObjectId(params.get("configId")))
				.append("examDate", params.get("examDate"));
		
		exams.insertOne(exam);
		
		comm.respond(200, exam.toJson().getBytes());
	}
	
	@EntryDef("/deleteExam")
	public void DeleteExam(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		
		var mdb = comm.getMongo().getDatabase("tms");
		var exams = mdb.getCollection("exams");
		var examId = new ObjectId(params.get("id"));
		exams.deleteOne(new Document("_id",examId));
		
		comm.respond(200, "".getBytes());
	}
	
	@EntryDef("/getQuestions")
	public void GetQuestions(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var configId = params.get("id");
		
		var ans = comm.asyncCallRegistry("qms", "CreateTest", (x)->{}, Map.of("configId", configId)).get();
		var quests = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, quests.getBytes());
	}
	
	@EntryDef("/listAllExams")
	public void ListAllExams(Communication comm) throws IOException {
		var gson = new Gson();
		var mdb = comm.getMongo().getDatabase("tms");
		var exams = mdb.getCollection("exams");
		var output = new JsonArray();
		exams.find().forEach(exam->{
			output.add(gson.fromJson(exam.toJson(), JsonObject.class));
		});
		comm.respond(200, output.toString().getBytes());
	}
	
	@EntryDef("/listAllQuestionsForExam")
	public void ListAllQuestionsForExam(Communication comm) throws IOException {
		var gson = new Gson();
		var params = comm.getPostParameters();
		
		var mdb = comm.getMongo().getDatabase("tms");
		
		var examId = new ObjectId(params.get("id"));
		var exams = mdb.getCollection("exams");
		var exam = exams.find(new Document("_id",examId)).first();
		
		var configId = exam.getObjectId("configId");
		var confs = mdb.getCollection("confs");
		var conf = confs.find(new Document("_id", configId)).first();
		
		if(conf == null) {
			comm.respond(200, "[]".getBytes());
			return;
		}
		
		var groupIds = conf.get("groups");
		var confgroups = mdb.getCollection("confgroups");
		var groups = new ArrayList<Document>();
		confgroups.find(new Document("_id", new Document("$in", groupIds))).forEach(g->{
			groups.add(g);
		});
		
		var questions = mdb.getCollection("questions");
		var outQuestions = new ArrayList<Document>();
		
		for(var group:groups) {
			Document qCriteria = new Document("category", group.get("category"))
					.append("level", group.get("level"));
			if(group.containsKey("language"))
				qCriteria.append("language", group.get("language"));
			var gquests = questions.find(qCriteria);
			gquests.forEach(gq->{
				outQuestions.add(gq);
			});
		}
		
		var output = new JsonArray();
		for(var q:outQuestions) {
			output.add(gson.fromJson(q.toJson(), JsonElement.class));
		}
		
		comm.respond(200, output.toString().getBytes());
	}
	
}
