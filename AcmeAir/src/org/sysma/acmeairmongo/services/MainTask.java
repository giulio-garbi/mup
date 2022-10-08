package org.sysma.acmeairmongo.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.hc.core5.http.HttpHeaders;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sysma.acmeairmongo.auth.LRUCache;
import org.sysma.acmeairmongo.auth.Util;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

@TaskDef(name="main",
		filePath="acmeair_base/"
		)
public class MainTask extends TaskDefinition {
	
	private static int slowdown() {
		long k=1;
		for(long i=0; i<2_000_000L; i++)
			k += i*i;
		return (int)k;
	}
	
	private LRUCache<String, String> fsDatabase = new LRUCache<String, String>(10, ((String ft, Communication comm)->{
		var from = ft.subSequence(0, 3)+"";
		var to = ft.subSequence(3, 6)+"";
		String[] ans = {null};
		
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_flightsegment").find(
			new Document("originPort", from).append("destPort", to)
		);
		try(var cur = custs.cursor()){
			if(cur.hasNext()) {
				ans[0] = cur.next().get("_id")+"";
			}
		}
		return ans[0];
	}));
	

	private LRUCache<String, String> flDatabase = new LRUCache<String, String>(10, ((String idd, Communication comm)->{
		String[] ans = {"{}"};
		String[] parts = idd.split("-");
		var fsid = parts[0];
		var date = Long.parseLong(parts[1]);
		
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_flightsegment");
		var allFlDoc = new ArrayList<Document>();
		
		try(var cur = custs.aggregate(List.of(
			new Document("$match", new Document("_id", new org.bson.types.ObjectId(fsid))),
			new Document("$unwind", "$flights"),
			new Document("$match", new Document("flights.scheduledDepartureTime", date))
		)).cursor()){
			cur.forEachRemaining(d->{allFlDoc.add(d);});
		}
		
		StringBuilder answ = new StringBuilder("[");
		boolean emp = true;
		for(Document d:allFlDoc) {
			Document fl = (Document) d.get("flights");
			if(emp) {
				emp = false;
			} else {
				answ.append(",");
			}
			answ.append("{ _id:\"");
			answ.append(fl.get("_id"));
			answ.append("\", flightSegment: { _id:\"");
			answ.append(d.get("_id"));
			answ.append("\", originPort: \"");
			answ.append(d.get("originPort"));
			answ.append("\", destPort: \"");
			answ.append(d.get("destPort"));
			answ.append("\", miles: ");
			answ.append(d.get("miles"));
			answ.append("}, scheduledDepartureTime: ");
			answ.append(fl.get("scheduledDepartureTime"));
			answ.append(", scheduledArrivalTime: ");
			answ.append(fl.get("scheduledArrivalTime"));
			answ.append(", firstClassBaseCost: ");
			answ.append(fl.get("firstClassBaseCost"));
			answ.append(", economyClassBaseCost: ");
			answ.append(fl.get("economyClassBaseCost"));
			answ.append(", numFirstClassSeats: ");
			answ.append(fl.get("numFirstClassSeats"));
			answ.append(", numEconomyClassSeats: ");
			answ.append(fl.get("numEconomyClassSeats"));
			answ.append(", airplaneTypeId: \"");
			answ.append(fl.get("airplaneTypeId"));
			answ.append("\"}");
		}
		
		answ.append("]");
		ans[0] = answ.toString();
		return ans[0];
	}));
	
	private String checkForValidSessionCookie(Communication comm) {
		try {
			var sessid = Util.parseCookie(comm.getRequestHeaders().getOrDefault("Cookie", List.of())).getOrDefault("sessionid", null);
			if(sessid == null) {
				return null;
			}
			
			var ans = comm.asyncCallRegistry("auth", "createSession", (rq)->{}, "sess", sessid).get();
			var jsonLoginData = Util.inputStreamToString(ans.getEntity().getContent());
			ans.close();
			JSONParser parser = new JSONParser();
			var jld = ((JSONObject)parser.parse(jsonLoginData));
			String custId = (String)jld.get("customerid");
			return custId;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
	
	@EntryDef("/rest/api/loader/load")
	public void DBLoader(Communication comm) {
		try {
			var qry = comm.getPostParameters();
			int ncust = Integer.parseInt(qry.getOrDefault("numCustomers", ""+MAX_CUSTOMERS));
			MongoClient client = comm.getMongo();
			MongoDatabase database = client.getDatabase("aair");
			createCustomers(ncust, database);
			createFlightData(database);
			try {
				comm.respond(200);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (/*SQLException |*/ IOException e1) {
			e1.printStackTrace();
		}
	}
	private long duration(String miles) {
		long mph = 600;
		long msInH = 60*60*1000;
		return Long.parseLong(miles)*msInH/mph;
	}
	static final int MAX_CUSTOMERS = 1000;
	static final int MAX_DAYS_TO_SCHEDULE_FLIGHTS = 7;
	static final int MAX_FLIGHTS_PER_DAY = 2;
	final String[][] MILEAGE_CSV = {{"Mumbai","Delhi","Frankfurt","Hong Kong","London","Montreal","Moscow","New York","Paris","Rome","Singapore","Sydney","Tehran","Tokyo"},
			{"BOM","DEL","FRA","HKG","LHR","YUL","SVO","JFK","CDG","FCO","SIN","SYD","IKA","NRT"},
			{"Amsterdam","AMS","4258","5737","228","8551","230","3422","1330","3639","261","809","6524","13306","2527","9522"},
			{"Aukland","AKL","7662","9406","6883","6883","14202","10968","14622","10730","14452","14061","5230","1343","9338","8275"},
			{"Bangkok","BKK","1871","1815","5575","1065","8038","11618","4396","11401","7810","7011","897","5774","3394","2849"},
			{"Brussels","BRU","4263","5679","190","8493","211","3452","1383","3662","170","734","6551","13249","2525","9631"},
			{"Cairo","CAI","2699","2738","1815","6099","2185","6514","1796","6730","1995","1329","5127","10855","1216","8245"},
			{"Dubai","DXB","1199","1359","3008","3695","3412","6793","2303","6831","3258","2696","3630","7580","759","4828"},
			{"Frankfurt","FRA","4082","5463","NA","8277","400","3640","1253","3851","289","598","6379","13033","2342","9776"},
			{"Geneva","GVA","4173","5391","287","8205","457","3671","1493","3859","250","439","6519","12391","2434","10029"},
			{"Hong Kong","HKG","2673","2345","8277","NA","8252","10345","6063","10279","8493","7694","1607","4586","3843","1788"},
			{"Istanbul","IST","2992","4202","1185","7016","1554","5757","1093","6010","1394","852","5379","11772","1270","9162"},
			{"Karachi","KHI","544","655","3539","3596","5276","8888","2608","9104","3810","3307","2943","8269","1199","5742"},
			{"Kuwait","KWI","1714","1755","2499","4092","2903","6264","1918","6335","2739","2168","2942","8007","1200","5168"},
			{"Lagos","LOS","5140","6015","3018","8930","3098","6734","4806","6508","2922","2497","7428","11898","3659","11076"},
			{"London","LHR","4477","5907","400","8252","NA","3251","1557","3456","209","892","6754","13477","2738","9536"},
			{"Manila","MNL","3189","3656","6394","702","9564","2332","6906","10368","9336","8536","1481","3892","4503","1862"},
			{"Mexico City","MEX","10206","12054","7124","10726","6649","2306","8058","2086","6856","7639","12638","9457","8487","8588"},
			{"Montreal","YUL","7942","3821","3640","10345","3251","NA","5259","330","3433","4100","9193","12045","6223","8199"},
			{"Moscow","SVO","3136","2708","1253","6063","1557","5259","NA","5620","1533","1476","5242","11044","1526","4667"},
			{"Nairobi","NBO","2817","3364","3925","5347","4247","7271","3955","7349","4027","3353","4628","7536","2718","7019"},
			{"New York","JFK","7718","9550","3851","10279","3456","330","5620","NA","3628","4280","9525","12052","6113","8133"},
			{"Paris","CDG","4349","5679","269","8493","209","3433","1533","3628","NA","688","6667","13249","2610","9771"},
			{"Prague","PRG","9334","3549","253","5460","649","3852","1036","4066","542","581","6126","9997","2093","8531"},
			{"Rio de Janeir","GIG","9547","11263","9775","13429","6914","6175","8606","4816","5697","5707","9775","12741","7828","13512"},
			{"Rome","FCO","3840","3679","6238","7694","892","4100","1476","4280","688","NA","6238","12450","2121","9840"},
			{"Singapore","SIN","2432","2578","6379","1605","6754","9193","5252","9525","6667","6238","NA","3912","4110","3294"},
			{"Stockholm","ARN","4842","6057","730","8871","908","4725","760","3917","963","1519","5992","13627","2214","9726"},
			{"Sydney","SYD","6308","7795","13033","4586","13477","12045","11044","12052","13249","12450","3916","NA","8021","6904"},
			{"Tehran","IKA","1743","1582","2342","4712","2738","7465","11537","7681","2610","2121","4110","9693","NA","6858"},
			{"Tokyo","NRT","4184","4959","9776","1788","9536","8199","4667","8133","9771","9840","3294","6904","4770","NA"}};
	
	private void createCustomers(int qty, MongoDatabase mdb) {
		var custs = mdb.getCollection("n_customer");
		custs.deleteMany(Filters.empty());
		List<Document> newcusts = new ArrayList<Document>();
		for(int i=0; i<qty; i++) {
			newcusts.add(new Document()
					.append("_id", "uid"+i+"@email.com")
					.append("password", "password")
					.append("status", "GOLD")
					.append("total_miles", 1000000)
					.append("miles_ytd", 1000)
					.append("streetAddress1", "123 Main St.")
					.append("city", "Anytown")
					.append("stateProvince", "NC")
					.append("country", "USA")
					.append("postalCode", "27617")
					.append("phoneNumber", "919-123-4567")
					.append("phoneNumberType", "BUSINESS")
					.append("bookings", List.of(
						/*new Document()
							.append("fligtId", -1)
							.append("dateOfBooking", -2),
						new Document()
							.append("fligtId", -3)
							.append("dateOfBooking", -4)*/
					))
					.append("sessions",  List.of(
						//new Document()
						//	.append("lastAccessedTime", -1)
						//	.append("timeoutTime", -2)
					))); 
		}
		custs.insertMany(newcusts);
	}
	
	/*WriteQuery deleteAirportsQuery = Queries.registerWrite(dbPath, "DeleteAirports", 
			"DELETE FROM n_airportcodemapping");
	WriteQuery deleteFlightSegsQuery = Queries.registerWrite(dbPath, "DeleteFlightSegs", 
			"DELETE FROM n_flightsegment");
	WriteQuery deleteFlightsQuery = Queries.registerWrite(dbPath, "DeleteFlights", 
			"DELETE FROM n_flight");
	WriteQuery insertAirportQuery = Queries.registerWrite(dbPath, "InsertAirport", 
			"INSERT INTO n_airportcodemapping(id,airportName) VALUES (?, ?)");
	WriteQuery insertFlightSegQuery = Queries.registerWrite(dbPath, "InsertFlightSeg", 
			"INSERT INTO n_flightsegment(id,originPort,destPort,miles) VALUES (?, ?, ?, ?)");
	WriteQuery insertFlightQuery = Queries.registerWrite(dbPath, "InsertFlight", 
			"INSERT INTO n_flight(flightSegmentId,scheduledDepartureTime,id,scheduledArrivalTime, "
					+ "firstClassBaseCost, economyClassBaseCost, numFirstClassSeats, numEconomyClassSeats, airplaneTypeId) "
					+ "VALUES (?, ?, ?, ?, 500, 200, 10, 200, \"B747\")");*/
	
	private void createFlightData(MongoDatabase mdb) {
		var airps = mdb.getCollection("n_airportcodemapping");
		airps.deleteMany(Filters.empty());
		List<Document> newairps = new ArrayList<Document>();
		for(int i=0; i<MILEAGE_CSV[0].length; i++) {
			newairps.add(new Document("_id",MILEAGE_CSV[1][i])
					.append("airportName", MILEAGE_CSV[0][i]));
		}
		airps.insertMany(newairps);
		
		var fsegs = mdb.getCollection("n_flightsegment");
		fsegs.deleteMany(Filters.empty());
		
		long time = Util.getTodayMidnight();
		
		List<Document> newfsegs = new ArrayList<Document>();
		for(int c=0; c<MILEAGE_CSV[0].length; c++) {
			for(int r=2; r<MILEAGE_CSV.length; r++) {
				String orig = MILEAGE_CSV[1][c];
				String dest = MILEAGE_CSV[r][1];
				String miles = MILEAGE_CSV[r][c+2];
				if(miles.equals("NA"))
					continue;
				List<Document> flsOfFseg = new ArrayList<>();
				
				for (var kk = 0; kk < MAX_DAYS_TO_SCHEDULE_FLIGHTS; kk++) {
					long timeSt = time + kk*24*60*60*1000;
					for (var ll = 0; ll < MAX_FLIGHTS_PER_DAY; ll++) {
						long depart = timeSt;
						long arrive = (timeSt + duration(miles));
						flsOfFseg.add(new Document()
								.append("_id", ObjectId.get())
								.append("scheduledDepartureTime", depart)
								.append("scheduledArrivalTime", arrive)
								.append("firstClassBaseCost", 500)
								.append("economyClassBaseCost", 200)
								.append("numFirstClassSeats", 10)
								.append("numEconomyClassSeats", 200)
								.append("airplaneTypeId", "B747")
								);
					}	
				}
				newfsegs.add(new Document("originPort", orig)
						.append("destPort",dest)
						.append("miles", (miles))
						.append("flights", flsOfFseg));
			}
		}
		fsegs.insertMany(newfsegs);
	}
	
	
	
	@EntryDef("/rest/api/loader/query")
	public void NumCustomersQuery(Communication comm) {
		NumCustomers(comm);
	}
	
	@EntryDef("/rest/api/config/countCustomers")
	public void NumCustomers(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_customer");
		var ncust = custs.countDocuments();
		try {
			byte[] ans = (""+ncust).getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/rest/api/config/countBookings")
	public void NumBookings(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_customer");
		Document answ = null;
		try(var cur = custs.aggregate(List.of(
			new Document("$group", 
					new Document().append("_id", null)
					.append("totalSize", new Document("$sum", new Document("$size", "$bookings"))))
		)).cursor()){
			answ = cur.next();
		}
		try {
			byte[] ans = answ.get("totalSize").toString().getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/countFlights")
	public void NumFlights(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_flightsegment");
		Document answ = null;
		try(var cur = custs.aggregate(List.of(
			new Document("$group", 
					new Document().append("_id", null)
					.append("totalSize", new Document("$sum", new Document("$size", "$flights"))))
		)).cursor()){
			answ = cur.next();
		}
		try {
			byte[] ans = answ.get("totalSize").toString().getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/countSessions")
	public void NumSessions(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_customer");
		Document answ = null;
		try(var cur = custs.aggregate(List.of(
			new Document("$group", 
					new Document().append("_id", null)
					.append("totalSize", new Document("$sum", new Document("$size", "$sessions"))))
		)).cursor()){
			answ = cur.next();
		}
		try {
			byte[] ans = answ.get("totalSize").toString().getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/countFlightSegments")
	public void NumFlightSegments(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_flightsegment");
		var ncust = custs.countDocuments();
		try {
			byte[] ans = (""+ncust).getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/countAirports")
	public void NumAirports(Communication comm) {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_airportcodemapping");
		var ncust = custs.countDocuments();
		try {
			byte[] ans = (""+ncust).getBytes();
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/runtime")
	public void Runtime(Communication comm) {
		String ansS = "[{\"name\":\"Runtime\",\"description\":\"Java\"}]";
		
		byte[] ans = ansS.getBytes();
		try {
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/config/dataServices")
	public void DataServices(Communication comm) {
		String ansS = "[{\"name\":\"mongodb\",\"description\":\"MongoDB Database\"}]";
		
		byte[] ans = ansS.getBytes();
		try {
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "application/json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@EntryDef("/rest/api/config/activeDataService")
	public void ActiveDataService(Communication comm) {
		String ansS = "mongodb";
		
		byte[] ans = ansS.getBytes();
		try {
			comm.respond(200, ans, HttpHeaders.CONTENT_TYPE, "plain/text");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/login/logout")
	public void Logout(Communication comm) {
		int z = slowdown();
		try {
			var sessid = Util.parseCookie(comm.getRequestHeaders().getOrDefault("Cookie", List.of())).getOrDefault("sessionid", null);
			if(sessid != null) {
				var ans = comm.asyncCallRegistry("auth", "deleteSession", (rq)->{}, "sess", sessid).get();
				ans.close();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byte[] ans = "logged out".getBytes();
			comm.respond(200+0*z, ans, "Set-Cookie", "sessionid=; Path=/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@EntryDef("/rest/api/login")
	public void Login(Communication comm) {
		boolean[] ok = {false};
		int z = slowdown();
		try {
			var params = comm.getPostParameters();
			var login = params.get("login");
			var password = params.get("password");
			ok[0] = login != null && password != null;
			if(ok[0]) {
				MongoClient client = comm.getMongo();
				MongoDatabase mdb = client.getDatabase("aair");
				var custs = mdb.getCollection("n_customer").find(
					new Document("_id", login).append("password", password)
				);
				try(var cur = custs.cursor()){
					ok[0] = cur.hasNext();
				}
			}
			
			if(ok[0]) {
				var ans = comm.asyncCallRegistry("auth", "createSession", (rq)->{}, "user", login).get();
				var jsonLoginData = Util.inputStreamToString(ans.getEntity().getContent());
				ans.close();
				JSONParser parser = new JSONParser();
				String sessid = ((JSONObject)parser.parse(jsonLoginData)).get("_id").toString();
				
				if(ok[0]) {
					try {
						byte[] ans2 = "logged in".getBytes();
						comm.respond(200+0*z, ans2, HttpHeaders.CONTENT_TYPE, "plain/text",
								"Set-Cookie", "sessionid="+sessid+"; Path=/");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			comm.respond(403+0*z, "Set-Cookie", "sessionid=; Path=/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	@SuppressWarnings("deprecation")
	@EntryDef("/rest/api/flights/queryflights")
	public void QueryFlights(Communication comm) {String sessid = checkForValidSessionCookie(comm);
	int z = slowdown();
		if(sessid == null) {
			try {
				comm.respond(403, "Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		Map<String, String> params = Util.parseQuery(Util.parseBodyToQuery(Util.inputStreamToString(comm.getRequestBody())));
		
		var fromAirport = params.get("fromAirport");
		var toAirport = params.get("toAirport");
		var fromDateWeb = new Date(params.get("fromDate"));
		var fromDate = Util.getDateMidnight(fromDateWeb.getYear()+1900, fromDateWeb.getMonth(), fromDateWeb.getDate()); // convert date to local timezone
		var oneWay = (Boolean.parseBoolean(params.get("oneWay")));
		Long returnDate = null;
		String reversePath = "";
		if(!oneWay) {
			var returnDateWeb = new Date(params.get("returnDate"));
			returnDate = Util.getDateMidnight(returnDateWeb.getYear()+1900, returnDateWeb.getMonth(), returnDateWeb.getDate()); // convert date to local timezone
			reversePath = ",    {\"numPages\":1,\"flightsOptions\": "+getFlights(toAirport, fromAirport, returnDate, comm)+",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}\n";
		}
		String ansS = "{\"tripFlights\":\n"
			+ "[\n"
			+ "    {\"numPages\":1,\"flightsOptions\": "+getFlights(fromAirport, toAirport, fromDate, comm)+",\"currentPage\":0,\"hasMoreOptions\":false,\"pageSize\":10}\n"
			+ reversePath
			+ "], \"tripLegs\":"+(oneWay?1:2)+"}";
		
		try {
			byte[] ans = ansS.getBytes();
			comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getFlights(String from, String to, long time, Communication comm) {
		String fsid = fsDatabase.query(from+to, comm);
		if(fsid == null)
			return "[]";
		return flDatabase.query(fsid+"-"+time, comm);
	}
	
	
	
	@EntryDef("/rest/api/bookings/bookflights")
	public void BookFlights(Communication comm) {
		String sessid = checkForValidSessionCookie(comm);
		int z = slowdown();
		if(sessid == null) {
			try {
				comm.respond(403, "Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			Map<String, String> params = comm.getPostParameters();
			var userid = params.get("userid");
			var toFlight = params.get("toFlightId");
			var retFlight = params.getOrDefault("retFlightId", null);
			var oneWay = Boolean.parseBoolean(params.get("oneWayFlight"));
			var now = System.currentTimeMillis();
			//Connection conn;
			try {
				//conn = getDbConnection();
				String toBookingId = bookFlights(userid, toFlight, now, comm);
				String ansS = null;
				if(oneWay) {
					ansS = "{\"oneWay\":true,\"departBookingId\":\""+toBookingId+"\"}";
				} else {
					String retBookingId = bookFlights(userid, retFlight, now, comm);
					ansS = "{\"oneWay\":false,\"departBookingId\":\""+toBookingId+"\",\"returnBookingId\":\""+retBookingId+"\"}";
				}
				byte[] ans = ansS.getBytes();
		
				try {
					comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			comm.respond(500+0*z);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String bookFlights(String userId, String flightId, long dateOfBooking, Communication comm) throws SQLException {
		MongoClient client = comm.getMongo();
		MongoDatabase mdb = client.getDatabase("aair");
		var custs = mdb.getCollection("n_customer");
		//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
		var oid = ObjectId.get();
		custs.updateMany(new Document("_id", (userId)), 
				new Document("$addToSet", new Document("bookings", 
					new Document()
						.append("_id", oid)
						.append("flightId", new ObjectId(flightId))
						.append("dateOfBooking", dateOfBooking))));
		return oid.toHexString();
		
	}
	
	@EntryDef("/rest/api/bookings/cancelbooking")
	public void CancelBookings(Communication comm) {
		String sessid = checkForValidSessionCookie(comm);
		int z = slowdown();
		if(sessid == null) {
			try {
				comm.respond(403, "Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			Map<String, String> params = comm.getPostParameters();
			
			var number = params.get("number");
			var userid = params.get("userid");
			
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			var oid = new ObjectId(number);
			custs.updateMany(new Document("_id", (userid)), 
					new Document("$pull", new Document("bookings", 
						new Document("_id", oid))));
			
			
			byte[] ans = "{'status':'success'}".getBytes();
			try {
				comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] ans = "{'status':'error'}".getBytes();
		try {
			comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	
	@EntryDef("/rest/api/bookings/byuser/")
	public void BookingsByUser(Communication comm) {
		String sessid = checkForValidSessionCookie(comm);
		int z = slowdown();
		if(sessid == null) {
			try {
				comm.respond(403,"Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		

		try {
			var params = comm.getPostParameters();
			String userid = params.get("user");
			
			var bookings = new ArrayList<Document>();
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			try(var custCur = custs.find(new Document("_id", (userid))).cursor()){
				custCur.forEachRemaining(d->bookings.addAll(d.getList("bookings", Document.class)));
			}
			
			
			StringBuilder ansB = new StringBuilder("[");
			boolean first = true;
			for(var b:bookings) {
				if(first) {
					first = false;
				} else {
					ansB.append(", ");
				}
				ansB.append("{ customerId:\"");
				ansB.append(userid);
				ansB.append("\", _id:\"");
				ansB.append(b.get("_id"));
				ansB.append("\", flightId:\"");
				ansB.append(b.get("flightId"));
				ansB.append("\", dateOfBooking:");
				ansB.append(b.get("dateOfBooking"));
				ansB.append("}");
			}
			ansB.append("]");
			
			byte[] ans = ansB.toString().getBytes();
			try {
				comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			comm.respond(500+0*z, HttpHeaders.CONTENT_TYPE, "application/json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public void getData(Communication comm, String userId) {
		//Connection conn;
		String[] ansS = {null};
		int z = slowdown();
		try {
			var account = new Document();
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			//{"$addToSet":{"UserDetails.$.userGroupMessage":"Hello"}}
			try(var custCur = custs.find(new Document("_id", (userId))).cursor()){
				if(custCur.hasNext())
					account = custCur.next();
			}
			
			
			ansS[0] = "{"
				+ "\"miles_ytd\":\""+account.get("miles_ytd")+"\","
				+ "\"total_miles\":"+account.get("total_miles")+","
				+ "\"status\":\""+account.get("status")+"\","
				+ "\"_id\":\""+userId+"\","
				+ "\"password\":\""+account.get("password")+"\","
				+ "\"phoneNumber\":\""+account.get("phoneNumber")+"\","
				+ "\"phoneNumberType\":\""+account.get("phoneNumberType")+"\","
				+ "\"address\":{"
					+ "\"streetAddress1\":\""+account.get("streetAddress1")+"\","
					+ "\"city\":\""+account.get("city")+"\","
					+ "\"stateProvince\":\""+account.get("stateProvince")+"\","
					+ "\"country\":\""+account.get("country")+"\","
					+ "\"postalCode\":\""+account.get("postalCode")+"\""
			+ "}}";
			try {
				byte[] ans = ansS[0].getBytes();
				comm.respond(200+0*z,  ans, HttpHeaders.CONTENT_TYPE, "application/json");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();	
			try {
				comm.respond(500+0*z);
			} catch (IOException ex) {
				e.printStackTrace();
			}
			
		}
	}
	
	@EntryDef("/rest/api/customer/get/byid/")
	public void GetAccount(Communication comm) {
		String sessid = checkForValidSessionCookie(comm);
		if(sessid == null) {
			try {
				comm.respond(403, "Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		try {
			Map<String, String> params = comm.getPostParameters();
			String userId = params.get("user");
			getData(comm, userId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void setData(Communication comm, JSONObject params, String reqStr) {
		int z = slowdown();
		try {
			var address = (JSONObject) params.get("address");
			
			MongoClient client = comm.getMongo();
			MongoDatabase mdb = client.getDatabase("aair");
			var custs = mdb.getCollection("n_customer");
			custs.updateMany(new Document("_id", ((String)params.get("_id"))),
					new Document("$set",
							new Document()
								.append("password", (String)params.get("password"))
								.append("streetAddress1", (String)address.get("streetAddress1"))
								.append("city", (String)address.get("city"))
								.append("stateProvince", (String)address.get("stateProvince"))
								.append("country", (String)address.get("country"))
								.append("postalCode", (String)address.get("postalCode"))
								.append("phoneNumber", (String)params.get("phoneNumber"))
								.append("phoneNumberType", (String)params.get("phoneNumberType"))));
			
			
			try {
				byte[] ans = reqStr.getBytes();
				comm.respond(200+0*z, ans, HttpHeaders.CONTENT_TYPE, "application/json");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				comm.respond(500+0*z);
			} catch (IOException ex) {
				e.printStackTrace();
			}
		}
	}
	@EntryDef("/rest/api/customer/set/byid/")
	public void SetAccount(Communication comm) {
		// TODO SETUP userId IN OBJECT!!!!!
		String sessid = checkForValidSessionCookie(comm);
		if(sessid == null) {
			try {
				comm.respond(403, "Set-Cookie", "sessionid=; Path=/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		/*var qpr = comm.getPostParameters();
		String userId = qpr.get("user");*/
		
		JSONParser parser = new JSONParser();
		JSONObject params;
		try {
			String reqStr = Util.inputStreamToString(comm.getRequestBody());
			params = ((JSONObject)parser.parse(reqStr));
			setData(comm, params, reqStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
