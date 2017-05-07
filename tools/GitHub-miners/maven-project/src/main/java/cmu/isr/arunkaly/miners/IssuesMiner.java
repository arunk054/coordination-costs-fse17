package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;



public class IssuesMiner extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_ISSUES;
	protected static  String PARAMS = "state=all";
	
	public IssuesMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
		super.setParamAndCreateEndpointURL(PARAMS);
	}
}
