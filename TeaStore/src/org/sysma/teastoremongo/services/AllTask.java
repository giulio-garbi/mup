package org.sysma.teastoremongo.services;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mongodb.client.MongoClient;

@TaskDef(name="all")
public class AllTask extends TaskDefinition {
	/*@EntryDef("/isLoggedIn/")
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
	*/
	@EntryDef("/authlogin/")
	public String AuthLogin(String name, String password, MongoClient client) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException, SQLException {
		
		var gson = new Gson();
		
		var rsp = UsersByName(name, client);
		
		var user = gson.fromJson(
				rsp,
				JsonObject.class);
		
		Boolean valid = user.has("password") && password.equals(user.get("password").getAsString());
		
		JsonObject ans = new JsonObject();
		if(valid) {
			ans.addProperty("userid", user.get("ID").getAsString());
			ans.add("cart", new JsonArray());
		}
		
		return gson.toJson(ans);
	}
	
	//@EntryDef("/logout/")
	public String Logout(String cookie) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException {
		//var params = comm.getPostParameters();
		//String cookie = params.get("cookie");
		var ans = "{}";
		return ans;
	}
	/*
	@EntryDef("/addProductToCart/")
	public void AddProductToCart(Communication comm) throws IOException, JsonSyntaxException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		int productid = Integer.parseInt(params.get("productid"));
		
		var gson = new Gson();
		
		var product = gson.fromJson(
				Util.getAndClose(comm.asyncCallRegistry("all", "Products", x->{}, "id", productid+"")),
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
	*/
	//@EntryDef("/usersByName/")
	public String UsersByName(String name, MongoClient client) throws IOException, SQLException, InterruptedException, ExecutionException {
		JsonObject[] ans = {null};
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
		return new Gson().toJson(ans[0]);
	}
	/*
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
	
public static String pimgPath = Util.baseDir+"/images/";
	
	@EntryDef("/getProductImages/")
	public void GetProductImages(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		String qry = params.get("qry");
		var gson = new Gson();
		JsonObject jqry = gson.fromJson(qry, JsonObject.class);
		JsonObject jans = new JsonObject();
		
		Set<String> keys = jqry.keySet();
		for(String key:keys) {
			int imgIdx = Integer.parseInt(key)+513;
			String fileData = Files.readString(Path.of(pimgPath+imgIdx));
			JsonArray sz = jqry.get(key).getAsJsonArray();
			
			BufferedImage img = new BufferedImage(768, 768, BufferedImage.TYPE_INT_ARGB);
			BufferedImage dimg = new BufferedImage(sz.get(0).getAsInt(), 
					sz.get(1).getAsInt(), img.getType());  
		    Graphics2D g = dimg.createGraphics();  
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		    g.drawImage(img, 0, 0, sz.get(0).getAsInt(), sz.get(1).getAsInt(), 
		    		0, 0, 768, 768, null);  
		    g.dispose();  
		    jans.addProperty(key, fileData);
		}
		
		comm.respond(200, gson.toJson(jans).toString().getBytes());
	}
	
	@EntryDef("/GetWebImages/")
	public void GetWebImages(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		String qry = params.get("qry");
		var gson = new Gson();
		JsonObject jqry = gson.fromJson(qry, JsonObject.class);
		JsonObject jans = new JsonObject();
		
		Set<String> keys = jqry.keySet();
		for(String key:keys) {
			byte[] fileData = Files.readAllBytes(Path.of(pimgPath+key+".png"));
			JsonArray sz = jqry.get(key).getAsJsonArray();
			
			BufferedImage img = new BufferedImage(768, 768, BufferedImage.TYPE_INT_ARGB);
			BufferedImage dimg = new BufferedImage(sz.get(0).getAsInt(), 
					sz.get(1).getAsInt(), img.getType());  
		    Graphics2D g = dimg.createGraphics();  
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		    g.drawImage(img, 0, 0, sz.get(0).getAsInt(), sz.get(1).getAsInt(), 
		    		0, 0, 768, 768, null);  
		    g.dispose();  
		    var strFD = Base64.encodeBase64String(fileData);
		    jans.addProperty(key, strFD);
		}
		
		comm.respond(200, gson.toJson(jans).toString().getBytes());
	}
	
	@EntryDef("/recommend/")
	public void Recommend(Communication comm) throws IOException {
		var rnd = ThreadLocalRandom.current();
		int prod1 = (int) (rnd.nextFloat()*500);
		int prod2 = (int) (rnd.nextFloat()*500);
		int prod3 = (int) (rnd.nextFloat()*500);
		comm.respond(200, ("["+prod1+","+prod2+","+prod3+"]").toString().getBytes());
	}
	
	@EntryDef("/login/")
	public void Login(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		
		String out = "<html><body> <h1>Login page</h1>";
		
		String categories = Util.getAndClose(
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		int k = 0 * Util.slowdown(200_000);
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
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
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String categoryinfo = Util.getAndClose(
				comm.asyncCallRegistry("all", "Categories", x->{}, "id",category+""));
		out += "\n<p>" + categoryinfo + "</p>";
		
		int nproducts = Integer.parseInt(Util.getAndClose(
				comm.asyncCallRegistry("all", "ProductsCount", x->{}, "category",category+"")));
		
		int k = 0 * Util.slowdown(200_000);
		
		int from = (page-1)*prodPerPage+k;
		if(from>nproducts-prodPerPage)
			from = nproducts-prodPerPage;
		
		String products = Util.getAndClose(
				comm.asyncCallRegistry("all", "ProductsList", x->{}, "category",category+"",
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
				comm.asyncCallRegistry("all", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
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
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String productinfo = Util.getAndClose(
				comm.asyncCallRegistry("all", "Products", x->{}, "id",id+""));
		out += "\n<p>" + productinfo + "</p>";
		
		int k = 0 * Util.slowdown(200_000);
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var jcart = jcookie.get("cart").getAsJsonArray();
		var jproductinfo = gson.fromJson(productinfo, JsonObject.class);
		jproductinfo.addProperty("qty", 1);
		jcart.add(jproductinfo);
		
		var recommend = Util.getAndClose(comm.asyncCallRegistry("all", "Recommend",
				x->{}, "cookie", cookie, "items", gson.toJson(jcart)));
		var jrecommend = gson.fromJson(recommend, JsonArray.class);
		
		for(int i=0; i<jrecommend.size() && i<3; i++) {
			String productinfor = Util.getAndClose(
					comm.asyncCallRegistry("all", "Products", x->{}, "id",jrecommend.get(i).getAsInt()+""));
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
				comm.asyncCallRegistry("all", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
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
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var jcart = jcookie.get("cart").getAsJsonArray();
		
		var recommend = Util.getAndClose(comm.asyncCallRegistry("all", "Recommend",
				x->{}, "cookie", cookie, "items", gson.toJson(jcart)));
		var jrecommend = gson.fromJson(recommend, JsonArray.class);
		
		for(int i=0; i<jrecommend.size() && i<3; i++) {
			String productinfor = Util.getAndClose(
					comm.asyncCallRegistry("all", "Products", x->{}, "id",jrecommend.get(i).getAsInt()+""));
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
				comm.asyncCallRegistry("all", "GetProductImages", x->{}, "qry",gson.toJson(jArrayProdIds)));
		out += "\n<img src=\"" + prodimages + "\"/>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
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
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		
		
		var gson = new Gson();
		var jcookie = gson.fromJson(cookie, JsonObject.class);
		var userid = jcookie.get("userid").getAsString();
		
		var userdata = Util.getAndClose(comm.asyncCallRegistry("all", "Users",
				x->{}, "id", userid+""));
		
		out += "\n<p>" + userdata + "</p>";

		int k = 0 * Util.slowdown(200_000);
		
		var ordersdata = Util.getAndClose(comm.asyncCallRegistry("all", "OrdersList",
				x->{}, "userId", userid+""));
		
		out += "\n<p>" + ordersdata + "</p>";
		
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
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
				comm.asyncCallRegistry("all", "CategoriesList", x->{}, "count","-1", 
						"idxFrom", "-1"));
		out += "\n<p>" + categories + "</p>";
		
		String icon = Util.getAndClose(
				comm.asyncCallRegistry("all", "GetWebImages", x->{}, "qry","{icon: [400, 400]}"));
		out += "\n<img src=\"" + icon + "\"/>";
		
		int k = 0 * Util.slowdown(200_000);
		
		String login = Util.getAndClose(
				comm.asyncCallRegistry("all", "IsLoggedIn", x->{}, "cookie",cookie));
		out += "\n<p>" + login + "</p>";
		
		out += "\n</body></html>";
		
		comm.respond(200+k, out.getBytes());
	}

	*/
	//@EntryDef("/loginAction/")
	public void LoginAction(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException, JsonSyntaxException, SQLException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		String logout = params.get("logout");
		String username = params.get("username");
		String password = params.get("password");
		
		
		if(logout != null) {
			String ansCookie = Logout(cookie);
			int k = 0 * Util.slowdown(200_000);
			comm.respond(200+k, ansCookie.getBytes());
		} else {
			String ansCookie = AuthLogin(username, password, comm.getMongo());
			int k = 0 * Util.slowdown(200_000);
			comm.respond(200+k, ansCookie.getBytes());
		}
	}
	/*
	@EntryDef("/cartActionAdd/")
	public void CartActionAdd(Communication comm) throws IOException, UnsupportedOperationException, InterruptedException, ExecutionException {
		var params = comm.getPostParameters();
		String cookie = params.get("cookie");
		String productid = params.get("productid");
		
		int k = 0 * Util.slowdown(200_000);
		
		String ansCookie = Util.getAndClose(comm.asyncCallRegistry("all", "AddProductToCart", x->{}, 
				"cookie",cookie, "productid", productid));
		comm.respond(200+k, ansCookie.getBytes());
	}*/
}
