package cmu.isr.arunkaly.miners;

import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.Activity;
import cmu.isr.arunkaly.dblayer.GithubUser;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.dblayer.ReposExtracter;
import cmu.isr.arunkaly.dblayer.Repository;
import cmu.isr.arunkaly.dblayer.TemporaryCache;
import cmu.isr.arunkaly.dblayer.UniqueUsersExtracter;

public class CodeSearchForPython {

	private String owner;
	private String repo;
	private String upstreamPackageName;
	private TemporaryCache tempCache;

	public CodeSearchForPython(String owner, String repo, String upstreamPackageName, TemporaryCache tempCache) {
		this.owner = owner;
		this.repo = repo;
		this.upstreamPackageName = upstreamPackageName;
		this.tempCache = tempCache;
	}
	
	public void performCodeSearchForAllUsers() {
		String usersCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
		final MongoCollection<Document> usersCollection = MongoDBLayer.getInstance().getCollection(usersCollectionName);
		FindIterable<Document> iterable = usersCollection.find();
		iterable.forEach(new Block<Document>() {

			public void apply(Document doc) {
				String login = doc.getString("_id");
				if (tempCache!= null && tempCache.isExistsInCache(login)) {
					MyLogger.log("Skipping User:"+login+ " - Code search already performed.", LogLevel.DEBUG);
					return;
				}
				String[] searchTerms = getSearchQueryForUser(login);
				boolean isAPIError = false;
				for (String searchTerm : searchTerms) {
					SearchAPIMiner searchMiner = new SearchAPIMiner(searchTerm, owner, repo, EndPointNames.ENDPOINT_SEARCH_CODE, true);
					int count = searchMiner.invokeOnce();
					if (count == -1) {
						isAPIError = true;
						break;
					} else if (count > 0) {
						Document userDoc = MongoDBLayer.getInstance().getDocument(login, usersCollection);
						GithubUser currentUser = GithubUser.getObject(userDoc);
						MyLogger.log("Updating Reuse in commit for User: "+login,LogLevel.DEBUG);
						Activity ac = new Activity(ActivityNames.REUSE_IN_COMMIT, null);
						ac.setReuseCount(count);
						currentUser.addActivity(ac);
						UniqueUsersExtracter.updateToDB(usersCollection, currentUser);
						break;
					}
				}
				tempCache.addToCache(login, isAPIError);
			}
		});
	}
	private String[] getSearchQueryForUser(String login) {
		return getPossiblePackageImports("+user:"+login + "+in:file");
	}
	//TODO this is still Incomplete
	public void performCodeSearchForAllUserRepos() {
		
		final Map<String, Set<Repository>> mapOfUserRepos = ReposExtracter.populateUserReposMap(owner, repo);
		String usersCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
		MongoCollection<Document> usersCollection = MongoDBLayer.getInstance().getCollection(usersCollectionName);
		FindIterable<Document> iterable = usersCollection.find();
		iterable.forEach(new Block<Document>() {

			public void apply(Document doc) {
				String login = doc.getString("_id");
				Set<Repository> repos = mapOfUserRepos.get(login);
				if (repos == null)
					return;
				for (Repository repoObj  : repos) {
					String[] searchTerms = getSearchQuery(repoObj.getFullName());
					for (String searchTerm : searchTerms) {
						SearchAPIMiner searchMiner = new SearchAPIMiner(searchTerm, owner, repo, EndPointNames.ENDPOINT_SEARCH_CODE, true);
						if (searchMiner.invokeOnce() > 0) {
							
							//getUser
							//Add new Activity
							//update User
							
							break;
						}
					}
				}
				
			}
		});
		
		
		//if the search returns something (so dont have to search through all pages)
		//set the reuse flag in the unique Users collection and add other attributes
	}
	
	private String[] getSearchQuery(String repoFullName) {
		return getPossiblePackageImports("+repo:"+repoFullName + "+in:file");
	}
	private String[] getPossiblePackageImports(String suffixTerm) {
		String[] arr = new String[1];
		arr[0] = this.upstreamPackageName+suffixTerm;
		//the problem with these is that Python allows to import modules comma separated
		//Therefore we could have import abc,scipy => so if our search would miss this
		//To avoid this we just search for the packageName - will lead to lot of false Positives : deal with it later
		//arr[1] "\"from "+this.upstreamPackageName+"\"";
		//arr[2] "\"import "+this.upstreamPackageName+"\"";
		return arr;
	}
}
