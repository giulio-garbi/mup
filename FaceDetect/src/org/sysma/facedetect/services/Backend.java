package org.sysma.facedetect.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

@TaskDef(name="backend")
public class Backend extends TaskDefinition {
	
	private static int slowdown() {
		/*long k=1;
		for(long i=0; i<2_000_000L; i++)
			k += i*i;
		return (int)k;*/
		return 0;
	}
	
	static ThreadLocal<CascadeClassifier> face_cascade = ThreadLocal.withInitial(()->{
		return new CascadeClassifier("aux/data/haarcascade_frontalface_default.xml");
	});
	
	@EntryDef("/")
	public void StartPage(Communication comm) throws IOException {
		comm.respond(200, (" <!doctype html>\n"
				+ "        <title>Backend</title>\n"
				+ "        <h1>The backend microservice</h1>\n"
				+ "        <p>use /detect/ to detect faces in an image</p>\n"
				+ "        </form>").getBytes());
	}
	
	@EntryDef("/detect/")
	public void UploadFile(Communication comm) throws Exception {
		int z = slowdown();
		if(comm.getRequestMethod().equals("POST")) {
			var params = comm.getPostParameters();
			if(!params.containsKey("file")) {
				comm.respond(200+z*0, ("<!doctype html>\n"
						+ "    <title>Backend</title>\n"
						+ "    <h1>Backend, send POST with file</h1>\n"
						+ "    </form>").getBytes());
				return;
			}
			
			var timeout = Float.parseFloat(params.getOrDefault("upstream-timeout", "2.0"));
			var storage_extraload = (params.getOrDefault("storage-extraload", "5"));
			var mob = new MatOfByte(Base64.getDecoder().decode(params.get("file").split("\\|",2)[1]));
			var img = Imgcodecs.imdecode(mob,Imgcodecs.IMREAD_COLOR);
			var gray = new MatOfByte();
			Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
			
			ArrayList<MatOfRect> faces_v = new ArrayList<>();
			
			for(int k=0; k<3; k++) {
				var mor = new MatOfRect();
				face_cascade.get().detectMultiScale(gray, mor, 1.3, 5);
				faces_v.add(mor);
			}
			
			var faces = faces_v.get(faces_v.size()-1).toList();
			String facesAns = "[";
			for(int i=0; i<faces.size(); i++) {
				if(i>0)
					facesAns += ", ";
				var faces_i = faces.get(i);
				var x = faces_i.x;
				var y = faces_i.y;
				var w = faces_i.width;
				var h = faces_i.height;
				facesAns += "["+x+", "+y+", "+w+", "+h+"]";
				Imgproc.rectangle(img, new Point(x,y), new Point(x+w,y+h), new Scalar(255,0,0), 2);
			}
			facesAns += "]";
			MatOfByte img_encode = new MatOfByte();
		    Imgcodecs.imencode(".jpg", img, img_encode);
		    
		    comm.asyncCallRegistryWithFile("storage", "StorageSet", x->{},
		    				"files", params.get("file").split("\\|",2)[0], img_encode.toArray(),
							"upstream-timeout", Float.toString(timeout-1), 
							"storage-extraload", storage_extraload).get().close();
		    var node_name = System.getenv("NODE_NAME");
		    if(node_name == null || node_name.length() == 0)
		        node_name = "localhost";
		    String resp = "{\"faces\":"+facesAns+", \"backend_name\": \""+node_name+"\"}";
		    comm.respond(200, resp.getBytes());
		} else {
			comm.respond(200, ("<!doctype html>\n"
					+ "    <title>Backend</title>\n"
					+ "    <h1>Backend, send POST with file</h1>\n"
					+ "    </form> ").getBytes());
		}
	}
}
