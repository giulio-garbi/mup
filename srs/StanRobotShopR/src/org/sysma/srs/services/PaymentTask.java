package org.sysma.srs.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.Queries;
import org.sysma.schedulerExecutor.Queries.WriteQuery;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name = "payment")
public class PaymentTask extends TaskDefinition{

	private final static String dbPath = Util.dbBasePath+"orders.db";
	
	private final static WriteQuery saveOrderQuery = Queries.registerWrite(dbPath, 
			"SaveOrder", "INSERT INTO Orders(ID, user, cart) VALUES (?,?,?)");
	
	@EntryDef("/api/payment/pay")
	public void Pay(Communication comm) throws IOException, SQLException {
		var params = comm.getPostParameters();
		var user = params.get("user");
		var cart = params.get("cart");
		var orderId = ThreadLocalRandom.current().nextLong()+"";
		
		comm.writeQuery(saveOrderQuery, (ps)->{
			try {
				ps.setString(1, orderId);
				ps.setString(2, user);
				ps.setString(3, cart);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		comm.respond(200, ("{\"orderid\":\""+orderId+"\"}").getBytes(), "Content-Type","application/json");
	}
}
