package org.sysma.schedulerExecutor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.sysma.schedulerExecutor.Queries.ReadQuery;
import org.sysma.schedulerExecutor.Queries.WriteQuery;

import com.mongodb.client.MongoClient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class Communication {
	private static class NetworkTimeMonitor{
		long sendToDestTime = -1;
		long receivedAtDestTime = -1;
		long sentbackTime = -1;
		long receivedbackTime = -1;
		
		public CloseableHttpResponse execute(CloseableHttpClient hclient, 
				ClassicHttpRequest msg) throws IOException {
			sendToDestTime = System.currentTimeMillis();
			var httpresponse = hclient.execute(msg);
			receivedbackTime = System.currentTimeMillis();
			try {
				var sback = httpresponse.getHeader("sentback");
				if(sback != null)
					sentbackTime = Long.parseLong(sback.getValue());
				var rcv = httpresponse.getHeader("received");
				if(rcv != null)
					receivedAtDestTime = Long.parseLong(rcv.getValue());
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return httpresponse;
		}
	}
	
	private static final ConnectionReuseStrategy NO_REUSE = new ConnectionReuseStrategy() {
		@Override
		public boolean keepAlive(HttpRequest request, HttpResponse response,
				org.apache.hc.core5.http.protocol.HttpContext context) {
			return false;
		}};
	final HttpExchange request;
	BasicCookieStore httpCookieStore = new BasicCookieStore();
	private CloseableHttpClient hclient = HttpClients.custom()
			.setConnectionReuseStrategy(NO_REUSE)
			.setDefaultCookieStore(httpCookieStore)
			.setDefaultRequestConfig(RequestConfig.custom()
			.setResponseTimeout(1, TimeUnit.DAYS)
			.setConnectionRequestTimeout(1, TimeUnit.DAYS)
			.setConnectTimeout(1, TimeUnit.DAYS).build())
			//.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
			//		.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofDays(1)).build())
			//		.build())
			.build();
			//HttpClients.createDefault();
	private final Scheduler sched;
	public final Entry entry;
	private Thread thread;
	private Semaphore lock;
	
	private final long receivedTime;
	
	private String entryName;
	private String taskName;
	public String client;
	
	String threadName;
	
	void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	
	void close() {
		try {
			hclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, String> getPostParameters() throws IOException{
		var data = this.getRequestBody().readAllBytes();
		var dts = new StringEntity(new String(data), 
				ContentType.APPLICATION_FORM_URLENCODED);
		var params = EntityUtils.parse(dts);
		HashMap<String, String> p = new HashMap<>();
		for(var nvp:params)
			p.put(nvp.getName(), nvp.getValue());
		return p;
	}
	
	/*public void respond(int rCode, byte[] response, Map<String, String> headers) throws IOException{
		for(var kv:headers.entrySet()) {
			request.getResponseHeaders().add(kv.getKey(), kv.getValue());
		}
		boolean hasBody = response != null && response.length > 0;
		if(hasBody) {
			request.sendResponseHeaders(rCode, response.length);
			request.getResponseBody().write(response);
		} else {
			request.sendResponseHeaders(rCode, -1);
		}
		request.close();
		entry.log.get().add(new LogLine.End(taskName, entryName, client, System.currentTimeMillis()));
	}*/
	
	public void respond(int rCode, byte[] response, String... headers) throws IOException{
		Headers h = request.getResponseHeaders();
		for(int i=0; i<headers.length; i+=2)
		//	if(!"sentback".equals(headers[i]))
				h.add(headers[i], headers[i+1]);
		//h.add("sentback", System.currentTimeMillis()+"");
		h.remove("received");
		h.add("received", this.receivedTime+"");
		h.remove("sentback");
		h.add("sentback", System.currentTimeMillis()+"");
		boolean hasBody = response != null && response.length > 0;
		if(hasBody) {
			request.sendResponseHeaders(rCode, response.length);
			request.getResponseBody().write(response);
		} else {
			request.sendResponseHeaders(rCode, -1);
		}
		request.getResponseBody().close();
		request.close();
		entry.log.get().add(new LogLine.End(taskName, entryName, client, System.currentTimeMillis()));
	}
	
	/*public void respond(int rCode, Map<String, String> headers) throws IOException{
		respond(rCode, null, headers);
	}*/
	
	public MongoClient getMongo() {
		return Queries.getMongoDbConnection(threadName);
	}
	
	public void respond(int rCode, String... headers) throws IOException{
		respond(rCode, null, headers);
	}
	
	public void readQuery(ReadQuery query, Consumer<PreparedStatement> fill, Consumer<ResultSet> read) throws SQLException {
		query.doRead(this, fill, read);
	}
	
	public void writeQuery(WriteQuery query, Consumer<PreparedStatement> fill) throws SQLException {
		query.doWrite(this, fill);
	}
	
	public void writeQuery(WriteQuery query, Consumer<PreparedStatement> fill, Consumer<ResultSet> read) throws SQLException {
		query.doWrite(this, fill, read);
	}
	
	public Headers getRequestHeaders() {
		return request.getRequestHeaders();
	}
	
	public URI getRequestURI() {
		return request.getRequestURI();
	}
	
	public String getRequestMethod() {
		return request.getRequestMethod();
	}

	public HttpContext getHttpContext() {
		return request.getHttpContext();
	}
	
	public InputStream getRequestBody() {
		return request.getRequestBody();
	}
	
	public InetSocketAddress getRemoteAddress() {
		return request.getRemoteAddress();
	}
	
	public InetSocketAddress getLocalAddress() {
		return request.getLocalAddress();
	}

	public String getProtocol() {
		return request.getProtocol();
	}

	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}
	
	public HttpPrincipal getPrincipal() {
		return request.getPrincipal();
	}
	
	
	Communication(Entry entry, HttpExchange request, Scheduler sched, String client, 
			long receivedTime){
		this.client = client;
		this.entryName = entry.entryName;
		this.taskName = entry.taskName;
		//
		this.request = request;
		this.sched = sched;
		this.entry = entry;
		this.receivedTime = receivedTime;
		this.lock = new Semaphore(0);
	}
	
	void start() {
		thread = new Thread(()->{
			try {
				//System.out.println(System.currentTimeMillis()+" start1 "+System.identityHashCode(this));
				sched.callTaskset();
				//System.out.println(System.currentTimeMillis()+" start2 "+System.identityHashCode(this));
				yieldBeforeStart();
				//System.out.println(System.currentTimeMillis()+" start3 "+System.identityHashCode(this));
				entry.run(this, this.threadName);
				//System.out.println(System.currentTimeMillis()+" Job end" );
				sched.jobHalted(this);
				hclient.close();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}
	
	void resume() {
		lock.release();
	}
	
	private void yieldBeforeStart() {
		sched.addWaitingJob(this);
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*public void asyncCall(String name, String task, String entry, Consumer<HttpUriRequest> customizeRequest) {
		String calledEntry = task+"."+entry;
		URI destination = TaskDefinition.getURI(task, entry);
		this.pendingCalls.put(name, asyncCall(destination, calledEntry, customizeRequest));
	}
	
	public CloseableHttpResponse waitForCall(String name) {
		try {
			return this.pendingCalls.get(name).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/
	
	public CompletableFuture<CloseableHttpResponse> asyncCallPageRegistry(String calledTaskName, 
			String calledPage, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		String calledEntryName = "FileServer"+(calledPage.startsWith("/")?"":"/")+calledPage;
		return this.asyncCallRegistry(calledTaskName, calledEntryName, customizeRequest, params);
	}
	
	private CompletableFuture<CloseableHttpResponse> asyncCallPage(String calledTaskName, 
			String calledPage, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		String calledEntryName = "FileServer"+(calledPage.startsWith("/")?"":"/")+calledPage;
		entry.log.get().add(new LogLine.Call(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		CompletableFuture<CloseableHttpResponse> future = new CompletableFuture<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse get() throws InterruptedException, ExecutionException {
				entry.log.get().add(new LogLine.WaitFor(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
				var ans = super.get();
				entry.log.get().add(new LogLine.Resume(entry.taskName, entry.entryName, client, System.currentTimeMillis()));
				return ans;
			}
		};
		//long acStartTime = System.currentTimeMillis();
		new Thread(new Runnable(){
			public void run() {
				//long acStartedTime = System.currentTimeMillis();
				//System.out.println("Communication.asyncCallPage; "+(acStartedTime-acStartTime));
				HttpPost httppost = new HttpPost(TaskDefinition.getURIPage(calledTaskName, calledPage));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				customizeRequest.accept(httppost);
				try {
					//System.out.println(httppost.getRequestUri()+" ->");
					
					/*var httpresponse = hclient.execute(httppost);
					long sbackTime = -1;*/
					
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					var httpresponse = commTime.execute(hclient, httppost);
					/*System.out.println("-- "+httpresponse.getCode());
					for(var ck:httpresponse.getHeaders()) {
						System.out.println(ck.getName() + " => " + ck.getValue());
					};*/
					/*try {
						//var sback = httpresponse.getHeader("sentback");
						//if(sback != null)
						//	sbackTime = Long.parseLong(sback.getValue());
					} catch (ProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
					entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, 
							calledEntryName, client, commTime.receivedbackTime, commTime.sentbackTime, 
							commTime.sendToDestTime, commTime.receivedAtDestTime));
					future.complete(httpresponse);
				} catch (CancellationException | IOException e) {
					future.completeExceptionally(e);
				} 
			}
		}).start();
		return future;
	}
	
	private CompletableFuture<CloseableHttpResponse> asyncCall(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			Map<String,String> params) {
		var par = params.entrySet().stream().flatMap(e->Stream.of(e.getKey(), e.getValue())).toArray(String[]::new);
		return this.asyncCall(calledTaskName, calledEntryName, customizeRequest, par);
	}
	
	
	
	private CompletableFuture<CloseableHttpResponse> asyncCall(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		entry.log.get().add(new LogLine.Call(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		CompletableFuture<CloseableHttpResponse> future = new CompletableFuture<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse get() throws InterruptedException, ExecutionException {
				entry.log.get().add(new LogLine.WaitFor(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
				var ans = super.get();
				entry.log.get().add(new LogLine.Resume(entry.taskName, entry.entryName, client, System.currentTimeMillis()));
				return ans;
			}
		};
		new Thread(new Runnable(){
			public void run() {
				// TODO Auto-generated method stub
				HttpPost httppost = new HttpPost(TaskDefinition.getURI(calledTaskName, calledEntryName));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				
				customizeRequest.accept(httppost);
				try {
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					var httpresponse = commTime.execute(hclient, httppost);
					/*System.out.println("-- "+httpresponse.getCode());
					for(var ck:httpresponse.getHeaders()) {
						System.out.println(ck.getName() + " => " + ck.getValue());
					};*/
					entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, calledEntryName, 
							client, commTime.receivedbackTime, commTime.sentbackTime, 
							commTime.sendToDestTime, commTime.receivedAtDestTime));
					future.complete(httpresponse);
				} catch (CancellationException | IOException e) {
					try {
						System.out.println(httppost.getUri());
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					future.completeExceptionally(e);
				} 
			}
			
		}).start();
		return future;
	}
	
	static String inputStreamToString(InputStream inputStream) {
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result;
		try {
			result = bis.read();
			while(result != -1) {
			    buf.write((byte) result);
			    result = bis.read();
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return buf.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static String[] headersToString(org.apache.hc.core5.http.Header[] headers) {
		String[] out = new String[headers.length * 2];
		for(int i=0; i<headers.length; i++) {
			out[2*i] = headers[0].getName();
			out[2*i+1] = headers[0].getValue();
		}
		return out;
	}
	
	
	

	public void forwardCallRegistry(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		
		//entry.log.get().add(new LogLine.ForwardCallWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		new Thread(new Runnable(){
			public void run() {
				try(CloseableHttpClient hclientFwd = HttpClients.custom()
						.setConnectionReuseStrategy(NO_REUSE)
						.setDefaultCookieStore(httpCookieStore).build()){
					URI msURI = null;
					HttpPost httppostReg = new HttpPost(TaskDirectory.getRegistry().resolve("/query"));
					httppostReg.setEntity(new UrlEncodedFormEntity(
							List.of(new BasicNameValuePair("ms", calledTaskName), 
									new BasicNameValuePair("ep", calledEntryName))));
					NetworkTimeMonitor commTimeReg = new NetworkTimeMonitor();
					try(var httpresponseReg = commTimeReg.execute(hclientFwd, httppostReg)){
						String ans = inputStreamToString(httpresponseReg.getEntity().getContent());
						msURI = URI.create(ans);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					
					HttpPost httppost = new HttpPost(msURI);
					httppost.setEntity(new UrlEncodedFormEntity(postParameters));
					
					customizeRequest.accept(httppost);
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					try(var httpresponse = commTime.execute(hclientFwd, httppost)) {
						/*System.out.println("-- "+httpresponse.getCode());
						for(var ck:httpresponse.getHeaders()) {
							System.out.println(ck.getName() + " => " + ck.getValue());
						};*/
						/*entry.log.get().add(new LogLine.ForwardReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, 
								commTimeReg.receivedbackTime, commTimeReg.sentbackTime, commTimeReg.sendToDestTime, commTimeReg.receivedAtDestTime,
								commTime.sendToDestTime));
						entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, calledEntryName, 
								client, commTime.receivedbackTime, commTime.sentbackTime, commTime.sendToDestTime, commTime.receivedAtDestTime));
						*/
						respondAfterForward(commTimeReg, commTime, calledTaskName, calledEntryName,
								httpresponse.getCode(), 
								inputStreamToString(httpresponse.getEntity().getContent()).getBytes(),
								headersToString(httpresponse.getHeaders()));
					} catch (CancellationException | IOException e) {
						try {
							System.out.println(httppost.getUri());
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							respond(503);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} 
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
			
		}).start();
	}
	

	
	public CompletableFuture<CloseableHttpResponse> asyncCallRegistry(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			Map<String,String> params) {
		var par = params.entrySet().stream().flatMap(e->Stream.of(e.getKey(), e.getValue())).toArray(String[]::new);
		return this.asyncCallRegistry(calledTaskName, calledEntryName, customizeRequest, par);
	}
	
	public CompletableFuture<CloseableHttpResponse> asyncCallRegistry(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		
		entry.log.get().add(new LogLine.CallWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		CompletableFuture<CloseableHttpResponse> future = new CompletableFuture<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse get() throws InterruptedException, ExecutionException {
				//Thread.sleep(1);
				entry.log.get().add(new LogLine.WaitForWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
				var ans = super.get();
				entry.log.get().add(new LogLine.Resume(entry.taskName, entry.entryName, client, System.currentTimeMillis()));
				Thread.sleep(1);
				return ans;
			}
		};
		new Thread(new Runnable(){
			public void run() {
				URI msURI = null;
				HttpPost httppostReg = new HttpPost(TaskDirectory.getRegistry().resolve("/query"));
				httppostReg.setEntity(new UrlEncodedFormEntity(
						List.of(new BasicNameValuePair("ms", calledTaskName), 
								new BasicNameValuePair("ep", calledEntryName))));
				NetworkTimeMonitor commTimeReg = new NetworkTimeMonitor();
				try(var httpresponseReg = commTimeReg.execute(hclient, httppostReg)){
					String ans = inputStreamToString(httpresponseReg.getEntity().getContent());
					msURI = URI.create(ans);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				HttpPost httppost = new HttpPost(msURI);
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				
				customizeRequest.accept(httppost);
				try {
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					var httpresponse = commTime.execute(hclient, httppost);
					/*System.out.println("-- "+httpresponse.getCode());
					for(var ck:httpresponse.getHeaders()) {
						System.out.println(ck.getName() + " => " + ck.getValue());
					};*/
					entry.log.get().add(new LogLine.ForwardReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, 
							commTimeReg.receivedbackTime, commTimeReg.sentbackTime, commTimeReg.sendToDestTime, commTimeReg.receivedAtDestTime,
							commTime.sendToDestTime));
					entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, calledEntryName, 
							client, commTime.receivedbackTime, commTime.sentbackTime, commTime.sendToDestTime, commTime.receivedAtDestTime));
					future.complete(httpresponse);
				} catch (CancellationException | IOException e) {
					try {
						System.out.println(httppost.getUri());
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					future.completeExceptionally(e);
				} 
			}
			
		}).start();
		return future;
	}

	public Future<CloseableHttpResponse> asyncCallWithRegExec(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		
		entry.log.get().add(new LogLine.CallWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		CompletableFuture<CloseableHttpResponse> future = new CompletableFuture<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse get() throws InterruptedException, ExecutionException {
				entry.log.get().add(new LogLine.WaitForWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
				var ans = super.get();
				entry.log.get().add(new LogLine.Resume(entry.taskName, entry.entryName, client, System.currentTimeMillis()));
				return ans;
			}
		};
		new Thread(new Runnable(){
			public void run() {
				HttpPost httppostReg = new HttpPost(TaskDefinition.getURI("registry", "registry-Query"));
				httppostReg.setEntity(new UrlEncodedFormEntity(
						List.of()));
				NetworkTimeMonitor commTimeReg = new NetworkTimeMonitor();
				try(var httpresponseReg = commTimeReg.execute(hclient, httppostReg)){
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				HttpPost httppost = new HttpPost(TaskDefinition.getURI(calledTaskName, calledEntryName));
				httppost.setEntity(new UrlEncodedFormEntity(postParameters));
				
				customizeRequest.accept(httppost);
				try {
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					var httpresponse = commTime.execute(hclient, httppost);
					/*System.out.println("-- "+httpresponse.getCode());
					for(var ck:httpresponse.getHeaders()) {
						System.out.println(ck.getName() + " => " + ck.getValue());
					};*/
					entry.log.get().add(new LogLine.ForwardReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, 
							commTimeReg.receivedbackTime, commTimeReg.sentbackTime, commTimeReg.sendToDestTime, commTimeReg.receivedAtDestTime,
							commTime.sendToDestTime));
					entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client,
							commTime.receivedbackTime, commTime.sentbackTime, commTime.sendToDestTime, commTime.receivedAtDestTime));
					future.complete(httpresponse);
				} catch (CancellationException | IOException e) {
					try {
						System.out.println(httppost.getUri());
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					future.completeExceptionally(e);
				} 
			}
			
		}).start();
		return future;
	}
	
	public void forwardWithRegExec(String calledTaskName, 
			String calledEntryName, 
			Consumer<HttpPost> customizeRequest,
			String... params) {
		var postParameters = new ArrayList<NameValuePair>();
		for(int i=0; i<params.length; i+=2)
			postParameters.add(new BasicNameValuePair(params[i], params[i+1]));
		
		//entry.log.get().add(new LogLine.ForwardCallWithReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, System.currentTimeMillis()));
		new Thread(new Runnable(){
			public void run() {
				try(CloseableHttpClient hclientFwd = HttpClients.custom()
						.setConnectionReuseStrategy(NO_REUSE)
						.setDefaultCookieStore(httpCookieStore).build()){
					HttpPost httppostReg = new HttpPost(TaskDefinition.getURI("registry", "registry-Query"));
					httppostReg.setEntity(new UrlEncodedFormEntity(
							List.of()));
					NetworkTimeMonitor commTimeReg = new NetworkTimeMonitor();
					try(var httpresponseReg = commTimeReg.execute(hclientFwd, httppostReg)){
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					
					
					HttpPost httppost = new HttpPost(TaskDefinition.getURI(calledTaskName, calledEntryName));
					httppost.setEntity(new UrlEncodedFormEntity(postParameters));
					
					customizeRequest.accept(httppost);
					NetworkTimeMonitor commTime = new NetworkTimeMonitor();
					try (var httpresponse = commTime.execute(hclientFwd, httppost)) {
						/*System.out.println("-- "+httpresponse.getCode());
						for(var ck:httpresponse.getHeaders()) {
							System.out.println(ck.getName() + " => " + ck.getValue());
						};*/
						/*entry.log.get().add(new LogLine.ForwardReg(entry.taskName, entry.entryName, calledTaskName, calledEntryName, client, 
								commTimeReg.receivedbackTime, commTimeReg.sentbackTime, commTimeReg.sendToDestTime, commTimeReg.receivedAtDestTime,
								commTime.sendToDestTime));
						entry.log.get().add(new LogLine.Replied(entry.taskName, entry.entryName, calledTaskName, calledEntryName, 
								client, commTime.receivedbackTime, commTime.sentbackTime,
								commTime.sendToDestTime, commTime.receivedAtDestTime));*/
						respondAfterForward(commTimeReg, commTime, calledTaskName, calledEntryName, httpresponse.getCode(), 
								inputStreamToString(httpresponse.getEntity().getContent()).getBytes(),
								headersToString(httpresponse.getHeaders()));
					} catch (CancellationException | IOException e) {
						try {
							System.out.println(httppost.getUri());
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							respond(503);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} 
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
			}
			
		}).start();
	}
	
	private void respondAfterForward(NetworkTimeMonitor commTimeReg,
			NetworkTimeMonitor commTime, String calledTaskName, String calledEntryName,
			int rCode, byte[] response, String... headers) throws IOException{
		Headers h = request.getResponseHeaders();
		for(int i=0; i<headers.length; i+=2)
				h.add(headers[i], headers[i+1]);
		h.remove("received");
		h.add("received", this.receivedTime+"");
		h.remove("sentback");
		h.add("sentback", System.currentTimeMillis()+"");
		boolean hasBody = response != null && response.length > 0;
		if(hasBody) {
			request.sendResponseHeaders(rCode, response.length);
			request.getResponseBody().write(response);
		} else {
			request.sendResponseHeaders(rCode, -1);
		}
		request.getResponseBody().close();
		request.close();
		entry.log.get().add(new LogLine.ForwardingReg(taskName, entryName, calledTaskName, calledEntryName, client, 
				System.currentTimeMillis(), 
				commTimeReg.sendToDestTime, commTimeReg.receivedAtDestTime, commTimeReg.sentbackTime, commTimeReg.receivedbackTime,
				commTime.sendToDestTime, commTime.receivedAtDestTime, commTime.sentbackTime, commTime.receivedbackTime));
	}
}
