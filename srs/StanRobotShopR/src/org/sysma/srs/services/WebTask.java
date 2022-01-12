package org.sysma.srs.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name = "web", filePath = Util.basePath+"web" )
public class WebTask extends TaskDefinition{
	
	@EntryDef("/api/catalogue/categories")
	public void Categories(Communication comm) throws IOException, InterruptedException, ExecutionException {
		comm.forwardCallRegistry("catalog", "Categories", (x)->{});
	}
	@EntryDef("/api/user/uniqueid")
	public void UserUniqueId(Communication comm) throws IOException, InterruptedException, ExecutionException {
		comm.forwardCallRegistry("user", "UniqueId", (x)->{});
	}
	@EntryDef("/api/catalogue/products")
	public void GetProducts(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var cat = params.get("cat");
		comm.forwardCallRegistry("catalog", "GetProducts", (x)->{},
				"cat", cat);
		/*var cats = Util.inputStreamToString(ans.getEntity().getContent());
		ans.close();
		comm.respond(200, cats.getBytes(),"Content-Type","application/json");*/
	}
	@EntryDef("/api/catalogue/product")
	public void GetProduct(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		comm.forwardCallRegistry("catalog", "GetProduct", (x)->{},
				"prod", prod);
	}
	@EntryDef("/api/ratings/api/fetch")
	public void GetRating(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		comm.forwardCallRegistry("ratings", "GetRating", (x)->{},
				"prod", prod);
	}
	@EntryDef("/api/ratings/api/rate")
	public void Rate(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		var vote = params.get("vote");
		comm.forwardCallRegistry("ratings", "Rate", (x)->{},
				"prod", prod, "vote", vote);
	}
	
	@EntryDef("/api/cart/cart")
	public void GetCart(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		comm.forwardCallRegistry("cart", "GetCart", (x)->{},
				"user", user);
	}
	
	@EntryDef("/api/cart/add")
	public void CartAdd(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var prod = params.get("prod");
		var qty = params.get("qty");
		comm.forwardCallRegistry("cart", "CartAdd", (x)->{},
				"user", user, "prod", prod, "qty", qty);
	}
	
	@EntryDef("/api/cart/update")
	public void CartUpdate(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var prod = params.get("prod");
		var qty = params.get("qty");
		comm.forwardCallRegistry("cart", "CartUpdate", (x)->{},
				"user", user, "prod", prod, "qty", qty);
	}
	
	@EntryDef("/api/shipping/codes")
	public void GetCodes(Communication comm) throws IOException, InterruptedException, ExecutionException {
		comm.forwardCallRegistry("shipping", "GetCodes", (x)->{});
	}
	
	@EntryDef("/api/shipping/match")
	public void SearchLocation(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var code = params.get("code");
		var query = params.get("query");
		comm.forwardCallRegistry("shipping", "SearchLocation", (x)->{},
				"code", code, "query", query);
	}
	
	@EntryDef("/api/shipping/calc")
	public void CalcShipping(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var city_uuid = params.get("city_uuid");
		comm.forwardCallRegistry("shipping", "CalcShipping", (x)->{},
				"city_uuid", city_uuid);
	}
	

	
	@EntryDef("/api/shipping/confirm")
	public void ConfirmShipping(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var dest = params.get("dest");
		var cost = params.get("cost");
		comm.forwardCallRegistry("cart", "AddShipping", (x)->{},
				"user", user, "dest", dest, "cost", cost);
	}
	
	@EntryDef("/api/payment/pay")
	public void Pay(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var cart = params.get("cart");
		comm.forwardCallRegistry("payment", "Pay", (x)->{},
				"user", user, "cart", cart);
	}
	
	@EntryDef("/api/user/login")
	public void Login(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var password = params.get("password");
		comm.forwardCallRegistry("user", "Login", (x)->{},
				"user", user, "password", password);
	}
	
	@EntryDef("/api/cart/rename")
	public void CartRename(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var userOld = params.get("userOld");
		var userNew = params.get("userNew");
		comm.forwardCallRegistry("cart", "CartRename", (x)->{},
				"userOld", userOld, "userNew", userNew);
	}
	
	@EntryDef("/api/user/history")
	public void History(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");

		comm.forwardCallRegistry("user", "History", (x)->{},
				"user", user);
	}
	
	@EntryDef("/api/user/register")
	public void Register(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var password = params.get("password");
		var email = params.get("email");
		comm.forwardCallRegistry("user", "Register", (x)->{},
				"user", user, "password", password, "email", email);
	}
	
	@EntryDef("/api/catalogue/search")
	public void SearchProducts(Communication comm) throws IOException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		var query = params.get("query");

		comm.forwardCallRegistry("catalog", "SearchProducts", (x)->{},
				"query", query);
	}
}
