package org.sysma.lqn.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Entry {
	public final String name;
	public final Activity[] replyActivities;
	
	public Entry(String name, Activity... replyActivities) {
		this.name = name;
		this.replyActivities = replyActivities;
	}
	
	public Entry(String name2, ArrayList<String> replyActivities2) {
		this(name2, replyActivities2.stream().map(x->new Activity(x,0,0,0,null,false)).collect(Collectors.toList()).toArray(new Activity[0]));
	}

	public String toXml() {
		return "<entry name=\""+name+"\"/>";
	}
	
	public String getRAXml() {
		if(replyActivities.length == 0)
			return "";
		return "<reply-entry name=\""+this.name+"\">\n"
				+ Arrays.stream(replyActivities).map(a->"<reply-activity name=\""+a.name+"\"/>\n").collect(Collectors.joining())//"<reply-activity name=\"parReply\"/>\n"
				+ "</reply-entry>";
	}
	
	/*
	 * <reply-entry name="server">
               <reply-activity name="parReply"/>
               <reply-activity name="seqReply"/>
            </reply-entry>
	 */
}
