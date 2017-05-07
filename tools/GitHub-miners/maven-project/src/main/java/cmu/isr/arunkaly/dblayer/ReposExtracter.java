package cmu.isr.arunkaly.dblayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.miners.UserReposMiner;


//Reads the DB and creates a map of userId and Set of Repository objects

public class ReposExtracter {

	public static Map<String,Set<Repository>> populateUserReposMap(String owner, String repo) 
	{
		String collectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.USER_REPOS_COLLECTION_PREFIX+EndPointNames.ENDPOINT_REPOS);
		MongoCollection<Document> reposCollection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = reposCollection.find();
		final Map<String,Set<Repository>> mapOfUserRepos = new HashMap<String, Set<Repository>>();
		
		iterable.forEach(new Block<Document>() {
			public void apply(Document doc) {
				Repository repoObj = Repository.getObject(doc);
				if (repoObj == null)
					return;
				Set<Repository> existingRepos =mapOfUserRepos.get(repoObj.getOwner());
				if (existingRepos == null){
					existingRepos = new HashSet<Repository>();
					mapOfUserRepos.put(repoObj.getOwner(),existingRepos);
				}
				existingRepos.add(repoObj);
			}
		});
		return mapOfUserRepos;
	}
	
	
}
