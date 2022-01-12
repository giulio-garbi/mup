package org.sysma.teastoremongo.services;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.sysma.schedulerExecutor.Communication;
import org.sysma.schedulerExecutor.EntryDef;
import org.sysma.schedulerExecutor.TaskDef;
import org.sysma.schedulerExecutor.TaskDefinition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@TaskDef(name="image")
public class ImageTask extends TaskDefinition {
	public static String pimgPath = Util.baseDir+"/images/";
	
	@EntryDef("/getProductImages/")
	public void GetProductImages(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		String qry = params.get("qry");
		var gson = new Gson();
		JsonObject jqry = gson.fromJson(qry, JsonObject.class);
		JsonObject jans = new JsonObject();
		
		Set<String> keys = jqry.keySet();
		for(String key:keys) {
			int imgIdx = Integer.parseInt(key)+513;
			String fileData = Files.readString(Path.of(pimgPath+imgIdx));
			JsonArray sz = jqry.get(key).getAsJsonArray();
			
			BufferedImage img = new BufferedImage(768, 768, BufferedImage.TYPE_INT_ARGB);
			BufferedImage dimg = new BufferedImage(sz.get(0).getAsInt(), 
					sz.get(1).getAsInt(), img.getType());  
		    Graphics2D g = dimg.createGraphics();  
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		    g.drawImage(img, 0, 0, sz.get(0).getAsInt(), sz.get(1).getAsInt(), 
		    		0, 0, 768, 768, null);  
		    g.dispose();  
		    jans.addProperty(key, fileData);
		}
		
		comm.respond(200, gson.toJson(jans).toString().getBytes());
	}
	
	@EntryDef("/GetWebImages/")
	public void GetWebImages(Communication comm) throws IOException {
		var params = comm.getPostParameters();
		String qry = params.get("qry");
		var gson = new Gson();
		JsonObject jqry = gson.fromJson(qry, JsonObject.class);
		JsonObject jans = new JsonObject();
		
		Set<String> keys = jqry.keySet();
		for(String key:keys) {
			byte[] fileData = Files.readAllBytes(Path.of(pimgPath+key+".png"));
			JsonArray sz = jqry.get(key).getAsJsonArray();
			
			BufferedImage img = new BufferedImage(768, 768, BufferedImage.TYPE_INT_ARGB);
			BufferedImage dimg = new BufferedImage(sz.get(0).getAsInt(), 
					sz.get(1).getAsInt(), img.getType());  
		    Graphics2D g = dimg.createGraphics();  
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		    g.drawImage(img, 0, 0, sz.get(0).getAsInt(), sz.get(1).getAsInt(), 
		    		0, 0, 768, 768, null);  
		    g.dispose();  
		    var strFD = Base64.encodeBase64String(fileData);
		    jans.addProperty(key, strFD);
		}
		
		comm.respond(200, gson.toJson(jans).toString().getBytes());
	}
}
