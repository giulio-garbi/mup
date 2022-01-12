package org.sysma.srs.services;

import java.io.IOException;
import java.sql.SQLException;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.Queries;
import org.sysma.schedulerExecutor.Queries.ReadQuery;
import org.sysma.schedulerExecutor.Queries.WriteQuery;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@TaskDef(name = "ratings")
public class RatingsTask extends TaskDefinition{

	private final static String dbPath = Util.dbBasePath+"ratings.db";
	
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
		comm.respond(200, new Gson().toJson(ans[0]).getBytes(),"Content-Type","application/json");
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
		comm.respond(200, new Gson().toJson(ans).getBytes(),"Content-Type","application/json");
	}
}
