package org.sysma.teastoremongo.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@TaskDef(name="auth")
public class AuthTask extends TaskDefinition {
	@EntryDef("/isLoggedIn/")
	public void IsLoggedIn(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		Boolean valid = false;
		
		try {
			JsonObject jobj = new Gson().fromJson(cookie, JsonObject.class);
			valid = jobj.has("userid");
		} catch (JsonSyntaxException e) {}
		
		comm.respond(200, valid.toString().getBytes());
	}
	
	@EntryDef("/login/")
	public void Login(Communication comm) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String name = params.get("name");
		String password = params.get("password");
		
		var gson = new Gson();
		
		var rsp = Util.getAndClose(comm.asyncCallRegistry("persistence", "UsersByName", z->{}, "name",name));
		
		var user = gson.fromJson(
				rsp,
				JsonObject.class);
		
		Boolean valid = user.has("password") && password.equals(user.get("password").getAsString());
		
		JsonObject ans = new JsonObject();
		if(valid) {
			ans.addProperty("userid", user.get("ID").getAsString());
			ans.add("cart", new JsonArray());
		}
		
		comm.respond(200, gson.toJson(ans).getBytes());
	}

	@EntryDef("/logout/")
	public void Logout(Communication comm) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException {
		//var params = comm.getPostParameters();
		//String cookie = params.get("cookie");
		var ans = "{}";
		comm.respond(200, ans.getBytes());
	}
	
	@EntryDef("/addProductToCart/")
	public void AddProductToCart(Communication comm) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		int productid = Integer.parseInt(params.get("productid"));
		
		var gson = new Gson();
		
		var product = gson.fromJson(
				Util.getAndClose(comm.asyncCallRegistry("persistence", "Products", x->{}, "id", productid+"")),
				JsonObject.class
		);
		
		JsonObject jcookie = new Gson().fromJson(cookie, JsonObject.class);
		
		var cart = jcookie.get("cart").getAsJsonArray();
		var added = false;
		
		for(int i=0; i<cart.size(); i++) {
			if(cart.get(i).getAsJsonObject().get("ID").getAsInt() == productid) {
				var pri = cart.get(i).getAsJsonObject();
				pri.addProperty("qty", pri.get("qty").getAsInt() + 1);
				added = true;
			}
		}
		
		if(!added) {
			product.addProperty("qty", 1);
			cart.add(product);
		}
		
		comm.respond(200, gson.toJson(jcookie).toString().getBytes());
	}
}
