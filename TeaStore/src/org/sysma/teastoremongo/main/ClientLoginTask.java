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
		
		Thread.sleep(1500);
		try {
			Util.getAndClose(comm.asyncCallRegistry("all", "Login", (x)->{}, "cookie", cookie));
			Thread.sleep(1500);
			
		} catch (IOException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
