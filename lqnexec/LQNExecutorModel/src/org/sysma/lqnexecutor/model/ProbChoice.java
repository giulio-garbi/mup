package org.sysma.lqnexecutor.model;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("prob_choice")
public class ProbChoice implements Instruction {
	
	@XStreamAsAttribute
	private String name;
	
	@XStreamAsAttribute
	private double prob;

	@XStreamAsAttribute
	private Instruction[] then;
	
	@XStreamAsAttribute
	@XStreamAlias("else")
	private Instruction[] else_;

	@Override
	public String getName() {
		if(name == null)
			return String.format("probChoice(%f)",prob);
		else
			return name;
	}
	
	public Supplier<Boolean> getProb() {
		return ()->( ThreadLocalRandom.current().nextDouble() < prob);
	}

	public Instruction[] getThen() {
		return then;
	}

	public Instruction[] getElse_() {
		return else_;
	}

	public void setProb(float prob) {
		this.prob = prob;
	}

	public void setThen(Instruction[] then) {
		this.then = then;
	}

	public void setElse(Instruction[] else_) {
		this.else_ = else_;
	}
	
}
