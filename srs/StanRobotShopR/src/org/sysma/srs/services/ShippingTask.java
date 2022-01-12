package org.sysma.srs.services;

import java.io.IOException;
import java.sql.SQLException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.Queries;
import org.sysma.schedulerExecutor.Queries.ReadQuery;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name = "shipping")
public class ShippingTask extends TaskDefinition{

	private final static String dbPath = Util.dbBasePath+"shipping.db";
	private final static ReadQuery getCodesQuery = Queries.registerRead(dbPath, 
			"GetCodes", "SELECT uuid, code, name FROM codes");
	private final static ReadQuery getLocationQuery = Queries.registerRead(dbPath, 
			"GetLocation", 
			"SELECT uuid, country_code, city, name, region, latitude, longitude "
			+ "FROM cities "
			+ "WHERE country_code = ? AND name LIKE ?");
	private final static ReadQuery getLocationLatLon = Queries.registerRead(dbPath, 
			"GetLocationLatLon", 
			"SELECT latitude, longitude "
			+ "FROM cities "
			+ "WHERE uuid = ?");
	
	@EntryDef("/api/shipping/codes")
	public void GetCodes(Communication comm) throws IOException, SQLException {
		JsonArray codes = new JsonArray();
		comm.readQuery(getCodesQuery, (ps)->{}, 
		(rs)->{
			try {
				while(rs.next()) {
					JsonObject code = new JsonObject();
					code.addProperty("uuid", rs.getInt("uuid"));
					code.addProperty("code", rs.getString("code"));
					code.addProperty("name", rs.getString("name"));
					codes.add(code);
				} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(codes).getBytes(), "Content-Type","application/json");
	}
	
	
	@EntryDef("/api/shipping/match")
	public void SearchLocation(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var code = params.get("code");
		var query = params.get("query");
		
		JsonArray locations = new JsonArray();
		comm.readQuery(getLocationQuery, (ps)->{
			try {
				ps.setString(1, code);
				ps.setString(2, query+"%");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 
		(rs)->{
			try {
				while(rs.next()) {
					JsonObject location = new JsonObject();
					location.addProperty("uuid", rs.getInt("uuid"));
					location.addProperty("code", rs.getString("country_code"));
					location.addProperty("city", rs.getString("city"));
					location.addProperty("name", rs.getString("name"));
					location.addProperty("region", rs.getString("region"));
					location.addProperty("latitude", rs.getString("latitude"));
					location.addProperty("longitude", rs.getString("longitude"));
					locations.add(location);
				} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(locations).getBytes(), "Content-Type","application/json");
	}
	
	private double getDistance(double targetLatitude, double targetLongitude) {
        double earthRadius = 6371e3; // meters

		double homeLatitude = 51.164896;
        double homeLongitude = 7.068792;

        // convert to radians
        double latitudeR = Math.toRadians(homeLatitude);
        double targetLatitudeR = Math.toRadians(targetLatitude);
        // difference in Radians
        double diffLatR = Math.toRadians(targetLatitude - homeLatitude);
        double diffLongR = Math.toRadians(targetLongitude - homeLongitude);

        double a = Math.sin(diffLatR / 2.0) * Math.sin(diffLatR / 2.0)
            + Math.cos(latitudeR) * Math.cos(targetLatitudeR)
            * Math.sin(diffLongR / 2.0) * Math.sin(diffLongR);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return (long)Math.rint(earthRadius * c / 1000.0);
    }
	
	@EntryDef("/api/shipping/calc")
	public void CalcShipping(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var city_uuid = Integer.parseInt(params.get("city_uuid"));
		
		JsonObject[] ans = {new JsonObject()};
		comm.readQuery(getLocationLatLon, (ps)->{
			try {
				ps.setInt(1, city_uuid);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 
		(rs)->{
			try {
				if(rs.next()) {
					double lat = rs.getDouble("latitude");
					double lon = rs.getDouble("longitude");
					double dist = getDistance(lat, lon);
					double cost = dist * 5 / 1000;
					ans[0].addProperty("distance", dist);
					ans[0].addProperty("cost", cost);
				} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(), "Content-Type","application/json");
	}
	
	/*
	private final static ReadQuery getRatingQuery = Queries.registerRead(dbPath, 
			"GetRating", "SELECT avg_rating, rating_count FROM ratings WHERE sku = ?");
	
	private final static WriteQuery rateQuery = Queries.registerWrite(dbPath, "Rate", 
		"INSERT INTO ratings (sku, avg_rating, rating_count) "
		+ "VALUES(?, ?, 1) "
		+ "ON CONFLICT(sku) "
		+ "DO UPDATE SET avg_rating = (avg_rating * rating_count + ?)/(rating_count + 1), "
		+ "rating_count = rating_count + 1");
	
	@EntryDef("/api/ratings/api/fetch")
	public void GetRating(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		JsonObject[] ans = {new JsonObject()};
		comm.readQuery(getRatingQuery, (ps)->{
			try {
				ps.setString(1, prod);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				if(rs.next()) {
					ans[0].addProperty("avg_rating", rs.getDouble("avg_rating"));
					ans[0].addProperty("rating_count", rs.getInt("rating_count"));
				} else {
					ans[0].addProperty("avg_rating", 0.0);
					ans[0].addProperty("rating_count", 0);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(ans[0]).getBytes());
	}
	
	@EntryDef("/api/ratings/api/rate")
	public void Rate(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		var vote = Double.parseDouble(params.get("vote"));
		
		JsonObject ans = new JsonObject();
		comm.writeQuery(rateQuery, (ps)->{
			try {
				ps.setString(1, prod);
				ps.setDouble(2, vote);
				ps.setDouble(3, vote);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		ans.addProperty("success", true);
		comm.respond(200, new Gson().toJson(ans).getBytes());
	}
	*/
}
