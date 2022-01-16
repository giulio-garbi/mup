package org.sysma.teastoremongo.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public class Util {
	//public static final String baseDir = "/Users/giulio/SynologyDrive/teastore/teastore_data";
	public static final String baseDir = "teastore_data";
	
	
	public static final String webuibaseDir = Util.baseDir+"/webui";
	
	public static String getAndClose(CompletableFuture<CloseableHttpResponse> future) throws InterruptedException, ExecutionException, UnsupportedOperationException, IOException {
		var chr = future.get();
		var ans = inputStreamToString(chr.getEntity().getContent());
		chr.close();
		return ans;
	}
	
	public static String inputStreamToString(InputStream inputStream) {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result;
		try {
			result = bis.read();
			while(result != -1) {
			    buf.write((byte) result);
			    result = bis.read();
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return buf.toString("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int slowdown(long itr) {
		int k = 0;
		for(int i=0; i<0*itr; i++)
			k+=i;
		return k;
	}
}
