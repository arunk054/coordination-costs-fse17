package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.DBInterface;

public class RepositoryMiner extends GenericRepoMiner{

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_REPOSITORY;
	
	public RepositoryMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists);
	}
	
	public boolean invokeAndWrite(DBInterface databaseController) {
		return super.invokeAndWrite(databaseController, true);
	}
}
