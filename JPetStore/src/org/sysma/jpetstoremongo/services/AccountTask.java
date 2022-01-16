package org.sysma.jpetstoremongo.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name = "account")
public class AccountTask extends TaskDefinition {
	
	@EntryDef("/sessuser/")
	public void GetSessionUser(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var sessid = Long.parseLong(params.get("sessid"));
		HashMap<String, String> userinfo = new HashMap<>();
		var mdb = comm.getMongo().getDatabase("jps");
		var sessions = mdb.getCollection("sessions");
		
		String userid = null;
		try(var cur = sessions.find(new Document("_id", sessid)).cursor()){
			if(cur.hasNext()) {
				var doc = cur.next();
				userid = doc.getString("username");
			}
		}
		
		if(userid != null) {
			var account = mdb.getCollection("account");
			try(var cur = account.find(new Document("_id", userid)).cursor()){
				var doc = cur.next();
				userinfo.put("firstName", doc.getString("firstname"));
				userinfo.put("lastName", doc.getString("lastname"));
				userinfo.put("email", doc.getString("email"));
				userinfo.put("phone", doc.getString("phone"));
				userinfo.put("address1", doc.getString("addr1"));
				userinfo.put("address2", doc.getString("addr2"));
				userinfo.put("city", doc.getString("city"));
				userinfo.put("state", doc.getString("state"));
				userinfo.put("zip", doc.getString("zip"));
				userinfo.put("country", doc.getString("country"));
			}
			var profile = mdb.getCollection("profile");
			try(var cur = profile.find(new Document("_id", userid)).cursor()){
				var doc = cur.next();
				userinfo.put("languagePreference", doc.getString("langpref"));
				userinfo.put("favouriteCategoryId", doc.getString("favcategory"));
				userinfo.put("listOption", doc.getLong("mylistopt")+"");
				userinfo.put("bannerOption", doc.getLong("banneropt")+"");
				userinfo.put("username", userid);
			}
			var bannerdata = mdb.getCollection("bannerdata");
			try(var cur = bannerdata.find(new Document("_id", userinfo.get("favouriteCategoryId"))).cursor()){
				var doc = cur.next();
				userinfo.put("bannerName", doc.getString("bannername"));
			}
		}
		if(userinfo.isEmpty()) {
			comm.respond(403);
		} else {
			comm.respond(200, new JSONObject(userinfo).toJSONString().getBytes());
		}
	}
	
	@EntryDef("/newaccount/")
	public void NewAccount(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var mdb = comm.getMongo().getDatabase("jps");
		var signon = mdb.getCollection("signon");
		signon.insertOne(
				new Document("_id", params.get("username"))
				.append("password", params.get("password")));
		
		var account = mdb.getCollection("account");
		account.insertOne(new Document("_id", params.get("username"))
				.append("firstname", params.get("account.firstName"))
				.append("lastname", params.get("account.lastName"))
				.append("email", params.get("account.email"))
				.append("phone", params.get("account.phone"))
				.append("addr1", params.get("account.address1"))
				.append("addr2", params.get("account.address2"))
				.append("city", params.get("account.city"))
				.append("state", params.get("account.state"))
				.append("zip", params.get("account.zip"))
				.append("status", "OK")
				.append("country", params.get("account.country")));
		
		var profile = mdb.getCollection("profile");
		profile.insertOne(new Document("_id", params.get("username"))
				.append("langpref", params.get("account.languagePreference"))
				.append("favcategory", params.get("account.favouriteCategoryId"))
				.append("listOption", params.getOrDefault("account.listOption", null)!=null?1:0)
				.append("bannerOption", params.getOrDefault("account.bannerOption", null)!=null?1:0));
		
		comm.respond(200);
	}
	
	@EntryDef("/signon/")
	public void Signon(Communication comm) {
		try {
			var params = comm.getPostParameters();
			boolean[] loginOk = {false};
			
			var mdb = comm.getMongo().getDatabase("jps");
			var signon = mdb.getCollection("signon");
			var so_doc = signon.find(
					new Document("_id", params.get("username"))
					.append("password", params.get("password"))).first();
			loginOk[0] = so_doc != null;
			if(loginOk[0]) {
				long sessid = ThreadLocalRandom.current().nextLong();
				var sessions = mdb.getCollection("sessions");
				sessions.insertOne(
						new Document("_id", sessid)
						.append("username", params.get("username")));
				comm.respond(200, (""+sessid).getBytes());
			} else {
				comm.respond(403);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/signout/")
	public void Signout(Communication comm) {
		try {
			Map<String, String> params = comm.getPostParameters();
			var sessid = Long.parseLong(params.get("sessid"));
			var mdb = comm.getMongo().getDatabase("jps");
			var sessions = mdb.getCollection("sessions");
			sessions.deleteOne(new Document("_id", sessid));
			comm.respond(200);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
}
