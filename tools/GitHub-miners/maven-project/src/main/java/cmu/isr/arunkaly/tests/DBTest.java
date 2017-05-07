package cmu.isr.arunkaly.tests;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import cmu.isr.arunkaly.configs.Configurations;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DBTest {

	
	private static MongoClient mongoClient;
	private static MongoDatabase db;
	
	public static void main(String[] args) {
		//Connect to Mongodb
		createdbinstance();
		//db.createCollection("TestDB");
		MongoCollection<Document> col = db.getCollection("TestDB");
		TestDBObject dbObject1 = createDbObject("name5");
		TestDBObject dbObject2 = createDbObject("name6");
		//col.insertOne(Document.parse((new JSONObject(dbObject1)).toString()));
		
		//DBObject do1 = new BasicDBObject("_id", "name2").append("login", "hello").append("count", 23);
		//System.out.println(do1.toString());
		JSONObject jo = new JSONObject(dbObject2);
		jo.put("_id", "name6");
		//col.insertOne(Document.parse((jo.toString())));
		col.insertOne(Document.parse((jo.toString())));
		
		
		//List<Activity1> myActivities = new ArrayList<Activity1>();
		//Activity1 ac = new Activity1("def",new Date());
		//len = getLen(col);
		//System.out.println("len = "+len);
		//Document d = Document.parse((new JSONObject(ac).toString()));
		//System.out.println(d);
		//col.updateOne(new BasicDBObject("_id","name6"),new BasicDBObject("$set", new BasicDBObject("activities", "acfd")) );
		//System.out.println(jo.toString());
		//col.insertOne(Document.parse(do1.toString()));
		mongoClient.close();
		
	}
	static int len = 0;
	private static int getLen (MongoCollection<Document> col) {
		
		FindIterable<Document> iterable =col.find(new BasicDBObject("_id","name6"));
		
		iterable.forEach(new Block<Document>() {

			public void apply(Document t) {
				System.out.println(((List)(t.get("activities"))).size());
				len = ((List)(t.get("activities"))).size();
			}
		});
		return len;
		
	}
	private static TestDBObject createDbObject(String login) {
		TestDBObject curObj = new TestDBObject(login);
		int len = (int) (Math.random()*10);
		for (int i = 0; i < len; ++i) {
			Activity1 ac = new Activity1("act"+(i+1), new Date(System.currentTimeMillis()-(long)(Math.random()*100000)-(long)10000));
			curObj.addActivity(ac);
		}
		
		return curObj;
	}
	private static void createdbinstance() {
		System.out.println("Creating Mongodb instance");
		String databaseName = Configurations.CURRENT_DB_NAME;
		
		try {
			System.out.println("Creating Client...");
			mongoClient = new MongoClient(Configurations.DB_SERVER_HOST, Configurations.DB_SERVER_PORT );
			System.out.println("Get Database...");
			db = mongoClient.getDatabase(databaseName);
		} catch (Exception e) {
			return;
		}
	}

}
