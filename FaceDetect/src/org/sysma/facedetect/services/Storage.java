package org.sysma.facedetect.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bson.internal.Base64;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;

import io.netty.util.internal.ThreadLocalRandom;

@TaskDef(name="storage")
public class Storage extends TaskDefinition {
	private static int REDIS_PORT = 6379;
	static RedisClient redisClient = new RedisClient(
		      RedisURI.create("redis://localhost:"+REDIS_PORT));
	static ArrayList<String> imgFiles = new ArrayList<>();

	
	private static int slowdown() {
		long k=1;
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		k += 9*9;
		return (int)k;
	}
	
	public static void setup() throws IOException {
		for (var path: Files.list(Path.of("storage")).collect(Collectors.toList())){
			byte[] content = Files.readAllBytes(path);
			var conn = redisClient.connect();
			conn.append(path.getFileName().toString(), Base64.encode(content));
		}
	}
	
	@EntryDef("/")
	public void StartPage(Communication comm) throws IOException {
		comm.respond(200, "<!doctype html>\n<title>Storage</title>\n<h1>The storage microservice</h1>\n<p>use /store/ to store an image or /fetch/ to retrieve it</p>\n</form>".getBytes());
	}
	
	@EntryDef("/store/")
	public void StorageSet(Communication comm) throws Exception {
		int z = slowdown();
		if(comm.getRequestMethod().equals("POST")) {
			var params = comm.getPostParametersFiles();
			if(!params.containsKey("files")) {
				comm.respond(200+z*0, "<!doctype html>\n<title>Storage</title>\n<h1>Storage, send POST with thing to store</h1>\n</form>".getBytes());
				return;
			}
		    var node_name = System.getenv("NODE_NAME");
		    if(node_name == null || node_name.length() == 0)
		        node_name = "localhost";
		    comm.respond(200, ("{storage_name:\""+node_name+"\"}").getBytes(), "x-upstream-ip", "?", "Content-Type", "application/json");
		} else {
			comm.respond(200, "<!doctype html>\n<title>Storage</title>\n<h1>Storage, send POST with thing to store</h1>\n</form>".getBytes());
		}
	}
	
	@EntryDef("/fetch/")
	public void StorageGet(Communication comm) throws IOException {
		int z = slowdown();
		if(comm.getRequestMethod().equals("POST")) {
			var params = comm.getPostParameters();
			if(!params.containsKey("json")) {
				comm.respond(200+z*0, "<!doctype html>\n<title>Storage</title>\n<h1>Storage, send POST filename to retrieve</h1>\n</form>".getBytes());
				return;
			}
			var data = new Gson().fromJson(params.get("json"), com.google.gson.JsonObject.class);
		    RedisConnection<String, String> r = redisClient.connect();
		    
		    var storage_extraload = Integer.parseInt(params.getOrDefault("storage-extraload", "5"));
		    String ans;
		    if(imgFiles.size() == 0) {
		    	ans = "{\"exists\": \"false\"}";
		    	r.close();
		    	comm.respond(200, ans.getBytes(), "x-upstream-ip", "?", "Content-Type", "application/json");
		    } else {
		    	ArrayList<String> x = new ArrayList<>();
		    	for(int k=0; k<storage_extraload; k++) {
		    		x.add(r.get(imgFiles.get(ThreadLocalRandom.current().nextInt(imgFiles.size()))));
		    	}
		    	var key = data.get("imgfile").getAsString().split("\\.")[0];
		    	var file_data = r.get(key);
		    	if(file_data == null)
		    		file_data = r.get(imgFiles.get(ThreadLocalRandom.current().nextInt(imgFiles.size())));
		    	ans = file_data;
		    	r.close();
		    	comm.respond(200, ans.getBytes(), "x-upstream-ip", "?", "Content-Type", "image/jpeg");
		    }
		} else {
			comm.respond(200, "<!doctype html>\n<title>Storage</title>\n<h1>Storage, send POST filename to retrieve</h1>\n</form>".getBytes());
		}
	}
	
	public static void load() throws IOException {
		RedisConnection<String, String> r = redisClient.connect();
		Files.find(Path.of("aux/storage"), 999, (p, a)->a.isRegularFile()).forEach(	
			img->{
				try {
					var fn = img.getFileName().toString().split("\\.")[0];
					imgFiles.add(fn);
					r.append(fn, Base64.encode(Files.readAllBytes(img)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		);
		r.close();
	}
}
