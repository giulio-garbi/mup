package org.sysma.facedetect.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;

public class ClientTask extends MainTaskDefinition<String[]> {
	private static long nextExp(long avg) {
		return (long)((-Math.log(1-ThreadLocalRandom.current().nextDouble()))*avg);
	}
	
	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		int imgId = 3454;
		try {
			byte[] img;
			var resp1 = comm.asyncCallRegistry("frontend", "GetVal", x->{}, "imgfile", "img_"+imgId+".jpg", "storage-extraload", "0").get();
			img = resp1.getEntity().getContent().readAllBytes();
			resp1.close();
			Thread.sleep(nextExp(Long.parseLong(arg[2]))); //7500
			comm.asyncCallRegistryWithFile("frontend", "UploadFile", x->{}, 
					"imgfile", "img_"+imgId+".jpg", img, "lb-weights", arg[0]+", "+arg[1], "storage-extraload", "0").get().close();
		} catch (InterruptedException | ExecutionException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(nextExp(Long.parseLong(arg[3]))); //1050
	}
}
