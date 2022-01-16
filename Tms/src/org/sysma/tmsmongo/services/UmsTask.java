package org.sysma.tmsmongo.services;

import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import freemarker.template.TemplateException;

@TaskDef(name = "ums")
public class UmsTask extends TaskDefinition{
	
	@EntryDef("/emailInUse")
	public void EmailInUse(Communication comm) throws IOException, TemplateException {
		var params = comm.getPostParameters();
		var email = params.get("email");
		var mdb = comm.getMongo().getDatabase("tms");
		var users = mdb.getCollection("users");
		var chosenUser = users.find(new Document("email", email)).first();
		
		if(chosenUser == null) {
			var oid = new ObjectId();
			comm.respond(200, oid.toHexString().toString().getBytes());
		} else {
			comm.respond(200, chosenUser.getObjectId("_id").toHexString().toString().getBytes());
		}
	}

	@EntryDef("/userById")
	public void UserById(Communication comm) throws IOException, TemplateException {
		var params = comm.getPostParameters();
		var uid = new ObjectId(params.get("id"));
		var mdb = comm.getMongo().getDatabase("tms");
		var users = mdb.getCollection("users");
		var chosenUser = users.find(new Document("_id", uid)).first();
		
		if(chosenUser == null) {
			comm.respond(200, "null".getBytes());
		} else {
			comm.respond(200, chosenUser.toJson().getBytes());
		}
	}

	@EntryDef("/getAllUsers")
	public void GetAllUsers(Communication comm) throws IOException, TemplateException {
		var mdb = comm.getMongo().getDatabase("tms");
		var gson = new GsonBuilder().setPrettyPrinting().create();
		var users = mdb.getCollection("users");
		var output = new JsonArray();
		for(var user:users.find()) {
			output.add(gson.fromJson(user.toJson(), JsonObject.class));
		}
		comm.respond(200, gson.toJson(output).getBytes());
	}
	
}
