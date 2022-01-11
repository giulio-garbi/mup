package org.sysma.lqnxsim.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("precedence")
public class Precedence {
	@XStreamImplicit
	public Pre[] pre = new Pre[0];
	@XStreamImplicit
	public PreAnd[] preAnd = new PreAnd[0];
	@XStreamImplicit
	public PreOr[] preOr = new PreOr[0];

	@XStreamImplicit
	public Post[] post = new Post[0];
	@XStreamImplicit
	public PostAnd[] postAnd = new PostAnd[0];
	@XStreamImplicit
	public PostOr[] postOr = new PostOr[0];
}
