package org.sysma.lqn.xml;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Precedence {
	
	public  String preKind;
	public  String[] pre;
	public  String postKind;
	public  String[] post;
	public  float[] postOrProb;
	
	public Precedence() {}
	
	public Precedence preOr(Activity... acts) {
		preKind = "pre-OR";
		pre = new String[acts.length];
		for(int i=0; i<acts.length; i++)
			pre[i] = acts[i].name;
		return this;
	}
	
	public Precedence preAnd(Activity... acts) {
		preKind = "pre-AND";
		pre = new String[acts.length];
		for(int i=0; i<acts.length; i++)
			pre[i] = acts[i].name;
		return this;
	}
	
	public Precedence pre(Activity act) {
		preKind = "pre";
		pre = new String[] {act.name};
		return this;
	}
	
	public Precedence pre() {
		preKind = null;
		pre = null;
		return this;
	}
	
	public Precedence postOr(Activity[] acts, float[] probs) {
		postKind = "post-OR";
		post = new String[acts.length];
		postOrProb = new float[acts.length];
		for(int i=0; i<acts.length; i++) {
			post[i] = acts[i].name;
			postOrProb[i] = probs[i];
		}
		return this;
	}
	
	public Precedence postAnd(Activity... acts) {
		postKind = "post-AND";
		post = new String[acts.length];
		postOrProb = null;
		for(int i=0; i<acts.length; i++) {
			post[i] = acts[i].name;
		}
		return this;
	}

	public Precedence post(Activity act) {
		postKind = "post";
		post = new String[] {act.name};
		postOrProb = null;
		return this;
	}
	
	public Precedence post() {
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
}
