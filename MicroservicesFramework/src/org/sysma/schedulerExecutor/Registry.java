package org.sysma.schedulerExecutor;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.sun.net.httpserver.HttpExchange;

@TaskDef(name="registry")
public class Registry extends TaskDefinition {
	
	private final HashMap<String, ArrayList<String>> reg = new HashMap<>();
	private final HashMap<String, HashMap<String, String>> entries = new HashMap<>();
	
	@EntryDef("/query")
	public void Query(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		var ms = params.get("ms");
		var ep = params.get("ep");
		var services = reg.get(ms);
		if(services.isEmpty()) {
			comm.respond(503);
		} else {
			int i = ThreadLocalRandom.current().nextInt(services.size());
			URI ans = URI.create(services.get(i));
			//System.out.println(ans);
			//System.out.println(ms);
			//System.out.println(ep);
			if(ep.startsWith("FileServer/"))
				ans = ans.resolve((ep.substring(10)));
			else
				ans = ans.resolve(entries.get(ms).get(ep));
			//System.out.println(ans);
			comm.respond(200,ans.toString().getBytes());
		}
	}
	
	private static Map<String, String> getPostParameters(HttpExchange exc) throws IOException{
		var data = exc.getRequestBody().readAllBytes();
		var dts = new StringEntity(new String(data), 
				ContentType.APPLICATION_FORM_URLENCODED);
		var params = EntityUtils.parse(dts);
		HashMap<String, String> p = new HashMap<>();
		for(var nvp:params)
			p.put(nvp.getName(), nvp.getValue());
		return p;
	}
	
	@ContextDef("/register")
	public void Register(HttpExchange exc, HttpTask ht) throws IOException {
		var params = getPostParameters(exc);
		var ms = params.get("ms");
		var port = Integer.parseInt(params.get("port"));
		reg.computeIfAbsent(ms, x->new ArrayList<>())
			.add("http:/"+exc.getRemoteAddress().getAddress()+":"+port);
		var entmap = entries.computeIfAbsent(ms, x->new HashMap<>());
		for(var kv:params.entrySet()) {
			if(kv.getKey().startsWith("ep_")) {
				entmap.put(kv.getKey().substring(3), kv.getValue());
			}
		}
		exc.sendResponseHeaders(200, -1);
		exc.getResponseBody().close();
		exc.close();
	}
	
	@ContextDef("/delete")
	public void Delete(HttpExchange exc, HttpTask ht) throws IOException {
		var params = getPostParameters(exc);
		var ms = params.get("ms");
		var port = Integer.parseInt(params.get("port"));
		reg.computeIfAbsent(ms, x->new ArrayList<>())
			.remove("http:/"+exc.getRemoteAddress().getAddress()+":"+port);
		if(reg.get(ms).isEmpty())
			entries.remove(ms);
		exc.sendResponseHeaders(200, -1);
		exc.getResponseBody().close();
		exc.close();
	}
	
	@ContextDef("/log")
	public void Log(HttpExchange request, HttpTask ht) throws IOException {
		ArrayList<String> logs = new ArrayList<>();
		logs.add(ht.getLogAndClear());
		
		try(CloseableHttpClient hclient = HttpClients.createDefault()){
		
			for(var mss:reg.values()) {
				for(var ms:mss) {
					System.out.println(URI.create(ms+"/log"));
					HttpPost httppost = new HttpPost(URI.create(ms+"/log"));
					httppost.setEntity(new UrlEncodedFormEntity(List.of()));				
					try(var httpresponse = hclient.execute(httppost)){
						logs.add(Communication.inputStreamToString(httpresponse.getEntity().getContent()));
					}
				}
			}
		}
		String ansS = "["+logs.stream().collect(Collectors.joining(", "))+"]";
		byte[] ans = ansS.getBytes();
		request.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
		request.sendResponseHeaders(200, ans.length);
		request.getResponseBody().write(ans);
		request.getResponseBody().close();
	}
}
