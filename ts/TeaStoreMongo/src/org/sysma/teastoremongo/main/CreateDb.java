package org.sysma.teastoremongo.main;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class CreateDb {

	public static void main() {
		try(MongoClient client = MongoClients.create()){
			MongoDatabase mdb = client.getDatabase("teastore");
			makeUsers(mdb, 1000);
			makeCategories(mdb, 5, 100);
		}
	}
	
	private static void makeUsers(MongoDatabase mdb, int count) {
		var users = mdb.getCollection("user");
		users.deleteMany(com.mongodb.client.model.Filters.empty());
		for(int i=0; i<count; i++) {
			users.insertOne(new Document("_id",i)
					.append("email", "user"+i+"@teastore.com")
					.append("password", "user"+i)
					.append("username", "user"+i)
					.append("realname", "Name Surname "+i)
					.append("orders", List.of()));
		}
	}
	
	private static void makeCategories(MongoDatabase mdb, int nCat, int nProdPerCat) {
		var cats = mdb.getCollection("category");
		cats.deleteMany(com.mongodb.client.model.Filters.empty());
		var prods = mdb.getCollection("products");
		prods.deleteMany(com.mongodb.client.model.Filters.empty());
		
		for(int i=0; i<nCat; i++) {
			final int ii = i;
			var catid = i;
			cats.insertOne(new Document("_id", catid)
					.append("name", "Category "+i)
					.append("description", "Description of cat "+i));
			prods.insertMany(
				IntStream.range(0, nProdPerCat).mapToObj(j->
					new Document("_id", ii*nProdPerCat+j)
					.append("category_id", catid)
					.append("name", "Product "+ii+"-"+j)
					.append("description", "Description of product "+ii+"-"+j)
					.append("listpriceincents", (int)(Math.random()*15000))
				).collect(Collectors.toList()));
		}
	}

}
