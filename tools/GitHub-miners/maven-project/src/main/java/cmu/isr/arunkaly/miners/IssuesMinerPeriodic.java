package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;



public class IssuesMinerPeriodic extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_ISSUES;
	protected static  String PARAMS = "state=all&sort=updated&direction=asc";
	
	public IssuesMinerPeriodic(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
		super.setParamAndCreateEndpointURL(PARAMS);
	}
	public IssuesMinerPeriodic(String owner, String repo, boolean isWriteIfCollectionExists, String sinceParam) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, sinceParam);
		super.setParamAndCreateEndpointURL(PARAMS);
	}
}
