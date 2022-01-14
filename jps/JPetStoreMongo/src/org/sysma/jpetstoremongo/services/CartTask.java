package org.sysma.jpetstoremongo.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.bson.Document;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name = "cart")
public class CartTask extends TaskDefinition {
	
	@EntryDef("/add")
	public void Add(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var cart = new CartHandler(params.get("cart"));
		cart.add(comm, params.get("workingItemId"));
		comm.respond(200, cart.toJson().getBytes());
	}

	@EntryDef("/remove")
	public void Remove(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var cart = new CartHandler(params.get("cart"));
		cart.remove(params.get("workingItemId"));
		comm.respond(200, cart.toJson().getBytes());
	}

	@EntryDef("/update")
	public void Update(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var cart = new CartHandler(params.get("cart"));
		@SuppressWarnings("unchecked")
		Map<String, String> data = new Gson().fromJson(params.get("data"), Map.class);
		cart.update(data);
		comm.respond(200, cart.toJson().getBytes());
	}
	
	@EntryDef("/getorders")
	public void GetOrders(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		String userId = params.get("userId");
		
		JsonArray orderList = new JsonArray();
		
		
		var mdb = comm.getMongo().getDatabase("jps");
		var orders = mdb.getCollection("orders");
		try(var cur = orders.find(new Document("useriid", userId)).cursor()){
			while(cur.hasNext()) {
				var doc = cur.next();
				JsonObject order = new JsonObject();
				order.addProperty("orderId", doc.getString("orderid"));
				order.addProperty("orderDate", doc.getString("orderdate"));
				order.addProperty("totalPrice", doc.getLong("totalprice"));
				orderList.add(order);
			}
		}
		comm.respond(200, new Gson().toJson(orderList).getBytes());
	}
	
	@EntryDef("/getorder")
	public void GetOrder(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		String orderId = params.get("orderId");
		JsonObject order = new JsonObject();
		
		var mdb = comm.getMongo().getDatabase("jps");
		var orders = mdb.getCollection("orders");
		var o_doc = orders.find(new Document("_id", orderId)).first();
		
		order.addProperty("orderId", o_doc.getString("orderid"));
		order.addProperty("orderDate", o_doc.getString("orderdate"));
		order.addProperty("shipAddress1", o_doc.getString("shipaddr1"));
		order.addProperty("shipAddress2", o_doc.getString("shipaddr2"));
		order.addProperty("shipCity", o_doc.getString("shipcity"));
		order.addProperty("shipState", o_doc.getString("shipstate"));
		order.addProperty("shipZip", o_doc.getString("shipzip"));
		order.addProperty("shipCountry", o_doc.getString("shipcountry"));
		order.addProperty("billAddress1", o_doc.getString("billaddr1"));
		order.addProperty("billAddress2", o_doc.getString("billaddr2"));
		order.addProperty("billCity", o_doc.getString("billcity"));
		order.addProperty("billState", o_doc.getString("billstate"));
		order.addProperty("billZip", o_doc.getString("billzip"));
		order.addProperty("billCountry", o_doc.getString("billcountry"));
		order.addProperty("courier", o_doc.getString("courier"));
		order.addProperty("totalPrice", o_doc.getString("totalprice"));
		order.addProperty("billToFirstName", o_doc.getString("billtofirstname"));
		order.addProperty("billToLastName", o_doc.getString("billtolastname"));
		order.addProperty("shipToFirstName", o_doc.getString("shiptofirstname"));
		order.addProperty("shipToLastName", o_doc.getString("shiptolastname"));
		order.addProperty("creditCard", o_doc.getString("creditcard"));
		order.addProperty("expiryDate", o_doc.getString("exprdate"));
		order.addProperty("cardType", o_doc.getString("cardtype"));
		order.addProperty("status", "P");
		
		JsonArray lineItems = new JsonArray();
		order.add("lineItems", lineItems);
		
		var lineitem = mdb.getCollection("lineitem");
		var item = mdb.getCollection("item");
		var product = mdb.getCollection("product");
		
		try(var cur = lineitem.find(new Document("orderid", orderId)).cursor()){
			while(cur.hasNext()) {
				var li_doc = cur.next();
				JsonObject lineItem = new JsonObject();
				double qty = li_doc.getDouble("quantity");
				double uprice = li_doc.getDouble("unitprice");
				lineItem.addProperty("quantity", qty);
				lineItem.addProperty("unitPrice", uprice);
				lineItem.addProperty("total", qty*uprice);
				
				var item_doc = item.find(new Document("_id", 
						li_doc.getString("itemid"))).first();
				
				lineItem.addProperty("item_attribute1", Util.onNull(item_doc.getString("attr1"),""));
				lineItem.addProperty("item_attribute2", Util.onNull(item_doc.getString("attr2"),""));
				lineItem.addProperty("item_attribute3", Util.onNull(item_doc.getString("attr3"),""));
				lineItem.addProperty("item_attribute4", Util.onNull(item_doc.getString("attr4"),""));
				lineItem.addProperty("item_attribute5", Util.onNull(item_doc.getString("attr5"),""));
				lineItem.addProperty("item_itemId", li_doc.getString("itemid"));
				
				var prod_doc = product.find(new Document("_id", 
						item_doc.getString("productid"))).first();

				lineItem.addProperty("item_product_name", prod_doc.getString("name"));
				lineItems.add(lineItem);
			}
		}
		comm.respond(200, new Gson().toJson(order).getBytes());
	}

	@EntryDef("/order")
	public void Order(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException, SQLException {
		var params = comm.getPostParameters();
		var cart = new CartHandler(params.get("cart"));
		var orderid = ThreadLocalRandom.current().nextInt()+"";
		
		var mdb = comm.getMongo().getDatabase("jps");
		var orders = mdb.getCollection("orders");
		var lineitem = mdb.getCollection("lineitem");
		
		orders.insertOne(new Document("_id", params.get("username"))
				.append("userid", params.get("account_userId"))
				.append("orderdate", params.get("order_orderDate"))
				.append("shipaddr1", params.get("order_shipAddress1"))
				.append("shipaddr2", params.get("order_shipAddress2"))
				.append("shipcity", params.get("order_shipCity"))
				.append("shipstate", params.get("order_shipState"))
				.append("shipzip", params.get("order_shipZip"))
				.append("shipcountry", params.get("order_shipCountry"))
				.append("billaddr1", params.get("order_billAddress1"))
				.append("billaddr2", params.get("order_billAddress2"))
				.append("billcity", params.get("order_billCity"))
				.append("billstate", params.get("order_billState"))
				.append("billzip", params.get("order_billZip"))
				.append("billcountry", params.get("order_billCountry"))
				.append("courier", "UPS")
				.append("totalprice", cart.cart.get("subTotal").getAsDouble())
				.append("billtofirstname", params.get("order_billToFirstName"))
				.append("billtolastname", params.get("order_billToLastName"))
				.append("shiptofirstname", params.get("order_shipToFirstName"))
				.append("shiptolastname", params.get("order_shipToLastName"))
				.append("creditcard", params.get("order_creditCard"))
				.append("exprdate", params.get("order_expiryDate"))
				.append("cardtype", params.get("order_cardType"))
				.append("locale", params.get("profile_langpref")));
		
		var items = cart.getItems();
		for(int i=0; i<items.size(); i++) {
			var item = items.get(i).getAsJsonObject();
			final int ii = i;
			lineitem.insertOne(new Document("orderid", orderid)
					.append("linenum", ii)
					.append("itemid", item.get("item").getAsJsonObject()
							.get("itemId").getAsString())
					.append("quantity", item.get("quantity").getAsInt())
					.append("unitprice", item.get("item").getAsJsonObject()
							.get("listPrice").getAsDouble()));
		}
		
		comm.respond(200, orderid.getBytes());
	}
}
