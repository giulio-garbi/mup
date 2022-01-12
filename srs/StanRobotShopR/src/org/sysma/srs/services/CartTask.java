package org.sysma.srs.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.Queries;
import org.sysma.schedulerExecutor.Queries.ReadQuery;
import org.sysma.schedulerExecutor.Queries.WriteQuery;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name = "cart")
public class CartTask extends TaskDefinition{

	private static class Cart {
		private static final double TAX_RATE = 1.2;
		
		private double total; //without tax
		private double tax; 
		private HashMap<String, JsonObject> items;
		private JsonObject shipping = null;
		
		Cart(JsonObject data){
			if(data == null) {
				total = 0;
				tax = 0;
				items = new HashMap<>();
			} else {
				total = data.get("total").getAsDouble();
				tax = data.get("tax").getAsDouble();
				items = new HashMap<>();
				var itArray = data.get("items").getAsJsonArray();
				for(int i=0; i<itArray.size(); i++) {
					var it = itArray.get(i).getAsJsonObject();
					if(it.get("sku").getAsString().equals("SHIP")) {
						shipping = it;
					} else {
						items.put(it.get("sku").getAsString(), it);
					}
				}
			}
		}
		
		void addShipping(String dest, double price) {
			//{"qty":1,"sku":"SHIP","name":"shipping to Italy Romanengo","price":35.2,"subtotal":35.2}
			if(shipping != null) {
				total -= shipping.get("price").getAsDouble();
			} else {
				shipping = new JsonObject();
			}
			shipping.addProperty("qty", 1);
			shipping.addProperty("sku", "SHIP");
			shipping.addProperty("name", "Shipping to "+dest);
			shipping.addProperty("price", price);
			shipping.addProperty("subtotal", price);
			total += price;
			tax = total - total / TAX_RATE;
		}
		
		void removeShipping() {
			if(shipping != null) {
				total -= shipping.get("price").getAsDouble();
				tax = total - total / TAX_RATE;
				shipping = null;
			}
		}
		
		
		void add(JsonObject itemFromCatalog, int qty) {
			var sku = itemFromCatalog.get("sku").getAsString();
			if(items.containsKey(sku)) {
				var oldQty = items.get(sku).get("qty").getAsInt();
				update(sku, qty + oldQty);
			} else {
				JsonObject item = new JsonObject();
				var price = itemFromCatalog.get("price").getAsDouble();
				var subt = qty*price;
				item.addProperty("qty", qty);
				item.addProperty("sku", sku);
				item.addProperty("name", itemFromCatalog.get("name").getAsString());
				item.addProperty("price", price);
				item.addProperty("subtotal", subt);
				items.put(sku, item);
				total += subt;
				tax = total - total / TAX_RATE;
			}
		}
		
		void update(String sku, int qty) {
			if(qty == 0) {
				delete(sku);
			} else {
				JsonObject item = items.get(sku);
				var price = item.get("price").getAsDouble();
				var oldSubt = item.get("subtotal").getAsDouble();
				var newsubt = qty*price;
				item.addProperty("subtotal", newsubt);
				item.addProperty("qty", qty);
				total += newsubt - oldSubt;
				tax = total - total / TAX_RATE;
			}
		}
		
		void delete(String sku) {
			JsonObject item = items.get(sku);
			var oldSubt = item.get("subtotal").getAsDouble();
			total -= oldSubt;
			tax = total - total / TAX_RATE;
			items.remove(sku);
		}
		
		boolean isEmpty() {
			return items.isEmpty() && shipping == null;
		}
		
		String getJson() {
			if(isEmpty()) {
				return "cart not found";
			} else {
				JsonArray itemsArr = new JsonArray();
				for(var item:items.values())
					itemsArr.add(item);
				if(shipping != null)
					itemsArr.add(shipping);
				JsonObject cart = new JsonObject();
				cart.addProperty("total", total);
				cart.addProperty("tax", tax);
				cart.add("items", itemsArr);
				return new Gson().toJson(cart);
			}
		}
	}
	
	private final static String dbPath = Util.dbBasePath+"cart.db";
	
	private final static ReadQuery getCartQuery = Queries.registerRead(dbPath, 
			"GetCart", "SELECT Data FROM Cart WHERE ID = ?");
	private final static WriteQuery setCartQuery = Queries.registerWrite(dbPath, 
			"SetCart", 
			"INSERT INTO Cart (ID, Data) "
			+ "VALUES(?, ?) "
			+ "ON CONFLICT(ID) "
			+ "DO UPDATE SET Data = ?");
	private final static WriteQuery deleteCartQuery = Queries.registerWrite(dbPath, 
			"DeleteCart", 
			"DELETE FROM Cart WHERE ID = ?");
	private final static WriteQuery renameCartQuery = Queries.registerWrite(dbPath, 
			"RenameCart", 
			"UPDATE OR REPLACE Cart SET ID = ? WHERE ID = ? ");
					//+ "ON CONFLICT(ID) "
					//+ "DO UPDATE SET Data = (SELECT Data FROM Cart WHERE ID = ? )");
	
	private static Cart getCart(Communication comm, String user) throws SQLException {
		Cart[] ans = {null};
		comm.readQuery(getCartQuery, (ps)->{
			try {
				ps.setString(1, user);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				if(rs.next())
					ans[0] = new Cart(new Gson().fromJson(rs.getString(1), JsonObject.class));
				else
					ans[0] = new Cart(null);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return ans[0];
	}
	
	@EntryDef("/api/cart/cart")
	public void GetCart(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		Cart cart = getCart(comm, user);
		Util.busyWait(10000000);
		comm.respond(200, cart.getJson().getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/cart/add")
	public void CartAdd(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var prod = params.get("prod");
		var qty = Integer.parseInt(params.get("qty"));
		
		var ansConn = comm.asyncCallRegistry("catalog", "GetProduct", (x)->{}, "prod", prod).get();
		var itemJson = Util.inputStreamToString(ansConn.getEntity().getContent());
		ansConn.close();
		var item = new Gson().fromJson(itemJson, JsonObject.class);
		
		Cart cart = getCart(comm, user);
		cart.add(item, qty);
		var jsonCart = cart.getJson();
		
		if(cart.isEmpty()) {
			comm.writeQuery(deleteCartQuery, (ps)->{
				try {
					ps.setString(1, user);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} else {
			comm.writeQuery(setCartQuery, (ps)->{
				try {
					ps.setString(1, user);
					ps.setString(2, jsonCart);
					ps.setString(3, jsonCart);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		comm.respond(200, jsonCart.getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/cart/delete")
	public void CartDelete(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		comm.writeQuery(deleteCartQuery, (ps)->{
			try {
				ps.setString(1, user);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		comm.respond(200);
	}
	
	@EntryDef("/api/cart/rename")
	public void CartRename(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var userOld = params.get("userOld");
		var userNew = params.get("userNew");
		comm.writeQuery(renameCartQuery, (ps)->{
			try {
				ps.setString(1, userNew);
				ps.setString(2, userOld);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		Cart cart = getCart(comm, userNew);
		comm.respond(200, cart.getJson().getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/cart/update")
	public void CartUpdate(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var prod = params.get("prod");
		var qty = Integer.parseInt(params.get("qty"));
		
		Cart cart = getCart(comm, user);
		if(prod.equals("SHIP") && qty == 0) {
			cart.removeShipping();
		} else {
			cart.update(prod, qty);
		}
		var jsonCart = cart.getJson();
		
		if(cart.isEmpty()) {
			comm.writeQuery(deleteCartQuery, (ps)->{
				try {
					ps.setString(1, user);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} else {
			comm.writeQuery(setCartQuery, (ps)->{
				try {
					ps.setString(1, user);
					ps.setString(2, jsonCart);
					ps.setString(3, jsonCart);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		comm.respond(200, jsonCart.getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/cart/shipping")
	public void AddShipping(Communication comm) throws IOException, SQLException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var dest = params.get("dest");
		var cost = Double.parseDouble(params.get("cost"));
		
		Cart cart = getCart(comm, user);
		cart.addShipping(dest, cost);
		var jsonCart = cart.getJson();
		
		if(cart.isEmpty()) {
			comm.writeQuery(deleteCartQuery, (ps)->{
				try {
					ps.setString(1, user);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} else {
			comm.writeQuery(setCartQuery, (ps)->{
				try {
					ps.setString(1, user);
					ps.setString(2, jsonCart);
					ps.setString(3, jsonCart);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		comm.respond(200, jsonCart.getBytes(),"Content-Type","application/json");
	}
}
