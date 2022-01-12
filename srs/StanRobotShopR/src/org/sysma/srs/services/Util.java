package org.sysma.srs.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.velocity.app.VelocityEngine;

public class Util {
	//public static final String basePath = "/Users/giulio/SynologyDrive/robotshop/robotshop_base/";
	public static final String basePath = "robotshop_base/";
	public static final String dbBasePath = basePath+"db/";
	
	
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void busyWait(int cycles) {
		int z = 1;
		for(int i=0; i<cycles; i++)
			z = (z+i+1)%Math.max(z*i+1, 1);
	}
	
	public static VelocityEngine getEngine(String path) {
		VelocityEngine ve = new VelocityEngine();
	    ve.setProperty("file.resource.loader.path", path);
	    ve.init();
	    return ve;
	}
	
	public static <T> T onNull(T x, T onNull) {
		return x == null ? onNull : x;
	}
	
	public static String doRedirect(String url) {
		return "<html>\n"
				+ "<head><meta http-equiv=\"refresh\" content=1;url=\""+url+"\"></head>\n"
				+ "<body><a href=\""+url+"\">Continue...</a></body>\n"
				+ "</html>;";
	}
}
