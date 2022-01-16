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

@TaskDef(name="web", filePath = Util.webuibaseDir)
public class WebTask extends TaskDefinition {
	@EntryDef("/login/")
	public void Login(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		String out = "<html><body> <h1>Login page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		int k = 0 * Util.slowdown(200_000);
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200+k, out.getBytes());
	}

	@EntryDef("/category/")
	public static void Category(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		int category = Integer.parseInt(params.get("category"));
		int page = Integer.parseInt(params.get("page"));
		int prodPerPage = 20;
		
		String out = "<html><body> <h1>Category page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String categoryinfo = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "Categories", x->{}, "id",category+""));
		out += "\n<p>" + categoryinfo + "</p>";
		
		int nproducts = Integer.parseInt(Util.getAndClose(
				comm.asyncCallRegistry("persistence", "ProductsCount", x->{}, "category",category+"")));
		
		int k = 0 * Util.slowdown(200_000);
		
		int from = (page-1)*prodPerPage+k;
		if(from>nproducts-prodPerPage)
			from = nproducts-prodPerPage;
		
		String products = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "ProductsList", x->{}, "category",category+"",
						"idxFrom", from+"", "count", prodPerPage+""));
		out += "\n<p>" + products + "</p>";
		
		var gson = new Gson();
		
		JsonArray jproducts = gson.fromJson(products, JsonArray.class);
		JsonObject jArrayProdIds = new JsonObject();
		for(int i=0; i<jproducts.size(); i++) {
			int pidx = jproducts.get(i).getAsJsonObject().get("ID").getAsInt();
			JsonObject jo = new JsonObject();
			JsonArray sz = new JsonArray(); sz.add(600); sz.add(600);
			jo.add(pidx+"", sz);
			jArrayProdIds.add(pidx+"", sz);
		}
		String prodimages = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200, out.getBytes());
	}
	
	@EntryDef("/product/")
	public void Product(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		int id = Integer.parseInt(params.get("id"));
		
		String out = "<html><body> <h1>Product page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String productinfo = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "Products", x->{}, "id",id+""));
		out += "\n<p>" + productinfo + "</p>";
		
		int k = 0 * Util.slowdown(200_000);
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var jcart = jcookie.get("cart").getAsJsonArray();
		var jproductinfo = gson.fromJson(productinfo, JsonObject.class);
		jproductinfo.addProperty("qty", 1);
		jcart.add(jproductinfo);
		
		var recommend = Util.getAndClose(comm.asyncCallRegistry("recommender", "Recommend",
				x->{}, "cookie", cookie, "items", gson.toJson(jcart)));
		var jrecommend = gson.fromJson(recommend, JsonArray.class);
		
		for(int i=0; i<jrecommend.size() && i<3; i++) {
			String productinfor = Util.getAndClose(
					comm.asyncCallRegistry("persistence", "Products", x->{}, "id",jrecommend.get(i).getAsInt()+""));
			out += "\n<p>" + productinfor + "</p>";
		}
		
		
		JsonObject jArrayProdIds = new JsonObject();
		for(int i=0; i<jrecommend.size(); i++) {
			int pidx = jrecommend.get(i).getAsInt();
			JsonObject jo = new JsonObject();
			JsonArray sz = new JsonArray(); sz.add(200); sz.add(200);
			jo.add(pidx+"", sz);
			jArrayProdIds.add(pidx+"", sz);
		}
		String prodimages = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200+k, out.getBytes());
	}
	
	@EntryDef("/cart/")
	public void Cart(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		String out = "<html><body> <h1>Cart page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var jcart = jcookie.get("cart").getAsJsonArray();
		
		var recommend = Util.getAndClose(comm.asyncCallRegistry("recommender", "Recommend",
				x->{}, "cookie", cookie, "items", gson.toJson(jcart)));
		var jrecommend = gson.fromJson(recommend, JsonArray.class);
		
		for(int i=0; i<jrecommend.size() && i<3; i++) {
			String productinfor = Util.getAndClose(
					comm.asyncCallRegistry("persistence", "Products", x->{}, "id",jrecommend.get(i).getAsInt()+""));
			out += "\n<p>" + productinfor + "</p>";
		}
		
		int k = 0 * Util.slowdown(200_000);
		
		JsonObject jArrayProdIds = new JsonObject();
		for(int i=0+k; i<jrecommend.size(); i++) {
			int pidx = jrecommend.get(i).getAsInt();
			JsonObject jo = new JsonObject();
			JsonArray sz = new JsonArray(); sz.add(200); sz.add(200);
			jo.add(pidx+"", sz);
			jArrayProdIds.add(pidx+"",sz);
		}
		String prodimages = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200, out.getBytes());
	}
	
	@EntryDef("/profile/")
	public void Profile(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		String out = "<html><body> <h1>Profile page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var userid = jcookie.get("userid").getAsString();
		
		var userdata = Util.getAndClose(comm.asyncCallRegistry("persistence", "Users",
				x->{}, "id", userid+""));
		
		out += "\n<p>" + userdata + "</p>";

		int k = 0 * Util.slowdown(200_000);
		
		var ordersdata = Util.getAndClose(comm.asyncCallRegistry("persistence", "OrdersList",
				x->{}, "userId", userid+""));
		
		out += "\n<p>" + ordersdata + "</p>";
		
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200+k, out.getBytes());
	}
	
	@EntryDef("/index/")
	public void Index(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		String out = "<html><body> <h1>Index</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("persistence", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("image", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		int k = 0 * Util.slowdown(200_000);
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("auth", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200+k, out.getBytes());
	}

	
	@EntryDef("/loginAction/")
	public void LoginAction(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		String logout = params.get("logout");
		String username = params.get("username");
		String password = params.get("password");
		
		
		if(logout != null) {
			String ansCookie = Util.getAndClose(comm.asyncCallRegistry("auth", "Logout", x->{}, 
					"cookie",cookie));
			int k = 0 * Util.slowdown(200_000);
			comm.respond(200+k, ansCookie.getBytes());
		} else {
			String ansCookie = Util.getAndClose(comm.asyncCallRegistry("auth", "Login", x->{}, 
					"name",username, "password", password));
			int k = 0 * Util.slowdown(200_000);
			comm.respond(200+k, ansCookie.getBytes());
		}
	}

	@EntryDef("/cartActionAdd/")
	public void CartActionAdd(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		String productid = params.get("productid");
		
		int k = 0 * Util.slowdown(200_000);
		
		String ansCookie = Util.getAndClose(comm.asyncCallRegistry("auth", "AddProductToCart", x->{}, 
				"cookie",cookie, "productid", productid));
		comm.respond(200+k, ansCookie.getBytes());
	}
}
