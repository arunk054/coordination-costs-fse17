package cmu.isr.arunkaly.dblayer;

import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.client.MongoCollection;

public class TemporaryCacheImpl implements TemporaryCache{

	private String collectionName;

	public TemporaryCacheImpl(String collectionName) {
		this.collectionName = collectionName;
	}
	//returns false if already exists
	public boolean addToCache(String key, boolean isRetryAgain) {
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(collectionName);
		if (col != null) {
			JSONObject jo = new JSONObject();
			jo.put("_id", key);
			jo.put("isRetry",isRetryAgain);
			try {
				col.insertOne(Document.parse(jo.toString()));
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public boolean isExistsInCache(String key) {
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(collectionName);
		if (col == null)
			return false;
		return MongoDBLayer.getInstance().getDocument(key, col)!= null;
	}

	

	public void removeCache() {
		MongoDBLayer.getInstance().removeCollection(collectionName);
	}
	public void createNewCache(boolean deleteIfExists) {
		if (deleteIfExists)
			removeCache();
		
		MongoDBLayer.getInstance().getCollection(collectionName);
	}
	public boolean isExistsAndNoRetry(String key) {
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(collectionName);
		if (col == null)
			return false;
		Document doc = MongoDBLayer.getInstance().getDocument(key, col);
		if (doc == null)
			return false;
		return !doc.getBoolean("isRetry");
	}

}
