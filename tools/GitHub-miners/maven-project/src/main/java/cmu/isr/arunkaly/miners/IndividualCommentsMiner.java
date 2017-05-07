package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class IndividualCommentsMiner extends GenericRepoMiner{

	public IndividualCommentsMiner(String owner, String repo, String endpointName,boolean isWriteIfCollectionExists) {
		super(owner, repo,endpointName,isWriteIfCollectionExists);
		
	}
	
	public String getCollectionName() {
		return MongoDBLayer.getCollectionName(this.owner,this.repo,EndPointNames.ENDPOINT_ISSUES+"/"+EndPointNames.ENDPOINT_COMMENTS);
	}
}
