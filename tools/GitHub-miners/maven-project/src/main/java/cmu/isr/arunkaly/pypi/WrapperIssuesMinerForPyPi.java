package cmu.isr.arunkaly.pypi;

import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.miners.ContributorsMiner;
import cmu.isr.arunkaly.miners.GenericRepoMiner;
import cmu.isr.arunkaly.miners.IssueCommentsMiner;
import cmu.isr.arunkaly.miners.IssuesMiner;
import cmu.isr.arunkaly.miners.RepositoryMiner;


public class WrapperIssuesMinerForPyPi {



	
	private String repo;
	private String owner;
	private boolean isWriteIfCollectionExists;
	private List<GenericRepoMiner> genericDataMiners;

	public WrapperIssuesMinerForPyPi(String owner, String repo) {
		this(owner,repo,false);
	}
	public WrapperIssuesMinerForPyPi(String owner, String repo, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo=repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
		this.genericDataMiners = getListOfDataMiners();
	}
	//Ideally we want to return status
	public void invokeAllMiners(DBInterface databaseController) {
			for (GenericRepoMiner dataMiner: genericDataMiners) {
				if (!dataMiner.invokeAndWrite(databaseController)) {
					System.out.println("Error mining "+dataMiner.getCollectionName()+ " for: "+owner+"/"+repo);
				}
			}
	}	
	
	private List<GenericRepoMiner> getListOfDataMiners() {
		List<GenericRepoMiner> list = new ArrayList<GenericRepoMiner>();
		list.add(new IssuesMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		list.add(new IssueCommentsMiner(this.owner,this.repo,this.isWriteIfCollectionExists));
		
		return list;
	}


}
