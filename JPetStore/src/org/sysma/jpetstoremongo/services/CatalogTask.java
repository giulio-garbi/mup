package org.sysma.jpetstoremongo.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;

import org.bson.Document;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name = "catalog")
public class CatalogTask extends TaskDefinition {
	
	@EntryDef("/getCatProd/")
	public void GetCategoryProducts(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var catid = params.get("catid");
		JsonObject catinfo = new JsonObject();
		
		var mdb = comm.getMongo().getDatabase("jps");
		var category = mdb.getCollection("category");
		var c_doc = category.find(new Document("_id", catid)).first();
		catinfo.addProperty("name", c_doc.getString("name"));
		
		var product = mdb.getCollection("product");
		JsonArray prods = new JsonArray();
		try(var cur = product.find(new Document("category", catid)).cursor()){
			while(cur.hasNext()) {
				var p_doc = cur.next();
				JsonObject prod = new JsonObject();
				prod.addProperty("productId", p_doc.getString("_id"));
				prod.addProperty("name", p_doc.getString("name"));
				prods.add(prod);
			}
		}
		catinfo.add("products", prods);
		comm.respond(200, new Gson().toJson(catinfo).getBytes());
	}
	
	
	@EntryDef("/getProd/")
	public void GetProduct(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var prdid = params.get("productId");
		JsonObject catinfo = new JsonObject();
		var mdb = comm.getMongo().getDatabase("jps");
		var product = mdb.getCollection("product");
		var p_doc = product.find(new Document("_id", prdid)).first();
		catinfo.addProperty("name", p_doc.getString("name"));
		catinfo.addProperty("categoryId", p_doc.getString("category"));
		
		var item = mdb.getCollection("item");
		JsonArray items = new JsonArray();
		try(var cur = item.find(new Document("productid", prdid)).cursor()){
			while(cur.hasNext()) {
				var i_doc = cur.next();
				JsonObject jitem = new JsonObject();
				jitem.addProperty("productId", i_doc.getString("productid"));
				jitem.addProperty("attribute1", Util.onNull(i_doc.getString("attr1"),""));
				jitem.addProperty("attribute2", Util.onNull(i_doc.getString("attr2"),""));
				jitem.addProperty("attribute3", Util.onNull(i_doc.getString("attr3"),""));
				jitem.addProperty("attribute4", Util.onNull(i_doc.getString("attr4"),""));
				jitem.addProperty("attribute5", Util.onNull(i_doc.getString("attr5"),""));
				jitem.addProperty("listPrice", i_doc.getDouble("listprice"));
				jitem.addProperty("itemId", i_doc.getString("_id"));
				items.add(jitem);
			}
		}
		catinfo.add("itemList", items);
		comm.respond(200, new Gson().toJson(catinfo).getBytes());
	}
	
	@EntryDef("/searchProd/")
	public void SearchProduct(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var kws = params.get("keyword").split("\\s+");
		
		HashSet<String> alreadyFound = new HashSet<>();
		JsonArray results = new JsonArray();
		
		var mdb = comm.getMongo().getDatabase("jps");
		var product = mdb.getCollection("product");
		
		for(var kw:kws) {
			/*
			 * "SELECT product.productid, product.name, product.descn "
			+ "FROM product "
			+ "WHERE product.name LIKE  '%' || ? || '%' "
			 */
			try(var cur = product.find(new Document("name", new Document("$regex",".*"+kw+".*"))).cursor()){
				while(cur.hasNext()) {
					var p_doc = cur.next();
					
					JsonObject prod = new JsonObject();
					String pid = p_doc.getString("_id");
					prod.addProperty("productId", pid);
					prod.addProperty("name", p_doc.getString("name"));
					prod.addProperty("description", p_doc.getString("descn"));
					if(!alreadyFound.contains(pid)) {
						results.add(prod);
						alreadyFound.add(pid);
					}
				}
			}
		}
		
		comm.respond(200, new Gson().toJson(results).getBytes());
	}
	
	
	@EntryDef("/getItem/")
	public void GetItem(Communication comm) throws IOException, SQLException {
		Map<String, String> params = comm.getPostParameters();
		var iid = params.get("itemId");
		JsonObject catinfo = new JsonObject();
		
		JsonObject jproduct = new JsonObject();
		JsonObject jitem = new JsonObject();
		
		var mdb = comm.getMongo().getDatabase("jps");
		var item = mdb.getCollection("item");
		var i_doc = item.find(new Document("_id", iid)).first();
		
		var pid = i_doc.getString("productid");
		
		var product = mdb.getCollection("product");
		var p_doc = product.find(new Document("_id", pid)).first();

		var inventory = mdb.getCollection("inventory");
		var inv_doc = inventory.find(new Document("_id", iid)).first();
		
		jproduct.addProperty("productId", pid);
		jproduct.addProperty("description", p_doc.getString("descn"));
		jitem.addProperty("itemId", iid);
		jitem.addProperty("attribute1", Util.onNull(i_doc.getString("attr1"),""));
		jitem.addProperty("attribute2", Util.onNull(i_doc.getString("attr2"),""));
		jitem.addProperty("attribute3", Util.onNull(i_doc.getString("attr3"),""));
		jitem.addProperty("attribute4", Util.onNull(i_doc.getString("attr4"),""));
		jitem.addProperty("attribute5", Util.onNull(i_doc.getString("attr5"),""));
		jproduct.addProperty("name", p_doc.getString("name"));
		jitem.addProperty("quantity", inv_doc.getLong("qty"));
		jitem.addProperty("listPrice", i_doc.getDouble("listprice"));
		
		catinfo.add("product", jproduct);
		catinfo.add("item", jitem);
		
		comm.respond(200, new Gson().toJson(catinfo).getBytes());
	}
}
