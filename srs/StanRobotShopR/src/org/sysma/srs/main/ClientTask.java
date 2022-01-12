package org.sysma.srs.main;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;
import org.sysma.srs.services.Util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientTask extends MainTaskDefinition<String[]> {
	

	@SuppressWarnings("deprecation")
	private JsonParser jsonParser = new JsonParser();
	
	private static String[][] homePage = {
		{"index.html"},
		{"css/style.css", "js/acmeair-common.js", "images/AcmeAir.png",
			"images/acmeAirplane.png"},
		{"images/CloudBack2X.jpg"}
	};
	
	private static String[][] flightsPage = {
		{"flights.html"}
	};
	
	private static String[][] checkinPage = {
		{"checkin.html"}
	};
	
	/*
	 * http://localhost:9080/rest/api/flights/queryflights
	 */
	
	private void homePage(Communication comm) throws IOException, InterruptedException, ExecutionException {
		comm.asyncCallPageRegistry("web", "/", x->{}).get().close();
		comm.asyncCallRegistry("web", "Categories", (x)->{}).get().close();
		comm.asyncCallPageRegistry("web", "/splash.html", x->{}).get().close();
	}

	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		String user = arg[0];
		String password = arg[1];
		var gson = new Gson();
		Thread.sleep(50);
		try {
			homePage(comm);
			var useridConn = comm.asyncCallRegistry("web", "UserUniqueId", (x)->{}).get();
			var useridJson = Util.inputStreamToString(useridConn.getEntity().getContent());
			useridConn.close();
			var userGot = gson.fromJson(useridJson, JsonObject.class).get("uuid").getAsString();

			Thread.sleep(50);
			comm.asyncCallPageRegistry("web", "/dologin.html", x->{}).get().close();
			comm.asyncCallRegistry("web", "Login", (x)->{}, "user", user, "password", password).get().close();
			comm.asyncCallRegistry("web", "CartRename", (x)->{}, "userOld", userGot, "userNew", user).get().close();
			comm.asyncCallRegistry("web", "History", (x)->{}, "user", user).get().close();

			Thread.sleep(50);
			comm.asyncCallRegistry("web", "GetProducts", (x)->{}, "cat", "Artificial Intelligence").get().close();
			comm.asyncCallPageRegistry("web", "/product.html", x->{}).get().close();
			comm.asyncCallRegistry("web", "GetProduct", (x)->{}, "prod", "STAN-1").get().close();
			comm.asyncCallRegistry("web", "GetRating", (x)->{}, "prod", "STAN-1").get().close();
			comm.asyncCallPageRegistry("web", "/images/stan-1.jpg", x->{}).get().close();

			Thread.sleep(50);
			comm.asyncCallRegistry("web", "CartAdd", (x)->{}, "user",user,"prod", "STAN-1","qty","1").get().close();
			comm.asyncCallPageRegistry("web", "/cart.html", x->{}).get().close();
			comm.asyncCallRegistry("web", "GetCart", (x)->{}, "user", user).get().close();

			Thread.sleep(50);
			comm.asyncCallPageRegistry("web", "/shipping.html", x->{}).get().close();
			comm.asyncCallRegistry("web", "GetCodes", (x)->{}).get().close();

			Thread.sleep(50);
			comm.asyncCallRegistry("web", "SearchLocation", (x)->{}, "code","it", "query","rom").get().close();
			Thread.sleep(10);
			comm.asyncCallRegistry("web", "SearchLocation", (x)->{}, "code","it", "query","rome").get().close();
			Thread.sleep(20);
			comm.asyncCallRegistry("web", "CalcShipping", (x)->{}, "city_uuid", "4568310").get().close();
			Thread.sleep(50);
			comm.asyncCallPageRegistry("web", "/payment.html", x->{}).get().close();
			comm.asyncCallRegistry("web", "ConfirmShipping", (x)->{}, "user", user, "dest", "Italy Rome", "cost", "5.92").get().close();

			Thread.sleep(50);
			var cart = "{\"total\":72.92,\"tax\":12.153333333333329,\"items\":[{\"qty\":1,\"sku\":\"STAN-1\",\"name\":\"Stan\",\"price\":67,\"subtotal\":67,\"$$hashKey\":\"object:35\"},{\"qty\":1,\"sku\":\"SHIP\",\"name\":\"Shipping to Italy Rome\",\"price\":5.92,\"subtotal\":5.92,\"$$hashKey\":\"object:36\"}]}";
			comm.asyncCallRegistry("web", "Pay", (x)->{}, "user", user, "cart",cart).get().close();
			
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	private static void callPagesGrouped(Communication comm, String[]... pageGroups) {
		Arrays.stream(pageGroups).forEachOrdered((pg)->{
			ArrayList<CompletableFuture<CloseableHttpResponse>> futures = new ArrayList<>();
			Arrays.stream(pg).forEachOrdered(p->{
				futures.add(comm.asyncCallPage("main", p, x->{}));
			});
			for(int i=0; i<futures.size(); i++) {
				var cf = futures.get(i);
				try {
					var rsp = cf.get();
					//System.out.println(pg[i] + " --- " + rsp.getCode());
					rsp.close();
				} catch (IOException | InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}*/

}
