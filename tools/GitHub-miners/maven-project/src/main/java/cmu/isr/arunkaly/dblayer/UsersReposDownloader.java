package cmu.isr.arunkaly.dblayer;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.miners.UserReposMiner;

public class UsersReposDownloader {

	private String owner;
	private String repo;

	public UsersReposDownloader(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
	}
	
	public void downloadAndWriteReposOfUsers() {
		//get all users
		String usersCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
		MongoCollection<Document> usersCollection = MongoDBLayer.getInstance().getCollection(usersCollectionName);
		FindIterable<Document> iterable = usersCollection.find();
		iterable.forEach(new Block<Document>() {
//TODO: Implement TemporaryCache instead or writing every record
			public void apply(Document doc) {
				String login = doc.getString("_id");
				UserReposMiner repoMiner = new UserReposMiner(owner, repo, login, true);
				//int retry = 0;
				int retry = Constants.MAX_RETRIES_PER_REPO - 1;
				//This will overwrite several times but its ok.. for a given user we will have multiple repos
				while(retry < Constants.MAX_RETRIES_PER_REPO && repoMiner.invokeAndWrite(MongoDBLayer.getInstance()) == false) {
					retry++;
					MyLogger.logError("ERROR Downloading Repos for User "+login+" Retry : "+retry);
				}
			}
		});
		
		
		
	}
}
