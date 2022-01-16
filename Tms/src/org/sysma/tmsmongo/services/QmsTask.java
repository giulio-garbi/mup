package org.sysma.tmsmongo.services;

import java.io.IOException;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@TaskDef(name = "qms")
public class QmsTask extends TaskDefinition{
	
	@EntryDef("/createTest")
	public void CreateTest(Communication comm) throws IOException {
		var gson = new GsonBuilder().setPrettyPrinting().create();
		var params = comm.getPostParameters();
		
		var mdb = comm.getMongo().getDatabase("tms");
		
		var configId = new ObjectId(params.get("configId"));
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
			var gquests = questions.find(qCriteria).limit(group.getInteger("count"));
			gquests.forEach(gq->{
				outQuestions.add(gq);
			});
		}
		
		var output = new JsonArray();
		for(var q:outQuestions) {
			output.add(gson.fromJson(q.toJson(), JsonElement.class));
		}
		
		comm.respond(200, gson.toJson(output).getBytes());
	}
	
	
	@EntryDef("/findAllConfigurations")
	public void FindAllConfigurations(Communication comm) throws IOException {
		var gson = new GsonBuilder().setPrettyPrinting().create();
		var mdb = comm.getMongo().getDatabase("tms");
		var confs = mdb.getCollection("confs");
		var output = new JsonArray();
		confs.find().forEach(conf->{
			output.add(gson.fromJson(conf.toJson(), JsonObject.class));
		});
		comm.respond(200, gson.toJson(output).getBytes());
	}

	
	@EntryDef("/findAllCategoryInfos")
	public void FindAllCategoryInfos(Communication comm) throws IOException {
		var gson = new GsonBuilder().setPrettyPrinting().create();
		var mdb = comm.getMongo().getDatabase("tms");
		var cats = mdb.getCollection("categories");
		var output = new JsonArray();
		cats.find().forEach(cat->{
			output.add(gson.fromJson(cat.toJson(), JsonObject.class));
		});
		comm.respond(200, gson.toJson(output).getBytes());
	}
	
	
	@EntryDef("/createConfiguration")
	public void CreateConfiguration(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		
		var mdb = comm.getMongo().getDatabase("tms");
		var confs = mdb.getCollection("confs");
		var confgroups = mdb.getCollection("confgroups");
		
		var groupIds = new ArrayList<ObjectId>();
		var conf = new Document()
				.append("_id", new ObjectId())
				.append("name", params.get("name"))
				.append("description", params.get("description"))
				.append("groups", groupIds);
		
		var groupsJson = new GsonBuilder().setPrettyPrinting().create().fromJson(params.get("groups"), JsonArray.class);
		groupsJson.forEach(gje->{
			var groupJson = gje.getAsJsonObject(); 
			if(groupJson.has("isNew") && groupJson.get("isNew").getAsBoolean()) {
				var confGroup = new Document("_id", new ObjectId())
						.append("category", new ObjectId(groupJson.get("category").getAsString()))
						.append("count", groupJson.get("count").getAsInt())
						.append("language", groupJson.get("language").getAsString())
						.append("level", groupJson.get("level").getAsInt());
				confgroups.insertOne(confGroup);
				groupIds.add(confGroup.getObjectId("_id"));
			} else if (groupJson.has("_id")) {
				groupIds.add(new ObjectId(groupJson.get("_id").getAsString()));
			}
		});
		confs.insertOne(conf);
		comm.respond(200, conf.toJson().getBytes());
	}
	
}
