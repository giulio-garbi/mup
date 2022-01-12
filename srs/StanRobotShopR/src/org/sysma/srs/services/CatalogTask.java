package org.sysma.srs.services;

import java.io.IOException;
import java.sql.ResultSet;
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

@TaskDef(name = "catalog")
public class CatalogTask extends TaskDefinition{

	private final static String dbPath = Util.dbBasePath+"cat_user.db";
	
	private final static ReadQuery getProductsQuery = Queries.registerRead(dbPath, "GetProductsQuery", 
			"SELECT Sku, Name, Description, Price, Instock, IsRobot, IsAI "
			+ "FROM Products "
			+ "WHERE (IsRobot <> 0 AND ? <> 0) "
			+ "OR (IsAI <> 0 AND ? <> 0)");
	private final static ReadQuery getProductQuery = Queries.registerRead(dbPath, "GetProductQuery", 
			"SELECT Sku, Name, Description, Price, Instock, IsRobot, IsAI "
			+ "FROM Products "
			+ "WHERE Sku = ?");
	private final static ReadQuery searchProductsQuery = Queries.registerRead(dbPath, "SearchProductsQuery", 
			"SELECT Sku, Name, Description, Price, Instock, IsRobot, IsAI "
			+ "FROM Products "
			+ "WHERE Name LIKE ?");
	
	@EntryDef("/api/catalogue/categories")
	public void Categories(Communication comm) throws IOException {
		comm.respond(200, "[\"Artificial Intelligence\",\"Robot\"]".getBytes(), "Content-Type","application/json");
	}
	
	private static JsonObject makeProduct(ResultSet rs) throws SQLException {
		var prod = new JsonObject();
		prod.addProperty("sku", rs.getString("Sku"));
		prod.addProperty("name", rs.getString("Name"));
		prod.addProperty("description", rs.getString("Description"));
		prod.addProperty("price", rs.getDouble("Price"));
		prod.addProperty("instock", rs.getString("Instock"));
		var cats = new JsonArray();
		if(rs.getInt("IsRobot") != 0)
			cats.add("Robot");
		if(rs.getInt("IsAI") != 0)
			cats.add("Artificial Intelligence");
		prod.add("categories", cats);
		return prod;
	}
	
	@EntryDef("/api/catalogue/products")
	public void GetProducts(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var cat = params.get("cat");
		int isRobot = cat.equals("Robot") ? 1 : 0;
		int isAI = cat.equals("Artificial Intelligence") ? 1 : 0;
		var prods = new JsonArray();
		comm.readQuery(getProductsQuery, (ps)->{
			try {
				ps.setInt(1, isRobot);
				ps.setInt(2, isAI);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				while(rs.next())
					prods.add(makeProduct(rs));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(prods).getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/catalogue/product")
	public void GetProduct(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var prod = params.get("prod");
		JsonObject[] prodAns = {new JsonObject()};
		comm.readQuery(getProductQuery, (ps)->{
			try {
				ps.setString(1, prod);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				if(rs.next())
					prodAns[0] = (makeProduct(rs));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(prodAns[0]).getBytes(),"Content-Type","application/json");
	}
	
	@EntryDef("/api/catalogue/search")
	public void SearchProducts(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var query = params.get("query")+"%";
		var prods = new JsonArray();
		comm.readQuery(searchProductsQuery, (ps)->{
			try {
				ps.setString(1, query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, (rs)->{
			try {
				while(rs.next())
					prods.add(makeProduct(rs));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		comm.respond(200, new Gson().toJson(prods).getBytes(),"Content-Type","application/json");
	}
}
