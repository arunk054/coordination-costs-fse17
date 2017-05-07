package cmu.isr.arunkaly.miners;

import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;


public class InvokeRepoWrapper {

	private List<RepoElement> repositoriesToMine;

	
	public InvokeRepoWrapper(List<RepoElement> repositoriesToMine) {
		// TODO Auto-generated constructor stub
		this.repositoriesToMine = repositoriesToMine;
	}
	
	public double[] startMiningAllRepos() {
		DBInterface databaseController = MongoDBLayer.getInstance();
		double[] timeForEachRepoInSeconds = new double[repositoriesToMine.size()];
		int i = 0;
		for (RepoElement repo: repositoriesToMine) {
			long beforeTime = System.currentTimeMillis();
			WrapperRepoMiner wrapperRepoMiner = new WrapperRepoMiner(repo.getOwner(), repo.getRepo());
			int retry = 0;
			do {
				MyLogger.log("");
				MyLogger.log(((retry>0)?"Retry ":"Try ") + (retry+1)+ " : Mining all data for : "+repo);
				
				wrapperRepoMiner.invokeAllMiners(databaseController);
				//check if all endpoints have been sucessfully mined
				retry++;
			}while(retry < Constants.MAX_RETRIES_PER_REPO && !wrapperRepoMiner.isAllEndPointsMined(databaseController));
			timeForEachRepoInSeconds[i++] = ((System.currentTimeMillis()-beforeTime)/(1000D*60));
			
		}
		return timeForEachRepoInSeconds;
	}


	
}
