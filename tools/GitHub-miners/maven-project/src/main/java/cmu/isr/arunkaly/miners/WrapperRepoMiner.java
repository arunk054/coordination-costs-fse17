package cmu.isr.arunkaly.miners;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;
/*
 * Invokes all repo miners for a given repo
 */
public class WrapperRepoMiner {

	
	private String repo;
	private String owner;
	private boolean isWriteIfCollectionExists;
	private List<GenericRepoMiner> genericDataMiners;

	public WrapperRepoMiner(String owner, String repo) {
		this(owner,repo,false);
	}
	public WrapperRepoMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo=repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
		this.genericDataMiners = getListOfDataMiners();
	}
	//Ideally we want to return status
	public void invokeAllMiners(DBInterface databaseController) {
			for (GenericRepoMiner dataMiner: genericDataMiners) {
				if (!dataMiner.invokeAndWrite(databaseController)) {
					//remove this collection and try again because it errored
					MongoDBLayer.getInstance().dropCollection(dataMiner.owner, dataMiner.repo, dataMiner.endpointName);
				}
			}
	}	
	
	private List<GenericRepoMiner> getListOfDataMiners() {
		List<GenericRepoMiner> list = new ArrayList<GenericRepoMiner>();
		list.add(new RepositoryMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new ContributorsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new ReleasesMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new IssuesMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new IssueCommentsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		list.add(new WatchersMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new StarsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new ForksMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		//list.add(new StatsContributorsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		//list.add(new StatsCommitActivityMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		//Skip for now since difference between owner and rest is not very useful
		//list.add(new StatsParticipationMiner(this.owner,this.repo,this.isWriteIfCollectionExists));

		//This is a big one
		list.add(new CommitsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		return list;
	}

	public boolean isAllEndPointsMined(DBInterface databaseController) {
		//Go through each miner
		for (GenericRepoMiner dataMiner: genericDataMiners) {
			if (!databaseController.isExists(dataMiner.getCollectionName()))
				return false;
		}
		return true;
	}
}

