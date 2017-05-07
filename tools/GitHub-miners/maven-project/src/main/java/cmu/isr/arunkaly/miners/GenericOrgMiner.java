package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class GenericOrgMiner extends GenericGithubMiner {

	public GenericOrgMiner(String org, String endpointName,
			boolean isWriteIfCollectionExists) {
		super(endpointName, isWriteIfCollectionExists);
		this.org = org;
		createEndPointURL();
		
	}

	public static final String BASE_ORG_URL = Constants.GITHUB_API_URL+"orgs/";
	protected String org;
	
	@Override
	protected void createEndPointURL() {
		this.endpointURL = BASE_ORG_URL+this.org+((this.endpointName.isEmpty())?"":"/"+this.endpointName)+"?"+this.params;
		this.ghAPI = new GithubAPICore(this.endpointURL, this.endpointName,clientIdIndex);
	}

	@Override
	public String getCollectionName() {
		// TODO Auto-generated method stub
		return MongoDBLayer.getCollectionName(this.org,"",this.endpointName);
	}

}
