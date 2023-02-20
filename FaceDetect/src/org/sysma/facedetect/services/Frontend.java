package org.sysma.facedetect.services;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.sysma.facdedetect.util.Util;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.netty.util.internal.ThreadLocalRandom;

@TaskDef(name="frontend")
public class Frontend extends TaskDefinition {
	static int n_backends=2;
	
	private static int slowdown() {
		/*long k=1;
		for(long i=0; i<2_000_000L; i++)
			k += i*i;
		return (int)k;*/
		return 0;
	}
	
	@EntryDef("/")
	public void StartPage(Communication comm) throws IOException {
		comm.respond(200, "<!doctype html>\n<title>Frontend</title>\n<h1>The frontend microservice</h1>\n<p>use /detect/ to detect faces in an image or /fetch/ to fetch an image</p>\n</form>".getBytes());
	}
	
	private static int choice(float[] weights) {
		float sm = 0;
		for(var w:weights) {
			sm += w;
		}
		float chF = ThreadLocalRandom.current().nextFloat()*sm;
		float sm2 = 0;
		for(int i=0; i<weights.length-1; i++) {
			sm2 += weights[i];
			if(chF < sm2)
				return i;
		}
		return weights.length-1;
	}
	
	@EntryDef("/detect/")
	public void UploadFile(Communication comm) throws Exception {
		int z = slowdown();
		if(comm.getRequestMethod().equals("POST")) {
			var params = comm.getPostParametersFiles();
			if(!params.containsKey("imgfile") || params.get("imgfile").length() == 0 || 
					!params.get("imgfile").split("\\|",2)[0].endsWith(".jpg")) {
				comm.respond(200+z*0, (" <!doctype html>\n"
						+ "    <title>Upload new file</title>\n"
						+ "    <h1>Upload new file new</h1>\n"
						+ "    <form method=post enctype=multipart/form-data>\n"
						+ "      <input type=file name=imgfile>\n"
						+ "      <input type=text name=upstream-timeout>\n"
						+ "      <input type=submit value=Upload>\n"
						+ "    </form>").getBytes());
				return;
			}
			var stream = params.get("imgfile").split("\\|",2)[1];
			var lb_weights_str = params.getOrDefault("lb-weights", "1").split(",");
			var lb_weights = new float[lb_weights_str.length];
			for(int i=0; i<lb_weights_str.length; i++)
				lb_weights[i] = Float.parseFloat(lb_weights_str[i]);
			if(lb_weights.length != n_backends) {
				lb_weights = new float[n_backends];
				for(int i=0; i<n_backends; i++) {
					lb_weights[i] = (float) (1.0/n_backends);
				}
			}
			var timeout = Float.parseFloat(params.getOrDefault("upstream-timeout", "2.0"));
			var storage_extraload = (params.getOrDefault("storage-extraload", "5"));
			String backend = "backend"+choice(lb_weights);
			var ans = comm.asyncCallRegistry(backend, "UploadFile", x->{}, 
			  		"file", params.get("imgfile").split("\\|",2)[0]+"|"+stream,
					"upstream-timeout", Float.toString(timeout-1), 
					"storage-extraload", storage_extraload).get();
			var rsp = Util.inputStreamToString(ans.getEntity().getContent());
			ans.close();
			var data = new Gson().fromJson(rsp, JsonObject.class);

			var mob = new MatOfByte(org.bson.internal.Base64.decode(stream));
			var img = Imgcodecs.imdecode(mob,0);
			var faces = data.get("faces").getAsJsonArray();
			for(int i=0; i<faces.size(); i++) {
				var faces_i = faces.get(i).getAsJsonArray();
				var x = faces_i.get(0).getAsInt();
				var y = faces_i.get(1).getAsInt();
				var w = faces_i.get(2).getAsInt();
				var h = faces_i.get(3).getAsInt();
				Imgproc.rectangle(img, new Point(x,y), new Point(x+w,y+h), new Scalar(255,0,0), 2);
			}
			var node_name = System.getenv("NODE_NAME");
		    if(node_name == null || node_name.length() == 0)
		        node_name = "localhost";
		    Imgproc.putText(img, "Frontend: " + node_name, new Point(20,20), Imgproc.FONT_HERSHEY_SIMPLEX, .5, new Scalar(255,55,55),1);
		    Imgproc.putText(img, "Backend: " + data.get("backend_name").getAsString(), new Point(20,40), Imgproc.FONT_HERSHEY_SIMPLEX, .5, new Scalar(255,55,55),1);
		    MatOfByte img_encode = new MatOfByte();
		    Imgcodecs.imencode(".jpg", img, img_encode);
			
		    comm.respond(200, img_encode.toArray(), "x-upstream-ip", "?");
		} else {
			comm.respond(200, (" <!doctype html>"
					+ "    <title>Upload new file</title>"
					+ "    <h1>Upload new file new</h1>"
					+ "    <form method=post enctype=multipart/form-data>"
					+ "      <input type=file name=imgfile>"
					+ "      <input type=submit value=Upload>"
					+ "    </form>").getBytes());
		
		}
	}
	
	@EntryDef("/fetch/")
	public void GetVal(Communication comm) throws IOException, InterruptedException, ExecutionException {
		int z = slowdown();
		if(comm.getRequestMethod().equals("POST")) {
			Map<String, String> params = comm.getPostParameters();
			if(!params.containsKey("imgfile") || params.get("imgfile").length() == 0 || !params.get("imgfile").split("\\|",2)[0].endsWith(".jpg")) {
				comm.respond(200+z*0, ("<!doctype html>\n"
						+ "    <title>Get file</title>\n"
						+ "    <h1>Get file</h1>\n"
						+ "    <form method=post>\n"
						+ "      <input type=form name=imgfile>\n"
						+ "      <input type=submit value=Retrieve>\n"
						+ "    </form>").getBytes());
				return;
			}
			var timeout = Float.parseFloat(params.getOrDefault("upstream-timeout", "2.0"));
			var storage_extraload = (params.getOrDefault("storage-extraload", "5"));
			
			CloseableHttpResponse ans = comm.asyncCallRegistry("storage", "StorageGet", x->{}, 
					"json", "{\"imgfile\":"+params.get("imgfile")+"\"}",
					"upstream-timeout", Float.toString(timeout-1), 
					"storage-extraload", storage_extraload).get();
			String response = null;
			if(ans.getEntity().getContentType().equals("application/json")) {
				response = Util.inputStreamToString(ans.getEntity().getContent());
				ans.close();
				comm.respond(200, response.getBytes(), "Content-Type", "application/json");
			} else {
				byte[] content = org.bson.internal.Base64.decode(Util.inputStreamToString(ans.getEntity().getContent()));
				ans.close();
				var mob = new MatOfByte(content);
				var img = Imgcodecs.imdecode(mob,0);
				MatOfByte img_encode = new MatOfByte();
			    Imgcodecs.imencode(".jpg", img, img_encode);
				comm.respond(200, img_encode.toArray(), "Content-Type", "image/jpeg");
			}
		    
		} else {
			comm.respond(200, ("<!doctype html>\n"
					+ "    <title>Get file</title>\n"
					+ "    <h1>Get file</h1>\n"
					+ "    <form method=post>\n"
					+ "      <input type=form name=imgfile>\n"
					+ "      <input type=submit value=Retrieve>\n"
					+ "    </form>").getBytes());
		}
	}
}
