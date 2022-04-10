package org.sysma.lqnexecutor.model;

import java.time.Duration;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

public abstract class TimeDistribution {
	public abstract Duration next(Random rg);
	
	public static class Deterministic extends TimeDistribution{
		public final Duration d;
		
		public Deterministic(Duration d) {
			this.d = d;
		}

		@Override
		public Duration next(Random rg) {
			return d;
		}
	}
	
	public static class Exponential extends TimeDistribution{
		public final Duration mean;
		
		public Exponential(Duration mean) {
			this.mean = mean;
		}

		@Override
		public Duration next(Random rg) {
			var u = 1.-rg.nextDouble();
			var mult = -Math.log(u);
			return Duration.ofNanos((long)(mean.toNanos() * mult));
		}
	}

	public static class PositiveNormal extends TimeDistribution{
		public final Duration mean;
		public final Duration std;
		NormalDistribution N = new NormalDistribution();
		
		public PositiveNormal(Duration mean, Duration std) {
			this.mean = mean;
			this.std = std;
		}

		@Override
		public Duration next(Random rg) {
			
			long meanNano = this.mean.toNanos();
			long stdNano = this.std.toNanos();
			
			if(stdNano <= 100)
				return mean;
			
			double z0 = -meanNano*1.0/stdNano; //LowLimit
			double cdf0 = N.cumulativeProbability(z0);
			double cdfz = cdf0 + (1.-cdf0) * rg.nextDouble();
			double z = N.inverseCumulativeProbability(cdfz);
			
			long nanoAns = (long)(meanNano + stdNano * z);

			return Duration.ofNanos(nanoAns);
			
			
		}
	}
}
