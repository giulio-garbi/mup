package org.sysma.lqnexecutor.model.converters;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class DurationConverter extends AbstractSingleValueConverter {

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
		return type == Duration.class;
	}
	
	

	@Override
	public Object fromString(String str) {
		return convertFromString(str);
	}
	
	@Override
	public String toString(Object d) {
		return convertToString((Duration)d);
	}
	
	public static Object convertFromString(String str) {
		Pattern r = Pattern.compile("^\\s*(?<qty>\\d+)\\s*(?<unit>d|h|m|s|ms|ns)\\s*$");
		Matcher m = r.matcher(str);
		if(m.find()) {
			long qty = Long.parseLong(m.group("qty"));
			switch(m.group("unit")) {
			case "d":
				return Duration.ofDays(qty);
			case "h":
				return Duration.ofHours(qty);
			case "m":
				return Duration.ofMinutes(qty);
			case "s":
				return Duration.ofSeconds(qty);
			case "ms":
				return Duration.ofMillis(qty);
			case "ns":
				return Duration.ofNanos(qty);
			}
		} 
		throw new RuntimeException(str+": invalid duration");
	}
	
	public static String convertToString(Duration d) {
		if(d.toNanosPart()%1000000!=0) {
			return d.toNanos()+" ns";
		} else if(d.toMillisPart()!=0) {
			return d.toMillis()+" ms";
		} else if(d.toSecondsPart()!=0) {
			return d.toSeconds()+" s";
		} else if(d.toMinutesPart()!=0) {
			return d.toMinutes()+" m";
		} else if(d.toHoursPart()!=0) {
			return d.toHours()+" h";
		} else {
			return d.toDays()+" d";
		}
	}
}