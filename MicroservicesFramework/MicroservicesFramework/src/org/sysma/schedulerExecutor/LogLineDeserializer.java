package org.sysma.schedulerExecutor;

import java.lang.reflect.Type;

import org.sysma.schedulerExecutor.LogLine.Begin;
import org.sysma.schedulerExecutor.LogLine.Call;
import org.sysma.schedulerExecutor.LogLine.CallWithReg;
import org.sysma.schedulerExecutor.LogLine.End;
import org.sysma.schedulerExecutor.LogLine.ForwardCallWithReg;
import org.sysma.schedulerExecutor.LogLine.ForwardReg;
import org.sysma.schedulerExecutor.LogLine.ForwardingReg;
import org.sysma.schedulerExecutor.LogLine.QueryCall;
import org.sysma.schedulerExecutor.LogLine.QueryResume;
import org.sysma.schedulerExecutor.LogLine.Replied;
import org.sysma.schedulerExecutor.LogLine.Resume;
import org.sysma.schedulerExecutor.LogLine.WaitFor;
import org.sysma.schedulerExecutor.LogLine.WaitForWithReg;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LogLineDeserializer implements JsonDeserializer<LogLine>,
	JsonSerializer<LogLine>{

	@Override
	public LogLine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		var map = json.getAsJsonObject();
		Class<? extends LogLine> clazz = null;
		switch(map.get("kind").getAsString()) {
			case "begin":
				clazz = Begin.class;
				break;
			case "end":
				clazz = End.class;
				break;
			case "resume":
				clazz = Resume.class;
				break;
			case "call":
				clazz = Call.class;
				break;
			case "call_reg":
				clazz = CallWithReg.class;
				break;
			case "wait_reg":
				clazz = WaitForWithReg.class;
				break;
			case "fwd_reg":
				clazz = ForwardReg.class;
				break;
			case "replied":
				clazz = Replied.class;
				break;
			case "wait":
				clazz = WaitFor.class;
				break;
			case "query_call":
				clazz = QueryCall.class;
				break;
			case "query_resume":
				clazz = QueryResume.class;
				break;
			case "fwdcall_reg":
				clazz = ForwardCallWithReg.class;
				break;
			case "freg":
				clazz = ForwardingReg.class;
				break;
		}
		return context.deserialize(json, clazz);
	}

	@Override
	public JsonElement serialize(LogLine src, Type typeOfSrc, JsonSerializationContext context) {
		Class<? extends LogLine> clazz = null;
		switch(src.kind) {
			case "begin":
				clazz = Begin.class;
				break;
			case "end":
				clazz = End.class;
				break;
			case "resume":
				clazz = Resume.class;
				break;
			case "call":
				clazz = Call.class;
				break;
			case "call_reg":
				clazz = CallWithReg.class;
				break;
			case "wait_reg":
				clazz = WaitForWithReg.class;
				break;
			case "fwd_reg":
				clazz = ForwardReg.class;
				break;
			case "replied":
				clazz = Replied.class;
				break;
			case "wait":
				clazz = WaitFor.class;
				break;
			case "query_call":
				clazz = QueryCall.class;
				break;
			case "query_resume":
				clazz = QueryResume.class;
				break;
			case "fwdcall_reg":
				clazz = ForwardCallWithReg.class;
				break;
			case "freg":
				clazz = ForwardingReg.class;
				break;
		}
		return context.serialize(src, clazz);
	}
}
