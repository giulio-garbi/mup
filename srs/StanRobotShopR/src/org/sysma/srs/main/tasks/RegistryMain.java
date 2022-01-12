package org.sysma.srs.main.tasks;

import java.io.IOException;
import java.net.URI;

import org.sysma.schedulerExecutor.TaskDefinition;
import org.sysma.schedulerExecutor.TaskDirectory;
import org.sysma.srs.services.CartTask;
import org.sysma.srs.services.CatalogTask;
import org.sysma.srs.services.PaymentTask;
import org.sysma.srs.services.RatingsTask;
import org.sysma.srs.services.ShippingTask;
import org.sysma.srs.services.UserTask;
import org.sysma.srs.services.WebTask;

public class RegistryMain {
	public static void main(String[] args) throws IOException {
		if(args[0].equals("registry")) {
			int port = Integer.parseInt(args[1]);
			TaskDirectory.instantiateRegistry(port).start();
			System.out.println("Started registry at port "+port);
		} else {
			String service = args[0];
			int port = Integer.parseInt(args[1]);
			URI registryLocation = URI.create(args[2]);
			int mult = Integer.parseInt(args[3]);
			TaskDirectory.setRegistry(registryLocation);
			Class<? extends TaskDefinition> clazz = null;
			switch(service) {
			case "cart":
				clazz = CartTask.class;
				break;
			case "catalog":
				clazz = CatalogTask.class;
				break;
			case "payment":
				clazz = PaymentTask.class;
				break;
			case "ratings":
				clazz = RatingsTask.class;
				break;
			case "shipping":
				clazz = ShippingTask.class;
				break;
			case "user":
				clazz = UserTask.class;
				break;
			case "web":
				clazz = WebTask.class;
				break;
			}
			TaskDefinition.instantiate(clazz, port, mult).start();
			System.out.println("Started "+service+" at port "+port+" with mult "+mult);
			TaskDirectory.register(clazz, port);
		}
	}
}
