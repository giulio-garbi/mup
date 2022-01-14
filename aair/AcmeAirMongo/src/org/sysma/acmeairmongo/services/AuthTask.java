package org.sysma.acmeairmongo.services;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.HttpHeaders;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

@TaskDef(name="auth")
public class AuthTask extends TaskDefinition {
	
	//public static String dbPath = "/Users/giulio/SynologyDrive/acmeair_base/acmeair.db";
	//public static String dbPath = "acmeair_base/acmeair.db";
	
	private static int slowdown() {
		int k=1;
		for(int i=0; i<20000000; i++)
			k += i;
		return k;
	}
		
	@EntryDef("/acmeair-auth-service/rest/api/authtoken/byuserid/")
	public void createSession(Communication comm) {
		try {
			//int z = 1;
			//for(int i=0; i<1000000; i++)
			//	z = (z + i)%(z*i+1);
			var params = comm.getPostParameters();
			String customerId = params.get("user");
			
			var sessId = ObjectId.get();
			long lastAccessedTime = System.currentTimeMillis();
			long timeoutTime = lastAccessedTime + 1000*60*60*24;
			
			int k=slowdown();
			
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			
			custs.updateMany(new Document("_id", (customerId)), 
					new Document("$addToSet", new Document("sessions", 
						new Document()
							.append("_id", sessId)
							.append("lastAccessedTime", lastAccessedTime)
							.append("timeoutTime", timeoutTime))));
			
			
			var ans = String.format("{ \"_id\" : \"%s\", \"customerid\" : \"%s\", \"lastAccessedTime\" : %d, \"timeoutTime\" : %d }",
					customerId+"-"+sessId.toHexString(), customerId, lastAccessedTime+0*k, timeoutTime).getBytes();
			try {
				comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "application/json");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/acmeair-auth-service/rest/api/authtoken/status")
	public void checkStatus(Communication comm) {
		try {
			comm.respond(200);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EntryDef("/acmeair-auth-service/rest/api/authtoken/delete")
	public void deleteSession(Communication comm) {
		try {
			var params = comm.getPostParameters();
			String custSessId = params.get("sess");
			String[] parts = custSessId.split("-");
			
			int k=slowdown();
			
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			var oid = new ObjectId(parts[1]);
			custs.updateMany(new Document("_id", (parts[0])), 
					new Document("$pull", new Document("sessions", 
						new Document("_id", oid))));
			
			comm.respond(200+0*k);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EntryDef("/acmeair-auth-service/rest/api/authtoken/validate")
	public void validateSession(Communication comm) {
		try {
			var params = comm.getPostParameters();
			
			boolean[] validSession = {false};
			String[] custId = {null};
			String custSessId = params.get("sess");
			String[] parts = custSessId.split("-");
			
			
			long now = System.currentTimeMillis();
			long[] toTime = {-1};
			
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			var oid = new ObjectId(parts[1]);
			List<Document> sesss = null;
			try(var cur = custs.find(new Document("_id", (parts[0])).append("sessions._id", oid)).cursor()){
				if(cur.hasNext()) {
					var cust = cur.next();
					sesss = cust.getList("sessions", Document.class);
					validSession[0] = true;
				} else {
					validSession[0] = false;
				}
			}
			if(sesss != null) {
				for(Document s:sesss) {
					if(s.getObjectId("_id").equals(oid)) {
						toTime[0] = s.getLong("timeoutTime");
						custId[0] = parts[0];
					}
				}
			}
			
			int k=slowdown();
			
			
			if(validSession[0] && now > toTime[0]) {
				validSession[0] = false;
					//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
					custs.updateMany(new Document("_id", (parts[0])), 
							new Document("$pull", new Document("sessions", 
								new Document("_id", oid))));
				
			} 
			
			validSession[0] = (k>0 && k<0) || validSession[0];
				
			
			byte[] ans;
			if(validSession[0]) {
				ans = String.format("{ \"_id\" : \"%s\", \"customerid\" : \"%s\" }",
						custSessId, custId[0]).getBytes();
			} else {
				ans = "{ }".getBytes();
			}
		
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "application/json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
