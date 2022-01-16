package org.sysma.jpetstoremongo.main;

import java.io.File;
import java.nio.file.Files;

import org.bson.Document;

import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;

public class CreateDb {

	public static void main() throws Exception {
		var path = "jpscsv/";
		File dir = new File(path);
		try(var mc = MongoClients.create()){
			var mdb = mc.getDatabase("jps");
			mdb.drop();
			mdb = mc.getDatabase("jps");
			for(File f:dir.listFiles()) {
				System.out.println(f);
				String collName = f.getName().split("\\.")[0];
				if(collName.length() == 0)
					continue;
				var coll = mdb.getCollection(collName);
				coll.deleteMany(Filters.empty());
				var lines = Files.readAllLines(f.toPath());
				String[] fieldTypes = lines.get(0).split(";");
				String[] fieldNames = lines.get(1).split(";");
				for(String line:lines.subList(2, lines.size())) {
					String[] parts = line.split(";", -1);
					Document d = new Document();
					for(int i=0; i<parts.length; i++) {
						if(fieldTypes[i].strip().equals("s"))
							d.append(fieldNames[i], parts[i]);
						else if(fieldTypes[i].strip().equals("d"))
							d.append(fieldNames[i], Long.parseLong(parts[i]));
						else if(fieldTypes[i].strip().equals("f"))
							d.append(fieldNames[i], Double.parseDouble(parts[i]));
						else
							throw new Exception("Invalid format: "+fieldTypes[i]);
					}
					coll.insertOne(d);
				}
			}
		}
	}
}
