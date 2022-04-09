package org.sysma.lqnexecutor.model.converters;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sysma.lqnexecutor.model.TimeDistribution;
import org.sysma.lqnexecutor.model.TimeDistribution.Deterministic;
import org.sysma.lqnexecutor.model.TimeDistribution.Exponential;
import org.sysma.lqnexecutor.model.TimeDistribution.PositiveNormal;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class TimeDistributionConverter extends AbstractSingleValueConverter {

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
		return type == TimeDistribution.class;
	}
	
	private Deterministic tryDeterministic(String str) {
		try {
			return new Deterministic((Duration)DurationConverter.convertFromString(str));
		} catch(Exception e) {
			return null;
		}
	}
	
	private Exponential tryExponential(String str) {
		Pattern r = Pattern.compile("^\\s*exp\\((?<mean>.+)\\)\\s*$");
		Matcher m = r.matcher(str);
		if(m.find()) {
			try {
				return new Exponential((Duration)DurationConverter.convertFromString(m.group("mean")));
			} catch(Exception e) {
				return null;
			}
		}
		return null;
	}
	
	private PositiveNormal tryPosNormal(String str) {
		Pattern r = Pattern.compile("^\\s*N\\((?<mean>.+),(?<std>.+)\\)\\s*$");
		Matcher m = r.matcher(str);
		if(m.find()) {
			try {
				return new PositiveNormal(
						(Duration)DurationConverter.convertFromString(m.group("mean")),
						(Duration)DurationConverter.convertFromString(m.group("std")));
			} catch(Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Object fromString(String str) {
		var det = tryDeterministic(str);
		if(det != null)
			return det;
		var exp = tryExponential(str);
		if(exp != null)
			return exp;
		var pn = tryPosNormal(str);
		if(pn != null)
			return pn;
		throw new RuntimeException(str+": invalid distribution");
	}

	@Override
	public String toString(Object dist) {
		return convertToString(dist);
	}
	
	
	public static String convertToString(Object dist) {
		if(dist instanceof Deterministic) {
			return DurationConverter.convertToString(((Deterministic)dist).d);
		} else if(dist instanceof Exponential) {
			return "exp("+DurationConverter.convertToString(((Exponential)dist).mean)+")";
		} else if(dist instanceof PositiveNormal) {
			var pn = ((PositiveNormal)dist);
			return "N("+DurationConverter.convertToString(pn.mean)+", "+
					DurationConverter.convertToString(pn.std)+")";
		}
		throw new RuntimeException(dist+": invalid distribution");
	}
}