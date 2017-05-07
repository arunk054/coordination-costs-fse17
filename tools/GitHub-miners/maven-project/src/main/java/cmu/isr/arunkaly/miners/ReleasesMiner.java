package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;

public class ReleasesMiner extends GenericRepoMiner{

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_RELEASES;
	
	public ReleasesMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists);
	}
}
