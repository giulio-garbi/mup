package org.sysma.facdedetect.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Util {
	@SuppressWarnings("deprecation")
	public static Map<String, String> parseQuery(String qry){
		qry = java.net.URLDecoder.decode(qry);
		return Arrays.stream(qry.split("&")).map((kv)-> kv.split("=")).collect(Collectors.toMap((kv)->kv[0], (kv)->kv[1]));
	}
	
	public static String parseBodyToQuery(String body) {
		try {
			return new URL("http://example.com:80/?"+body).getQuery();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Map<String, String> parseCookie(List<String> qry){
		return qry.stream().flatMap((l)->Arrays.stream(l.split(";"))).map((kv)-> kv.strip().split("=")).collect(Collectors.toMap((kv)->java.net.URLDecoder.decode(kv[0]), (kv)->java.net.URLDecoder.decode(kv[1])));
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static long getTodayMidnight() {
		var cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.ZONE_OFFSET, 0);
		return cal.getTimeInMillis();
	}

	public static long getDateMidnight(int year, int month, int date) {
		var cal = Calendar.getInstance();
		cal.set(Calendar.DATE, date);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.ZONE_OFFSET, 0);
		return cal.getTimeInMillis();
	}
}
