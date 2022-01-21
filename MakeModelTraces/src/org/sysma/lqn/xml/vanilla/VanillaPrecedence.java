package org.sysma.lqn.xml.vanilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.sysma.lqn.xml.Precedence;

public class VanillaPrecedence {
	
	public  String preKind;
	public  String[] pre;
	public  String postKind;
	public  String[] post;
	public  float[] postOrProb;
	
	public VanillaPrecedence() {}
	
	public VanillaPrecedence preOr(VanillaActivity... acts) {
		preKind = "pre-OR";
		pre = new String[acts.length];
		for(int i=0; i<acts.length; i++)
			pre[i] = acts[i].name;
		return this;
	}
	
	public VanillaPrecedence preAnd(VanillaActivity... acts) {
		preKind = "pre-AND";
		pre = new String[acts.length];
		for(int i=0; i<acts.length; i++)
			pre[i] = acts[i].name;
		return this;
	}
	
	public VanillaPrecedence pre(VanillaActivity act) {
		preKind = "pre";
		pre = new String[] {act.name};
		return this;
	}
	
	public VanillaPrecedence pre() {
		preKind = null;
		pre = null;
		return this;
	}
	
	public VanillaPrecedence postOr(VanillaActivity[] acts, float[] probs) {
		postKind = "post-OR";
		post = new String[acts.length];
		postOrProb = new float[acts.length];
		for(int i=0; i<acts.length; i++) {
			post[i] = acts[i].name;
			postOrProb[i] = probs[i];
		}
		return this;
	}
	
	public VanillaPrecedence postAnd(VanillaActivity... acts) {
		postKind = "post-AND";
		post = new String[acts.length];
		postOrProb = null;
		for(int i=0; i<acts.length; i++) {
			post[i] = acts[i].name;
		}
		return this;
	}

	public VanillaPrecedence post(VanillaActivity act) {
		postKind = "post";
		post = new String[] {act.name};
		postOrProb = null;
		return this;
	}
	
	public VanillaPrecedence post() {
		postKind = null;
		post = null;
		postOrProb = null;
		return this;
	}
	
	/*public Precedence(String preKind, String[] pre, String postKind, String[] post, String postLoopEnd) {
		super();
		this.preKind = preKind;
		this.pre = pre;
		this.postKind = postKind;
		this.post = post;
		this.postLoopEnd = postLoopEnd;
		this.preOrProb = -1;
		this.preOrName = null;
	}
	
	public Precedence(String preKind, String[] pre, String postKind, String[] post, String preOrName, float preOrProb) {
		super();
		this.preKind = preKind;
		this.pre = pre;
		this.postKind = postKind;
		this.post = post;
		this.postLoopEnd = null;
		this.preOrProb = preOrProb;
		this.preOrName = preOrName;
	}*/
	
	public String toXml() {
		return "<precedence>\n"
				+ getPre()
				+ getPost()
				+ "</precedence>";
	}

	private String getPost() {
		if(post == null) {
			return "";
		} else if(postKind.equals("post-OR")) {
			return "<"+postKind+">\n"
					+ IntStream.range(0, post.length).boxed().map((i)->"<activity name=\""+post[i]+"\" "
							+ "prob=\""+postOrProb[i]+"\" />\n").collect(Collectors.joining())
					+ "</"+postKind+">";
		} else {
			return "<"+postKind+">\n"
					+ Arrays.stream(post).map((n)->"<activity name=\""+n+"\"/>\n").collect(Collectors.joining())
					+ "</"+postKind+">";
		}
	}

	private String getPre() {
		if(pre == null)
			return "";
		return "<"+preKind+">\n"
				+ Arrays.stream(pre).map((n)->"<activity name=\""+n+"\"/>\n").collect(Collectors.joining())
				+ "</"+preKind+">";
	}
	
	public static VanillaPrecedence from(Precedence p, HashMap<String, List<VanillaActivity>> convertedActs) {
		VanillaPrecedence vp = new VanillaPrecedence();
		vp.preKind = p.preKind;
		vp.postKind = p.postKind;
		vp.postOrProb = p.postOrProb;
		
		if(p.pre == null) {
			vp.pre = null;
		} else {
			vp.pre = Arrays.stream(p.pre)
					.map(preActName->getLast(convertedActs.get(preActName)).name)
					.toArray(String[]::new);
		}
		
		if(p.post == null) {
			vp.post = null;
		} else {
			vp.post = Arrays.stream(p.post)
					.map(postActName->convertedActs.get(postActName).get(0).name)
					.toArray(String[]::new);
		}
		return vp;
	}
	
	public static List<VanillaPrecedence> internalPrecedences(List<VanillaActivity> acts) {
		ArrayList<VanillaPrecedence> iprecs = new ArrayList<>();
		for(int i=0; i<acts.size()-1; i++) {
			VanillaPrecedence ipre = new VanillaPrecedence();
			ipre.pre(acts.get(i));
			ipre.post(acts.get(i+1));
			iprecs.add(ipre);
		}
		return iprecs;
	}
	
	private static <X> X getLast(List<X> xs) {
		return xs.get(xs.size()-1);
	}
}
