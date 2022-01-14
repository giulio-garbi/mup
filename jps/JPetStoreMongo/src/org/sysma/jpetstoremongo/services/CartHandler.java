package org.sysma.jpetstoremongo.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.velocity.VelocityContext;
import org.sysma.schedulerExecutor.Communication;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CartHandler {
	public JsonObject cart = null;
	
	private static String clean(String part) {
		while(part.length()>0 && part.charAt(0)=='\"')
			part = part.substring(1);
		while(part.length()>0 && part.charAt(part.length()-1)=='\"')
			part = part.substring(0, part.length()-1);
		return part;
	}
	
	public CartHandler(Communication comm) {
		if(comm != null)
			for(var cks:comm.getRequestHeaders().getOrDefault("Cookie", List.of())) {
				for(var ck: cks.split(";")) {
					ck = ck.stripLeading();
					String[] parts = ck.split("=");
					if(parts[0].equals("cart")) {
						cart = new Gson().fromJson(clean(parts[1]), JsonObject.class);
					}
				}
			}
		if(cart == null) {
			cart = new JsonObject();
			cart.add("cartItems", new JsonArray());
			cart.addProperty("subTotal", 0.0);
		}
	}
	
	public CartHandler() {
		cart = new JsonObject();
		cart.add("cartItems", new JsonArray());
		cart.addProperty("subTotal", 0.0);
	}
	
	public CartHandler(String cartJ) {
		cart = new Gson().fromJson(cartJ, JsonObject.class);
		if(cart == null) {
			cart = new JsonObject();
			cart.add("cartItems", new JsonArray());
			cart.addProperty("subTotal", 0.0);
		}
	}
	
	public void remove(String iid) {
		var items = cart.get("cartItems").getAsJsonArray();
		for(int i=0; i<items.size(); i++) {
			var itemLn = items.get(i).getAsJsonObject();
			if(itemLn.get("item").getAsJsonObject().get("itemId").getAsString().equals(iid)) {
				double totalLn = itemLn.get("total").getAsDouble();
				double totalCart = cart.get("subTotal").getAsDouble();
				cart.addProperty("subTotal", totalCart-totalLn);
				items.remove(i);
				break;
			}
		}
	}
	
	public JsonArray getItems() {
		return cart.get("cartItems").getAsJsonArray();
	}
	
	public void update(Map<String, String> qts) {
		var items = cart.get("cartItems").getAsJsonArray();
		double total = 0;
		for(int i=0; i<items.size(); i++) {
			var itemLn = items.get(i).getAsJsonObject();
			String itemId = itemLn.get("item").getAsJsonObject().get("itemId").getAsString();
			float qty = Float.parseFloat(qts.get(itemId));
			itemLn.addProperty("quantity", qty);
			double lnTot = qty * itemLn.get("item").getAsJsonObject().get("listPrice").getAsDouble();
			itemLn.addProperty("total", lnTot);
			total += lnTot;
		}
		cart.addProperty("subTotal", total);
	}
	
	public void add(Communication comm, String iid) throws InterruptedException, ExecutionException, UnsupportedOperationException, IOException {
		var items = cart.get("cartItems").getAsJsonArray();
		double totalCart = cart.get("subTotal").getAsDouble();
		var niCall = comm.asyncCallRegistry("catalog", "GetItem", (x)->{}, "itemId", iid).get();
		var newLine = new Gson().fromJson(Util.inputStreamToString(niCall.getEntity().getContent()), JsonObject.class);
		niCall.close();
		double itemPrice = newLine.get("item").getAsJsonObject().get("listPrice").getAsDouble();
		totalCart += itemPrice;
		newLine.addProperty("inStock", true);
		newLine.addProperty("quantity", 1);
		newLine.addProperty("total", itemPrice);
		cart.addProperty("subTotal", totalCart);
		items.add(newLine);
	}
	
	public void prepareContext(VelocityContext context) {
		context.put("cart", new Gson().fromJson(cart, Map.class));
	}
	
	public String toJson() {
		return new Gson().toJson(cart);
	}
}
