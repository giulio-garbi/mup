package org.sysma.teastoremongo.main;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;
import org.sysma.teastoremongo.services.Util;

public class ClientLoginTask extends MainTaskDefinition<String> {
	@Override
	public void main(Communication comm, String user) throws InterruptedException {
		String cookie = "{}";
		try {
			Thread.sleep(1000);
			cookie = Util.getAndClose(comm.asyncCallRegistry("all", "LoginAction", (x)->{}, 
					"cookie", cookie, "username", user, "password", user));
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
