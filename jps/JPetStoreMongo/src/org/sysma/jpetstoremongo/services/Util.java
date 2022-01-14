package org.sysma.jpetstoremongo.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.velocity.app.VelocityEngine;

public class Util {
	//public static final String basePath = "/Users/giulio/SynologyDrive/jpetstore/jpetstore_base/";
	public static final String basePath = "jpetstore_base/";
	//public static final String dbPath = basePath+"db.sqlite";
	
	
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
