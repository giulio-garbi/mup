package org.sysma.srs.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.Queries;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.Queries.ReadQuery;
import org.sysma.schedulerExecutor.Queries.WriteQuery;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name = "user")
public class UserTask extends TaskDefinition{



	private final static String dbPath = Util.dbBasePath+"cat_user.db";
	private final static String dbOrderPath = Util.dbBasePath+"orders.db";
	
	private final static ReadQuery getUserQuery = Queries.registerRead(dbPath, "GetUser", 
			"SELECT name, password, email "
			+ "FROM Users "
			+ "WHERE name = ? AND password = ?");
	
	private final static ReadQuery getHistQuery = Queries.registerRead(dbOrderPath, "GetHist", 
			"SELECT ID, Cart "
			+ "FROM Orders "
			+ "WHERE User = ?");
	
	private final static WriteQuery newUserQuery = Queries.registerWrite(dbPath, "NewUser", 
			"INSERT INTO Users(name, password, email) "
			+ "VALUES (?,?,?)");
	
	@EntryDef("/api/user/uniqueid")
	public void UniqueId(Communication comm) throws IOException {
		long idx = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
		comm.respond(200, ("{\"uuid\": \"anonymous-"+idx+"\"}").getBytes(), "Content-Type","application/json");
	}

	@EntryDef("/api/user/login")
	public void Login(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var password = params.get("password");
		String[] ans = {"invalid credentials"};
		
		comm.readQuery(getUserQuery, (ps)->{
			try {
				ps.setString(1, user);
				ps.setString(2, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				if(rs.next()) {
					JsonObject usr = new JsonObject();
					usr.addProperty("name", rs.getString("name"));
					usr.addProperty("password", rs.getString("password"));
					usr.addProperty("email", rs.getString("email"));
					ans[0] = new Gson().toJson(usr);
				} else {
					ans[0] = "invalid credentials";
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		if(ans[0].startsWith("{"))
			comm.respond(200, ans[0].getBytes(), "Content-Type","application/json");
		else
			comm.respond(403, ans[0].getBytes());
	}
	
	@EntryDef("/api/user/register")
	public void Register(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var password = params.get("password");
		var email = params.get("email");
		
		try {
			comm.writeQuery(newUserQuery, (ps)->{
				try {
					ps.setString(1, user);
					ps.setString(2, password);
					ps.setString(3, email);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			JsonObject usr = new JsonObject();
			usr.addProperty("name", user);
			usr.addProperty("password", password);
			usr.addProperty("email", email);
			comm.respond(200, new Gson().toJson(usr).getBytes(), "Content-Type","application/json");
		} catch(SQLException ex) {
			comm.respond(403,"user already registered".getBytes());
		}
	}
	


	@EntryDef("/api/user/history")
	public void History(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		
		JsonObject ans = new JsonObject();
		JsonArray hist = new JsonArray();
		ans.addProperty("name", user);
		ans.add("history", hist);
		
		var gson = new Gson();
		
		comm.readQuery(getHistQuery, (ps)->{
			try {
				ps.setString(1, user);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				while(rs.next()) {
					var orderid = rs.getString("ID");
					JsonObject order = new JsonObject();
					JsonObject cart = gson.fromJson(rs.getString("Cart"), JsonObject.class);
					order.addProperty("orderid", orderid);
					order.add("cart", cart);
					hist.add(order);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, gson.toJson(ans).getBytes(), "Content-Type","application/json");
	}
}
