package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;

public class StarsMiner extends GenericRepoMiner{

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_STARS;
	
	public StarsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists);
	}
}
