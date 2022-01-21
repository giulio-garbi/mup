package org.sysma.lqn.xml.vanilla;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.sysma.lqn.xml.Entry;

public class VanillaEntry {
	public final String name;
	public final VanillaActivity[] replyActivities;
	
	public VanillaEntry(String name, VanillaActivity... replyActivities) {
		this.name = name;
		this.replyActivities = replyActivities;
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

	public static VanillaEntry from(Entry e, HashMap<String, List<VanillaActivity>> convertedActs) {
		return new VanillaEntry(e.name, Arrays.stream(e.replyActivities)
			.map(ract->getLast(convertedActs.get(ract.name)))
			.toArray(VanillaActivity[]::new));
	}
	
	private static <X> X getLast(List<X> xs) {
		return xs.get(xs.size()-1);
	}
}
