package org.sysma.lqnxsim.exec;

import java.time.Duration;
import java.util.Random;

import org.sysma.lqnxsim.model.Activity;

public class ActivityUtil {
	private static long sec2nano(double x) {
		return (long)(x * 1000_000_000);
	}
	private static <T> T onNull(T x, T onNull) {
		if(x == null)
			return onNull;
		else
			return x;
	}
	private static double nextExp(Random rand) {
		return -Math.log(1-rand.nextDouble());
	}
	public static Duration getActivityDuration(Random rand, Activity act) {
		long nanos = 0;
		long meanNanos = sec2nano(onNull(act.hostDemandMean, 0.0));
		if(act.hostDemandCvsq == null || act.hostDemandCvsq == 1) {
			nanos = (long)(nextExp(rand) * meanNanos);
		} else if (act.hostDemandCvsq <= 0) {
			nanos = meanNanos;
		} else {
			var cvsq = act.hostDemandCvsq;
			var cv = Math.sqrt(cvsq);
			nanos = (long) (Math.max(0, 1.0+rand.nextGaussian()*(cv)) * meanNanos);
		}
		return Duration.ofNanos(nanos);
	}
}
