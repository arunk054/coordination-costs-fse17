package cmu.isr.arunkaly.tests;

import cmu.isr.arunkaly.dblayer.MongoDBLayer;
import cmu.isr.arunkaly.dblayer.TemporaryCacheImpl;
import cmu.isr.arunkaly.miners.CodeSearchForPython;
import cmu.isr.arunkaly.miners.RepoElement;

public class TrialCodeSearch {
	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		// RepoElement repoElement = new RepoElement("SciRuby", "nmatrix");
		RepoElement repoElement = new RepoElement("scipy", "scipy");
		String collectionName = MongoDBLayer.getCollectionName(
				repoElement.getOwner(), repoElement.getRepo(),
				"temp_code_search_users");
		TemporaryCacheImpl tempCache = new TemporaryCacheImpl(collectionName);
		// tempCache.removeCache();
		CodeSearchForPython codeS = new CodeSearchForPython(
				repoElement.getOwner(), repoElement.getRepo(), "scipy",
				tempCache);
		codeS.performCodeSearchForAllUsers();
		MongoDBLayer.getInstance().closeDB();
	}
}
