package org.sysma.jpetstoremongo.services;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@TaskDef(name = "frontend", 
	filePath = Util.basePath+"frontend")
public class FrontendTask extends TaskDefinition{

	final static String sessidCookie = "sessid";
	
	private VelocityEngine getVelocityEngine() {
		return Util.getEngine(Util.basePath+"frontend");
	}
	
	private String getSession(Communication comm) {
		for(var cks:comm.getRequestHeaders().getOrDefault("Cookie", List.of())) {
			for(var ck: cks.split(";")) {
				ck = ck.stripLeading();
				String[] parts = ck.split("=");
				if(parts[0].equals(sessidCookie))
					return parts[1];
			}
		}
		return null;
	}
	
	Map<String,String> prepareContextLogin(Communication comm, VelocityContext context) throws InterruptedException, ExecutionException, JsonSyntaxException, UnsupportedOperationException, IOException {
		String session = getSession(comm);
		
		if(session == null) {
			context.put("authenticated", false);
			return null;
		} else {
			var ans = comm.asyncCallRegistry("account", "GetSessionUser", (x)->{}, "sessid", session).get();
			if(ans.getCode() == 200) {
				context.put("authenticated", true);
				@SuppressWarnings("unchecked")
				var account = (Map<String,String>)(new Gson().fromJson(Util.inputStreamToString(ans.getEntity().getContent()), Map.class));
				ans.close();
				context.put("account", account);
				return account;
			} else {
				context.put("authenticated", false);
				ans.close();
				return null;
			}
		}
	}
	
	@EntryDef("/actions/Catalog.action")
	public void Main(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		Template t = ve.getTemplate("templates/main.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/catalog/viewCategory")
	public void ViewCategory(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		var params = comm.getPostParameters();
		Template t = ve.getTemplate("templates/Category.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		
		var catalog = comm.asyncCallRegistry("catalog", "GetCategoryProducts", (x)->{}, "catid", params.get("categoryId")).get();
		var catdata = new Gson().fromJson(Util.inputStreamToString(catalog.getEntity().getContent()), Map.class);
		catalog.close();
		
		context.put("category", catdata);
		
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EntryDef("/catalog/searchProducts")
	public void SearchProds(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		var params = comm.getPostParameters();
		Template t = ve.getTemplate("templates/SearchProducts.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		
		var catalog = comm.asyncCallRegistry("catalog", "SearchProduct", (x)->{}, "keyword", params.get("keyword")).get();
		var catdata = new Gson().fromJson(Util.inputStreamToString(catalog.getEntity().getContent()), ArrayList.class);
		catalog.close();
		
		context.put("productList", catdata);
		
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/catalog/viewItem")
	public void ViewItem(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		var params = comm.getPostParameters();
		Template t = ve.getTemplate("templates/Item.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		
		var catalog = comm.asyncCallRegistry("catalog", "GetItem", (x)->{}, "itemId", params.get("itemId")).get();
		var catdata = new Gson().fromJson(Util.inputStreamToString(catalog.getEntity().getContent()), Map.class);
		catalog.close();
		
		context.put("product", catdata.get("product"));
		context.put("item", catdata.get("item"));
		
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/catalog/viewProduct")
	public void ViewProduct(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		var params = comm.getPostParameters();
		Template t = ve.getTemplate("templates/Product.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		
		String productId = params.get("productId");
		
		var catalog = comm.asyncCallRegistry("catalog", "GetProduct", (x)->{}, "productId", productId).get();
		var prddata = new Gson().fromJson(Util.inputStreamToString(catalog.getEntity().getContent()), Map.class);
		catalog.close();
		
		context.put("product", prddata);
		
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/account/newAccount")
	public void CreateNewAccount(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		comm.asyncCallRegistry("account", "NewAccount", (z)->{}, comm.getPostParameters()).get().close();
		comm.respond(200, Util.doRedirect("/account/signonForm").getBytes());
	}
	
	@EntryDef("/account/newAccountForm")
	public void NewAccountForm(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		Template t = ve.getTemplate("templates/NewAccountForm.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/account/signonForm")
	public void SignonForm(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		Template t = ve.getTemplate("templates/signonForm.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/account/editAccountForm")
	public void EditAccountForm(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		Template t = ve.getTemplate("templates/EditAccountForm.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/account/login")
	public void Login(Communication comm) {
		try {
			var params = comm.getPostParameters();
			var username = params.get("username");
			var password = params.get("password");
			var ans = comm.asyncCallRegistry("account", "Signon", req->{}, 
					"username", username, "password", password).get();
			boolean loginOk = ans.getCode()==200;
			String sessid = "";
			if(loginOk)
				sessid = Util.inputStreamToString(ans.getEntity().getContent());
			ans.close();
			if(loginOk) {
				var cartJson = new CartHandler().toJson();
				comm.respond(200, Util.doRedirect("/actions/Catalog.action").getBytes(),
						"Set-Cookie", sessidCookie+"="+sessid+"; Path=/",
						"Set-Cookie", "cart="+cartJson+"; Path=/");
			} else {
				comm.respond(200, Util.doRedirect("/account/signonForm").getBytes(),
						"Set-Cookie", sessidCookie+"=; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT",
						"Set-Cookie", "cart=; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/account/signoff")
	public void Signout(Communication comm) {
		String session = getSession(comm);
		try {
			comm.asyncCallRegistry("account", "Signout", x->{}, "sessid", session).get().close();
			comm.respond(200, Util.doRedirect("/actions/Catalog.action").getBytes(),
					"Set-Cookie", sessidCookie+"=; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT",
					"Set-Cookie", "cart=; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/cart/addItemToCart")
	public void AddToCart(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var cartIn = new CartHandler(comm);
		var params = comm.getPostParameters();
		var ans = comm.asyncCallRegistry("cart", "Add", (x)->{}, "cart", cartIn.toJson(), 
				"workingItemId", params.get("workingItemId")).get();
		var cartOut = new CartHandler(Util.inputStreamToString(ans.getEntity().getContent()));
		ans.close();
		showCart(comm, cartOut);
	}
	
	@EntryDef("/cart/removeItemFromCart")
	public void RemoveFromCart(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var cartIn = new CartHandler(comm);
		var params = comm.getPostParameters();
		var ans = comm.asyncCallRegistry("cart", "Remove", (x)->{}, "cart", cartIn.toJson(), 
				"workingItemId", params.get("workingItemId")).get();
		var cartOut = new CartHandler(Util.inputStreamToString(ans.getEntity().getContent()));
		ans.close();
		showCart(comm, cartOut);
	}
	
	@EntryDef("/cart/updateCartQuantities")
	public void UpdateCart(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var cartIn = new CartHandler(comm);
		var params = comm.getPostParameters();
		var ans = comm.asyncCallRegistry("cart", "Update", (x)->{}, "cart", cartIn.toJson(), 
				"data", new Gson().toJson(params)).get();
		var cartOut = new CartHandler(Util.inputStreamToString(ans.getEntity().getContent()));
		ans.close();
		showCart(comm, cartOut);
	}
	
	@EntryDef("/cart/viewCart")
	public void ViewCart(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var cart = new CartHandler(comm);
		showCart(comm, cart);
	}
	
	private void showCart(Communication comm, CartHandler cart) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var ve = getVelocityEngine();
		Template t = ve.getTemplate("templates/Cart.vm");
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		cart.prepareContext(context);
		var cartJson = cart.toJson();
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes(),
					"Set-Cookie", "cart="+cartJson+"; Path=/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EntryDef("/order/newOrderForm")
	public void NewOrderForm(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		VelocityContext context = new VelocityContext();
		prepareContextLogin(comm, context);
		if((Boolean) context.get("authenticated")) {
			var ve = getVelocityEngine();
			Template t = ve.getTemplate("templates/NewOrderForm.vm");

			@SuppressWarnings("rawtypes")
			Map account = (Map) context.get("account");
			
			context.put("order_orderDate", java.time.LocalDate.now().toString());
			context.put("order", Map.of(
					"creditCard", "999 9999 9999 9999",
					"expiryDate", "12/03",
					"billToFirstName", account.get("firstName"),
					"billToLastName", account.get("lastName"),
					"billAddress1", account.get("address1"),
					"billAddress2", account.get("address2"),
					"billCity", account.get("city"),
					"billState", account.get("state"),
					"billZip", account.get("zip"),
					"billCountry", account.get("country")
			));
			
			StringWriter sw = new StringWriter();
			t.merge(context, sw);
			try {
				comm.respond(200, sw.toString().getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			Login(comm);
		}
	}
	
	@EntryDef("/order/newOrderConfirmed")
	public void NewOrderConfirmed(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		VelocityContext context = new VelocityContext();
		var account  = prepareContextLogin(comm, context);
		var params = comm.getPostParameters();
		params.put("account_userId", account.get("username"));
		params.put("profile_langpref", account.get("languagePreference"));
		var cart = new CartHandler(comm);
		params.put("cart", cart.toJson());
		
		var ans = comm.asyncCallRegistry("cart", "Order", (s)->{}, params).get();
		String orderId = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		doViewOrder(comm, orderId, true);
	}
	
	@EntryDef("/order/newOrder")
	public void NewOrder(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		VelocityContext context = new VelocityContext();
		Template t;
		var ve = getVelocityEngine();
		prepareContextLogin(comm, context);
		var params = comm.getPostParameters();
		for(var kv:params.entrySet())
			context.put(kv.getKey(), kv.getValue());
		if(params.get("page").equals("1") && 
				params.getOrDefault("shippingAddressRequired", null) != null) {
			t = ve.getTemplate("templates/ShippingForm.vm");
		} else {
			t = ve.getTemplate("templates/ConfirmOrder.vm");
		}
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/order/viewOrder")
	public void ViewOrder(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		var params = comm.getPostParameters();
		doViewOrder(comm, params.get("orderId"), false);
	}
	
	private void doViewOrder(Communication comm, String orderId, boolean destroyCart) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		VelocityContext context = new VelocityContext();
		var ve = getVelocityEngine();
		prepareContextLogin(comm, context);
		var ans = comm.asyncCallRegistry("cart", "GetOrder", (x)->{}, "orderId", orderId).get();
		var data = new Gson().fromJson(Util.inputStreamToString(ans.getEntity().getContent()), Map.class);
		ans.close();
		Template t = ve.getTemplate("templates/ViewOrder.vm");
		context.put("order", data);
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			if(destroyCart) {
				comm.respond(200, sw.toString().getBytes(),
						"Set-Cookie", "cart=; Path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
			} else {
				comm.respond(200, sw.toString().getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/order/listOrders")
	public void ListOrders(Communication comm) throws JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, IOException {
		VelocityContext context = new VelocityContext();
		var ve = getVelocityEngine();
		var account = prepareContextLogin(comm, context);
		String userId = account.get("username");
		var ans = comm.asyncCallRegistry("cart", "GetOrders", (x)->{}, "userId", userId).get();
		var data = new Gson().fromJson(Util.inputStreamToString(ans.getEntity().getContent()), ArrayList.class);
		ans.close();
		context.put("orderList", data);
		Template t = ve.getTemplate("templates/ListOrders.vm");
		StringWriter sw = new StringWriter();
		t.merge(context, sw);
		try {
			comm.respond(200, sw.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
