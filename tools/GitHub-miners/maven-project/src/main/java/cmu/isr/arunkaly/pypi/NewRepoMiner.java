package cmu.isr.arunkaly.pypi;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.miners.GenericRepoMiner;

public class NewRepoMiner extends GenericRepoMiner{

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_REPOSITORY;
	
	public NewRepoMiner(String owner, String repo, String endpointName,
			boolean isWriteIfCollectionExists) {
		super(owner, repo, endpointName, isWriteIfCollectionExists);
		// TODO Auto-generated constructor stub
	}

	public boolean invokeAndWrite(DBInterface databaseController) {
		return super.invokeAndWrite(databaseController, true);
	}
}
