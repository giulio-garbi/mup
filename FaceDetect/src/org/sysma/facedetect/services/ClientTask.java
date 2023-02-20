package org.sysma.facedetect.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.MainTaskDefinition;

public class ClientTask extends MainTaskDefinition<String[]> {
	@Override
	public void main(Communication comm, String[] arg) throws InterruptedException {
		int imgId = ThreadLocalRandom.current().nextInt(3455);
		try {
			byte[] img;
			var resp1 = comm.asyncCallRegistry("frontend", "GetVal", x->{}, "imgfile", "img_"+imgId+".jpg", "storage-extraload", "0").get();
			img = resp1.getEntity().getContent().readAllBytes();
			resp1.close();
			Thread.sleep(7500);
			comm.asyncCallRegistryWithFile("frontend", "UploadFile", x->{}, 
					"imgfile", "img_"+imgId+".jpg", img, "lb-weights", String.join(", ", arg), "storage-extraload", "0").get().close();
		} catch (InterruptedException | ExecutionException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.sleep(1050);
	}
}
