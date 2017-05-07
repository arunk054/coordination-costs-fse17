package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;



public class IssueCommentsMinerPeriodic extends TimeBasedMiner {

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_ISSUES_COMMENTS;
	protected static  String PARAMS = "state=all&sort=updated&direction=asc";
	
	public IssueCommentsMinerPeriodic(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA);
		super.setParamAndCreateEndpointURL(PARAMS);
	}
	public IssueCommentsMinerPeriodic(String owner, String repo, boolean isWriteIfCollectionExists, String sinceParam) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, sinceParam);
		super.setParamAndCreateEndpointURL(PARAMS);
	}
}
