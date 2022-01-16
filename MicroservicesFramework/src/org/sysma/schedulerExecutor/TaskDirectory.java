package org.sysma.schedulerExecutor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class TaskDirectory {
	private static URI registry;
	
	public static HttpTask instantiateRegistry(int port) {
		var ht = new Registry().instantiate(port, 0, 1);
		setRegistry(URI.create("http://localhost:"+port));
		return ht;
	}
	
	public static String getAllLogs() throws IOException {
		final RequestConfig requestConfig = RequestConfig.custom()
			    .setConnectTimeout(500000, TimeUnit.MILLISECONDS)
			    .setConnectionRequestTimeout(500000, TimeUnit.MILLISECONDS)
			    .setResponseTimeout(500000, TimeUnit.MILLISECONDS)
			    .build();
		try(CloseableHttpClient hclient = HttpClients.custom()
			    .setDefaultRequestConfig(requestConfig)
			    .build()){
			HttpPost httppost = new HttpPost(registry.resolve("/log"));
			httppost.setEntity(new UrlEncodedFormEntity(List.of()));				
			try(var httpresponse = hclient.execute(httppost)){
				return (Communication.inputStreamToString(httpresponse.getEntity().getContent()));
			}
		}
	}
	
	public static void setRegistry(URI registry) {
		TaskDirectory.registry = registry;
	}
	
	public static URI getRegistry() {
		return registry;
	}
	
	public static void register(Class<? extends TaskDefinition> tdef, int port) throws IOException {
		TaskDef td = tdef.getAnnotation(TaskDef.class);
		String taskName = td.name();
		ArrayList<NameValuePair> registerArgs = new ArrayList<>();
		registerArgs.add(new BasicNameValuePair("ms",taskName));
		registerArgs.add(new BasicNameValuePair("port",port+""));
		for(Method m: tdef.getMethods()) {
			EntryDef annEntry = m.getAnnotation(EntryDef.class);
			if(annEntry != null) {
				String entryName = m.getName();
				registerArgs.add(new BasicNameValuePair("ep_"+entryName, annEntry.value()));
			}
		}
		
		try(CloseableHttpClient hclient = HttpClients.createDefault()){
			HttpPost httppost = new HttpPost(getRegistry().resolve("/register"));
			httppost.setEntity(new UrlEncodedFormEntity(registerArgs));
			try(var x = hclient.execute(httppost)){}
		}
	}
	
	public static void delete(String task, int port) throws IOException {
		try(CloseableHttpClient hclient = HttpClients.createDefault()){
			HttpPost httppost = new HttpPost(getRegistry().resolve("/delete"));
			httppost.setEntity(new UrlEncodedFormEntity(List.of(
					new BasicNameValuePair("ms",task), new BasicNameValuePair("port",port+""))));
			try(var x = hclient.execute(httppost)){}
		}
	}
}
