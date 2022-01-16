package org.sysma.teastoremongo.main;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;
import org.sysma.teastoremongo.services.Util;

public class ClientTask extends MainTaskDefinition<String> {
	

	/*@SuppressWarnings("deprecation")
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
	};*/
	
	/*
	 * http://localhost:9080/rest/api/flights/queryflights
	 */
	
	private void homePage(Communication comm, String cookie) throws IOException, InterruptedException, ExecutionException {
		Util.getAndClose(comm.asyncCallRegistry("web", "Index", (x)->{}, "cookie", cookie));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/images/front.png", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/bootstrap/css/bootstrap.min.css", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/teastore.css", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/images/icon.ico", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/bootstrap/js/jquery.min.js", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/bootstrap/js/bootstrap.min.js", x->{}));
		Util.getAndClose(comm.asyncCallPageRegistry("web", "/resizingscript.js", x->{}));
	}

	@Override
	public void main(Communication comm, String user) throws InterruptedException {
		String cookie = "{}";
		//String cookie = "{userid:"+user.substring(4)+", cart:[]}";
		var rnd = ThreadLocalRandom.current();
		int catChosen1 = (int)(rnd.nextFloat()*5);
		int prodId1 = (int)(rnd.nextFloat()*(500));
		int catChosen2 = (int)(rnd.nextFloat()*5);
		int prodId2 = (int)(rnd.nextFloat()*(500));
		
		Thread.sleep(1500);
		try {
			homePage(comm, cookie);
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Login", (x)->{}, "cookie", cookie));
			Thread.sleep(1500);
			cookie = Util.getAndClose(comm.asyncCallRegistry("web", "LoginAction", (x)->{}, 
					"cookie", cookie, "username", user, "password", user));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Category", (x)->{}, 
					"cookie", cookie, "category", catChosen1+"", "page", 1+""));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Product", (x)->{}, 
					"cookie", cookie, "id", prodId1+""));
			Thread.sleep(1500);
			cookie = Util.getAndClose(comm.asyncCallRegistry("web", "CartActionAdd", (x)->{}, 
					"cookie", cookie, "productid", prodId1+""));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Cart", (x)->{}, 
					"cookie", cookie));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Category", (x)->{}, 
					"cookie", cookie, "category", catChosen2+"", "page", 1+""));
			Thread.sleep(1500);
			cookie = Util.getAndClose(comm.asyncCallRegistry("web", "CartActionAdd", (x)->{}, 
					"cookie", cookie, "productid", prodId2+""));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Cart", (x)->{}, 
					"cookie", cookie));
			Thread.sleep(1500);
			Util.getAndClose(comm.asyncCallRegistry("web", "Profile", (x)->{}, 
					"cookie", cookie));
			Thread.sleep(1500);
			homePage(comm, cookie);
			cookie = Util.getAndClose(comm.asyncCallRegistry("web", "LoginAction", (x)->{}, 
					"cookie", cookie, "logout", "1"));
			
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
