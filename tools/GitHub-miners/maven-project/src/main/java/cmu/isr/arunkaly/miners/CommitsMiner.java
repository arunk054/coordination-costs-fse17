package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;



public class CommitsMiner extends TimeBasedUntilMiner {

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_COMMITS;
	
	public CommitsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, Configurations.NUM_YEARS_OF_DATA,-1);
	}
	
	public CommitsMiner(String owner, String repo, boolean isWriteIfCollectionExists, double timeSince, double timeUntil) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists, timeSince, timeUntil);
	}
	
	public CommitsMiner(String owner, String repo, boolean isWriteIfCollectionExists, String timeSince, String timeUntil) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists,-1,-1);
		super.setParamAndCreateEndpointURL("since="+timeSince);
		super.setParamAndCreateEndpointURL("until="+timeUntil);
		//Bad way of duplicating code
		dateISOSinceStr = timeSince;
		
	}
}
