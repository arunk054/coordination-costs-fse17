package cmu.isr.arunkaly.tests;

import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.dblayer.UniqueUsersExtracter;
import cmu.isr.arunkaly.miners.CommitsMiner;
import cmu.isr.arunkaly.miners.InvokeRepoWrapper;
import cmu.isr.arunkaly.miners.RepoElement;



public class Trial1 {

	
	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		
		//RepoElement repoElement = new RepoElement("SciRuby", "nmatrix");
		RepoElement repoElement = new RepoElement("scipy", "scipy");
		List<RepoElement> listOfRepos = new ArrayList<RepoElement>();
		listOfRepos.add(repoElement);
		//MongoDBLayer.getInstance().permanentlyRemoveRepos(listOfRepos);
		
		InvokeRepoWrapper iw = new InvokeRepoWrapper(listOfRepos);
		iw.startMiningAllRepos();
		
		UniqueUsersExtracter userExtracter = new UniqueUsersExtracter(repoElement);
		userExtracter.removeUsersActivitiesCollection();
		userExtracter.populateUniqueUsers();
		
		//System.out.println(MongoDBLayer.getInstance().getCollection("TestDB").count());
		MongoDBLayer.getInstance().closeDB();
	}
}
