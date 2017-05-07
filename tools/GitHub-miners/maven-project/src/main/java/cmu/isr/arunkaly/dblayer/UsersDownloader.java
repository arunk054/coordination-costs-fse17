package cmu.isr.arunkaly.dblayer;

import org.bson.Document;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.miners.UserReposMiner;
import cmu.isr.arunkaly.miners.UsersMiner;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class UsersDownloader {

	private String owner;
	private String repo;

	public UsersDownloader(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
	}
	public void downloadAndWriteUserProfiles() {
		String usersCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
		MongoCollection<Document> usersCollection = MongoDBLayer.getInstance().getCollection(usersCollectionName);
		FindIterable<Document> iterable = usersCollection.find();
		iterable.forEach(new Block<Document>() {

			public void apply(Document doc) {
				String login = doc.getString("_id");
				UsersMiner userMiner = new UsersMiner(owner, repo, login, true);
				//int retry = 0;
				int retry = Constants.MAX_RETRIES_PER_REPO - 1;
				//This will overwrite several times but its ok.. for a given user we will have multiple repos
				while(retry < Constants.MAX_RETRIES_PER_REPO && userMiner.invokeAndWrite(MongoDBLayer.getInstance()) == false) {
					retry++;
					MyLogger.logError("ERROR Downloading User Profile for "+login+" Retry : "+retry);
				}
			}

		});

	}
}
