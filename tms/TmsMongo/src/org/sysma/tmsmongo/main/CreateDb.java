package org.sysma.tmsmongo.main;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class CreateDb {
	
	public static List<Document> createCategories(MongoDatabase mdb, int n){
		var coll = mdb.getCollection("categories");
		var ans = IntStream.range(0, n).mapToObj(
				i->new Document("_id", new ObjectId())
					.append("name", "cat #"+i)
					.append("description", "cat desc #"+i)
			).collect(Collectors.toList());
		coll.insertMany(ans);
		return ans;
	}
	
	public static List<Document> createQuestions(MongoDatabase mdb, int n, List<Document> cats){
		var coll = mdb.getCollection("questions");
		
		var ans = cats.stream().flatMap(
				cat->IntStream.range(0, n).mapToObj(
						q->new Document("_id", new ObjectId())
							.append("title", "title #"+q+" cat "+cat.getObjectId("_id"))
							.append("level", q%3)
							.append("body", "body #"+q+" cat "+cat.getObjectId("_id"))
							.append("category", cat.getObjectId("_id"))
							.append("language", "lang")))
				.collect(Collectors.toList());
		coll.insertMany(ans);
		return ans;
	}
	
	public static List<Document> createConfgroups(MongoDatabase mdb, List<Document> cats){
		var coll = mdb.getCollection("confgroups");
		
		var ans = cats.stream().flatMap(
				cat->IntStream.range(0, 3).mapToObj(
						lvl->new Document("_id", new ObjectId())
							.append("category", cat.getObjectId("_id"))
							.append("level", lvl)
							.append("language", "lang")
							.append("count", 2)))
				.collect(Collectors.toList());
		coll.insertMany(ans);
		return ans;
	}
	
	public static List<Document> createConfs(MongoDatabase mdb, int n, List<Document> cgroups){
		var coll = mdb.getCollection("confs");
		
		var ans = IntStream.range(0, n).mapToObj(i->
			new Document("_id", new ObjectId())
			.append("name", "name conf "+i)
			.append("description", "desc conf "+i)
			.append("groups", cgroups.stream().filter(x->Math.random()<0.5).collect(Collectors.toList()))
		).collect(Collectors.toList());

		coll.insertMany(ans);
		return ans;
	}
	
	public static List<Document> createExams(MongoDatabase mdb, int n, List<Document> confs){
		var coll = mdb.getCollection("exams");
		
		var ans = IntStream.range(0, n).mapToObj(i->
			new Document("_id", new ObjectId())
			.append("examinee", ("examinee "+i))
			.append("configId", confs.get((int)(Math.random()*confs.size())).get("_id"))
			.append("examDate", ("examDate "+i))
		).collect(Collectors.toList());

		coll.insertMany(ans);
		return ans;
	}
	
	public static List<Document> createUsers(MongoDatabase mdb, int n){
		var coll = mdb.getCollection("users");
		var ans = IntStream.range(0, n).mapToObj(
				i->new Document("_id", new ObjectId())
					.append("username", "user"+i)
					.append("email", "email"+i+"@gmail.com")
			).collect(Collectors.toList());
		coll.insertMany(ans);
		return ans;
	}
	
	public static void main(String[] args) throws Exception {
		int nCategories = 10;
		int nQperCat = 15;
		int nConfs = 50;
		int nExams = 50;
		int nUsers = 50;
		
		
		try(var mc = MongoClients.create()){
			var mdb = mc.getDatabase("tms");
			mdb.drop();
			mdb = mc.getDatabase("tms");
			
			var cats = createCategories(mdb, nCategories);
			createQuestions(mdb, nQperCat, cats);
			var cgroups = createConfgroups(mdb, cats);
			var confs = createConfs(mdb, nConfs, cgroups);
			createExams(mdb, nExams, confs);
			createUsers(mdb, nUsers);
		}
	}
}
