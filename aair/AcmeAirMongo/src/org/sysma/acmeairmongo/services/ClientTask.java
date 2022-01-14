package org.sysma.acmeairmongo.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.acmeairmongo.auth.Util;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.CompletableFuture;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public class ClientTask extends MainTaskDefinition<String[]> {
	

	@SuppressWarnings("deprecation")
	private JsonParser jsonParser = new JsonParser();
	
	private static String[][] homePage = {
		{"index.html"},
		//{"css/style.css", "js/acmeair-common.js", "images/AcmeAir.png",
		//	"images/acmeAirplane.png"},
		//{"images/CloudBack2X.jpg"}
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

	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		String username = arg[0];
		String password = arg[1];
		Thread.sleep(150);
		callPagesGrouped(comm, homePage);
		Thread.sleep(50);
		int slp = 1000;
		try {
			comm.asyncCallRegistry("main", "Login", (x)->{}, "login", username, "password", password).get().close();
			//System.out.println(".");
			Thread.sleep(slp);
			callPagesGrouped(comm, flightsPage);
			Thread.sleep(slp);
			
			int repeat = ThreadLocalRandom.current().nextInt(3) + 1;
			String flightToBook = null;
			
			for(int i=0; i<repeat; i++) {
				var flsResp = comm.asyncCallRegistry("main", "QueryFlights", (x)->{}, 
						"fromAirport", "JFK", "toAirport", "CDG",
						"fromDate", "Sun Jul 25 2021 00:00:00 GMT+0100 (Ora standard dell’Europa centrale)", 
						"oneWay", "true").get();
				@SuppressWarnings("deprecation")
				JsonObject jo = (JsonObject)jsonParser.parse(Util.inputStreamToString(flsResp.getEntity().getContent()));
				flsResp.close();
				var flights = jo.getAsJsonArray("tripFlights").get(0).getAsJsonObject().getAsJsonArray("flightsOptions");			
				flightToBook = flights.get(0).getAsJsonObject().get("_id").getAsString();
				
				Thread.sleep(slp);
			}
			
			var bookResp = comm.asyncCallRegistry("main", "BookFlights", (x)->{}, 
					"userid", username, "toFlightId", flightToBook+"",
					"oneWayFlight", "true").get();
			@SuppressWarnings("deprecation")
			JsonObject bookingjo = (JsonObject)jsonParser.parse(Util.inputStreamToString(bookResp.getEntity().getContent()));
			bookResp.close();
			var booking = bookingjo.get("departBookingId").getAsString();
			
			Thread.sleep(slp);
			callPagesGrouped(comm, checkinPage);
			comm.asyncCallRegistry("main", "BookingsByUser", (x)->{}, "user", username).get().close();
			Thread.sleep(slp);
			
			comm.asyncCallRegistry("main", "CancelBookings", (x)->{}, "userid", username, "number", booking).get().close();
			comm.asyncCallRegistry("main", "BookingsByUser", (x)->{}, "user", username).get().close();
			Thread.sleep(slp);
			
			
			comm.asyncCallRegistry("main", "Logout", (x)->{}).get().close();
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void callPagesGrouped(Communication comm, String[]... pageGroups) {
		/*try {
			Thread.sleep((int)Math.max(0,ThreadLocalRandom.current().nextGaussian()*200+600));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		Arrays.stream(pageGroups).forEachOrdered((pg)->{
			ArrayList<CompletableFuture<CloseableHttpResponse>> futures = new ArrayList<>();
			Arrays.stream(pg).forEachOrdered(p->{
				futures.add(comm.asyncCallPageRegistry("main", p, x->{}));
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
	}

}
