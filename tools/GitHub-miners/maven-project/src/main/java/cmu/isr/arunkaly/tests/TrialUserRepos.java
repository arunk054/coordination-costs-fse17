package cmu.isr.arunkaly.tests;

import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.miners.RepoElement;
import cmu.isr.arunkaly.miners.UserReposMiner;

public class TrialUserRepos {

	
	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		
		//RepoElement repoElement = new RepoElement("SciRuby", "nmatrix");
		RepoElement repoElement = new RepoElement("scipy", "scipy");
		List<String> usersList = new ArrayList<String>();
		usersList.add("rtreharne");
		usersList.add("sujithvm");
		
		for (String user: usersList) {
			UserReposMiner uminer = new UserReposMiner(repoElement.getOwner(), repoElement.getRepo(), user, true);
			uminer.invokeAndWrite(MongoDBLayer.getInstance());
		}
		MongoDBLayer.getInstance().closeDB();
	}
}
