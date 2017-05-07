package cmu.isr.arunkaly.tests;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.dblayer.UsersDownloader;
import cmu.isr.arunkaly.dblayer.UsersReposDownloader;
import cmu.isr.arunkaly.miners.RepoElement;

public class TrialUsersProfiles {

	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		RepoElement repoElement = new RepoElement("scipy", "scipy");
		//remove the collection
		
		UsersDownloader ud = new UsersDownloader(repoElement.getOwner(), repoElement.getRepo());
		ud.downloadAndWriteUserProfiles();
		MongoDBLayer.getInstance().closeDB();
	}
}
