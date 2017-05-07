package cmu.isr.arunkaly.tests;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.dblayer.UsersReposDownloader;
import cmu.isr.arunkaly.miners.RepoElement;
import cmu.isr.arunkaly.miners.UserReposMiner;

public class TrialAllUsersRepos {
	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		RepoElement repoElement = new RepoElement("scipy", "scipy");
		//remove the collection
		
		MongoDBLayer.getInstance().removeCollection(MongoDBLayer.getCollectionName(repoElement.getOwner(), repoElement.getRepo(), EndPointNames.USER_REPOS_COLLECTION_PREFIX+EndPointNames.ENDPOINT_REPOS));
		UsersReposDownloader urd = new UsersReposDownloader(repoElement.getOwner(), repoElement.getRepo());
		urd.downloadAndWriteReposOfUsers();
		MongoDBLayer.getInstance().closeDB();
	}
}
