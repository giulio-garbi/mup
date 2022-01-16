package org.sysma.teastoremongo.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name="persistence")
public class PersistenceTask extends TaskDefinition {
	
	@EntryDef("/products/")
	public void Products(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var id = Integer.parseInt(param.get("id"));
		JsonObject[] ans = {null};
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("products")
				.find(new Document("_id",id)).cursor()){
			if(cur.hasNext()) {
				var doc = cur.next();
				ans[0] = new JsonObject();
				ans[0].addProperty("ID", doc.getInteger("_id"));
				ans[0].addProperty("description", doc.getString("description"));
				ans[0].addProperty("listPriceInCents", doc.getInteger("listpriceincents"));
				ans[0].addProperty("name", doc.getString("name"));
				ans[0].addProperty("categoryId", doc.getInteger("category_id"));
			} else {
				ans[0] = new JsonObject();
			}
		}
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(), 
				"Content-Type", "application/json");
	}

	@EntryDef("/users/")
	public void Users(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var id = Integer.parseInt(param.get("id"));
		JsonObject[] ans = {null};
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("user")
				.find(new Document("_id",id)).cursor()){
			if(cur.hasNext()) {
				var doc = cur.next();
				ans[0] = new JsonObject();
				ans[0].addProperty("ID", doc.getInteger("_id"));
				ans[0].addProperty("userName", doc.getString("username"));
				ans[0].addProperty("password", doc.getString("password"));
				ans[0].addProperty("realName", doc.getString("realname"));
				ans[0].addProperty("email", doc.getString("email"));
			} else {
				ans[0] = new JsonObject();
			}
		}
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(), 
				"Content-Type", "application/json");
	}
	
	@EntryDef("/usersByName/")
	public void UsersByName(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var name = (param.get("name"));
		JsonObject[] ans = {null};
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("user")
				.find(new Document("username",name)).cursor()){
			if(cur.hasNext()) {
				var doc = cur.next();
				ans[0] = new JsonObject();
				ans[0].addProperty("ID", doc.getInteger("_id"));
				ans[0].addProperty("userName", doc.getString("username"));
				ans[0].addProperty("password", doc.getString("password"));
				ans[0].addProperty("realName", doc.getString("realname"));
				ans[0].addProperty("email", doc.getString("email"));
			} else {
				ans[0] = new JsonObject();
			}
		}
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(), 
				"Content-Type", "application/json");
	}
	
	@EntryDef("/productsCount/")
	public void ProductsCount(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var category = Integer.parseInt(param.get("category"));
		String[] ans = {"0"};
		var client = comm.getMongo();
		var cnt = client.getDatabase("teastore").getCollection("products")
				.countDocuments(new Document("category_id",category));
		ans[0] = cnt+"";
		comm.respond(200, (ans[0]).getBytes());
	}
	
	@EntryDef("/productsList/")
	public void ProductsList(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var category = Integer.parseInt(param.get("category"));
		var idxFrom = Integer.parseInt(param.get("idxFrom"));
		var count = Integer.parseInt(param.get("count"));
		JsonArray ans = new JsonArray();
		
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("products")
				.find(new Document("category_id",category))
				.sort(new Document("_id",1)).skip(idxFrom).limit(count)
				.cursor()){
			
			while(cur.hasNext()) {
				var doc = cur.next();
				var jo = new JsonObject();
				jo.addProperty("ID", doc.getInteger("_id"));
				jo.addProperty("description", doc.getString("description"));
				jo.addProperty("listPriceInCents", doc.getInteger("listpriceincents"));
				jo.addProperty("name", doc.getString("name"));
				jo.addProperty("categoryId", doc.getInteger("category_id"));
				ans.add(jo);
			} 
		}
		comm.respond(200, new Gson().toJson(ans).getBytes(), 
				"Content-Type", "application/json");
	}

	@EntryDef("/categoriesList/")
	public void CategoriesList(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var idxFrom = Math.min(Integer.parseInt(param.get("idxFrom")),0);
		var count = Math.min(Integer.parseInt(param.get("count")),0);
		JsonArray ans = new JsonArray();
		
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("category")
				.find()
				.sort(new Document("_id",1)).skip(idxFrom).limit(count)
				.cursor()){
			
			while(cur.hasNext()) {
				var doc = cur.next();
				var jo = new JsonObject();
				jo.addProperty("ID", doc.getInteger("_id"));
				jo.addProperty("description", doc.getString("description"));
				jo.addProperty("name", doc.getString("name"));
				ans.add(jo);
			} 
		}
		comm.respond(200, new Gson().toJson(ans).getBytes(), 
				"Content-Type", "application/json");
	}

	@EntryDef("/ordersList/")
	public void OrdersList(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var userId = Integer.parseInt(param.get("userId"));
		JsonArray ans = new JsonArray();
		
		var client = comm.getMongo();
		List<Document> orders = List.of();
		try(var cur = client.getDatabase("teastore").getCollection("user")
				.find(new Document("_id",userId))
				.cursor()){
			
			if(cur.hasNext()) {
				var doc = cur.next();
				orders = doc.getList("orders", Document.class);
			} 
		}
		
		for(var doc:orders) {
			var jo = new JsonObject();
			jo.addProperty("ID", doc.getInteger("_id"));
			jo.addProperty("address1", doc.getString("address1"));
			jo.addProperty("address2", doc.getString("address2"));
			jo.addProperty("addressName", doc.getString("addressname"));
			jo.addProperty("creditCardCompany", doc.getString("creditcardcompany"));
			jo.addProperty("creditCardExpiryLocalDate", doc.getString("creditcardexpirylocaldate"));
			jo.addProperty("creditCardNumber", doc.getString("creditcardnumber"));
			jo.addProperty("orderTime", doc.getString("ordertime"));
			jo.addProperty("totalPriceInCents", doc.getInteger("totalpriceincents"));
			jo.addProperty("userId", userId);
			ans.add(jo);
		}
		comm.respond(200, new Gson().toJson(ans).getBytes(), 
				"Content-Type", "application/json");
	}
	
	@EntryDef("/categories/")
	public void Categories(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var param = comm.getPostParameters();
		var id = Integer.parseInt(param.get("id"));
		
		JsonObject[] ans = {null};
		
		var client = comm.getMongo();
		try(var cur = client.getDatabase("teastore").getCollection("category")
				.find(new Document("_id",id))
				.cursor()){
			
			if(cur.hasNext()) {
				var doc = cur.next();
				ans[0] = new JsonObject();
				ans[0].addProperty("ID", doc.getInteger("_id"));
				ans[0].addProperty("description", doc.getString("description"));
				ans[0].addProperty("name", doc.getString("name"));
			} else {
				ans[0] = new JsonObject();
			}
		}
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(), 
				"Content-Type", "application/json");
	}
}
