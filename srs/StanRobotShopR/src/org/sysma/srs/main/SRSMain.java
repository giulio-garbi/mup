package org.sysma.srs.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.srs.services.CartTask;
import org.sysma.srs.services.CatalogTask;
import org.sysma.srs.services.PaymentTask;
import org.sysma.srs.services.RatingsTask;
import org.sysma.srs.services.ShippingTask;
import org.sysma.srs.services.UserTask;
import org.sysma.srs.services.WebTask;

public class SRSMain {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		var nclis = Arrays.stream(args).map(s->Integer.parseInt(s)).toArray(Integer[]::new);
		for(var ncli:nclis) {
			System.out.println("---------------------- "+ncli);
			System.out.println("I&R ");
			
			var register = TaskDirectory.instantiateRegistry(9099);
			register.start();
			
			var ctk = TaskDefinition.instantiate(CatalogTask.class, 9081, 0, 1);
			ctk.start();
			TaskDirectory.register(CatalogTask.class, 9081);
			
			var utk = TaskDefinition.instantiate(UserTask.class, 9082, 0, 1);
			utk.start();
			TaskDirectory.register(UserTask.class, 9082);
			
			var rtk = TaskDefinition.instantiate(RatingsTask.class, 9083, 0, 1);
			rtk.start();
			TaskDirectory.register(RatingsTask.class, 9083);
			
			var cartk = TaskDefinition.instantiate(CartTask.class, 9084, 0, 1);
			cartk.start();
			TaskDirectory.register(CartTask.class, 9084);
			
			var stk = TaskDefinition.instantiate(ShippingTask.class, 9085, 0, 1);
			stk.start();
			TaskDirectory.register(ShippingTask.class, 9085);
			
			var ptk = TaskDefinition.instantiate(PaymentTask.class, 9086, 0, 1);
			ptk.start();
			TaskDirectory.register(PaymentTask.class, 9086);
			
			var wtk = TaskDefinition.instantiate(WebTask.class, 9080, 0, 1);
			wtk.start();
			TaskDirectory.register(WebTask.class, 9080);
			
			ClientTask cli = new ClientTask();
			var tdumps = cli.startRegistry(Duration.ofSeconds(500),ncli, (i)->new String[] {"user"+i, "password"});
			
			String outfn = "example"+ncli+".json";
			var pw = new PrintWriter(outfn);
			
			pw.print("[");
			for(int i=0; i<tdumps.size(); i++) {
				pw.print(tdumps.get(i));
				if(i<tdumps.size()-1) {
					pw.println(",");
				} else {
					pw.println("]");
				}
			}
			pw.close();

			ctk.stop();
			utk.stop();
			rtk.stop();
			cartk.stop();
			stk.stop();
			ptk.stop();
			wtk.stop();
			register.stop();
		}
	}

}
