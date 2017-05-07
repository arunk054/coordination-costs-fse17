package cmu.isr.arunkaly.dblayer;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.BSON;
import org.bson.Document;
import org.json.JSONArray;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.miners.RepoElement;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBLayer implements DBInterface{
	protected static final String REPOSITORY_URL_FIELD = "URL";
	private String databaseName;
	private MongoDatabase db;
	private MongoClient mongoClient;
	private static MongoDBLayer currentInstance;

	//just single instance for now.
	private MongoDBLayer() {
		System.out.println("Creating Mongodb instance");
		this.databaseName = Configurations.CURRENT_DB_NAME;
		
		try {
			System.out.println("Creating Client...");
			MongoClientOptions options= MongoClientOptions.builder().socketKeepAlive(true).build();
			this.mongoClient = new MongoClient(new ServerAddress(Configurations.DB_SERVER_HOST, Configurations.DB_SERVER_PORT),options);
			System.out.println("Get Database...");
			this.db = mongoClient.getDatabase(this.databaseName);
		} catch (Exception e) {
			return;
		}
	}

	public boolean isRunning () {
		if (this.mongoClient==null)
			return false;
		try {
			
			this.mongoClient.getAddress();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	public static MongoDBLayer getInstance() {
		if (currentInstance != null )
			return currentInstance;
		currentInstance = new MongoDBLayer();
		return currentInstance;
	}

	private void closeConnection() {
		if (mongoClient!=null)
			this.mongoClient.close();
	}

	public static String getCollectionName(String owner, String repo, String endpointName) {
		
		return owner+Constants.OWNER_REPO_SEPARATOR+repo+Constants.REPO_ENDPOINT_SEPARATOR+endpointName.replaceAll("/", "__");
	}


	
	public boolean writeRecords(JSONArray records, String collectionName) {
		//this will create collection if it does not exist
		//We are doing this before checking exists because db.collectionExists is not working
		MongoCollection<Document> table = db.getCollection(collectionName);
		
		/*if (!overwriteIfExists) {
			//boolean collectionExists = db.collectionExists(collectionName);
			//the above function not working so we are using the count
			if (this.isExists(collectionName)) {
				MyLogger.log("Collection "+collectionName + " already exists. Skipping write...");
				return false;
			}
		}*/
		MyLogger.log("Writing to "+collectionName,LogLevel.DEBUG);
		//
		
		int len = records.length();
		//required when len = 0
		//if (len == 0)
		if (len == 0 && !this.isExists(collectionName))
			db.createCollection(collectionName);
		int batchSize = 1000;
		ArrayList<Document> listOfDocs = new ArrayList<Document>();
		for (int i = 0; i < records.length(); i++) {
			if ((i+1) % batchSize == 0)
			{
				insertDocuments(table,listOfDocs);
				listOfDocs = new ArrayList<Document>();
			}
			Document dbObj =  Document.parse(records.get(i).toString());
			listOfDocs.add(dbObj);
		}
		insertDocuments(table,listOfDocs);
		System.out.println("records written "+len + " Current size : "+table.count());
		return true;
	}
	
	public boolean isExists(String collectionName) {
		//MongoCollection<Document> table = db.getCollection(collectionName);
		//return (table.count()>0)?true:false;
		for (String name: db.listCollectionNames()) {
			if (name.equals(collectionName))
				return true;
		}
		return false;
		
	}

	public void insertOne(MongoCollection<Document> collection, Document document) {
		if (document == null )
			return;
		collection.insertOne(document);
	}

	public void insertDocuments(MongoCollection<Document> collection, List<Document> documents) {
		if (documents == null || documents.isEmpty())
			return;
		collection.insertMany(documents);
	}

	public List<Document> getDocuments(String collectionName) {
		FindIterable<Document> iterable = db.getCollection(collectionName).find();

		final List<Document> returnList = new ArrayList<Document>();
		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {

				try {
					returnList.add(document);

				} catch (Exception e) {
					//Just skip
				}
			}
		});
		return returnList;

	}


	public void closeDB() {
		closeConnection();
		
	}
	public void dropCollection (String owner, String repo, String endpoint) {
		String collectionName = getCollectionName(owner,repo,endpoint);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		if (collection!=null)
			collection.drop();
	}

	public MongoCollection<Document> getCollection(String collectionName) {
		return db.getCollection(collectionName);
	}

	public void permanentlyRemoveRepos(List<RepoElement> removedRepos) {
		for (RepoElement repoElem: removedRepos) {
			MyLogger.log("Remove all collections for Repo: "+repoElem);
			for (String endpoint: EndPointNames.ALL_COLLECTION_SUFFIX) {
				String collectionName = getCollectionName(repoElem.getOwner(),repoElem.getRepo(),endpoint);
				MongoCollection<Document> collection = db.getCollection(collectionName);
				if (collection!=null)
					collection.drop();
			}
			
		}
	}

	public void removeCollection(String collectionName ){
		MongoCollection<Document> col = getCollection(collectionName);
		if (col!=null)
			col.drop();
	}
	public Document getDocument(String key, MongoCollection<Document> col) {
		FindIterable<Document> iterable = col.find(new BasicDBObject("_id",key));
		//just return the first one
		if (!iterable.iterator().hasNext())
			return null;
		return iterable.iterator().next();
	}
	//TODO also set this the format when writing to db
	public static Date getDateFromString(String dateStr) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			return formatter.parse(dateStr);
		} catch (ParseException e) {
			MyLogger.log("Error parsing date: "+dateStr,LogLevel.ERROR);
		}
		return null;
	}

}
