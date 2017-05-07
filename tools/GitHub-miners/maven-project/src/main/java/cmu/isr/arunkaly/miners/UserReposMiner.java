package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class UserReposMiner extends GenericRepoMiner{

	private String userName;
	public static final String BASE_USERS_URL = Constants.GITHUB_API_URL+"users/";
	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_REPOS;
	
	//TODO Add param : type = all if we want more repos. Default: type = owner 
	public UserReposMiner(String owner, String repo, String userName, boolean isWriteIfCollectionExists) {
		super(owner,repo,ENDPOINT_NAME, isWriteIfCollectionExists);
		this.userName = userName;
		createEndPointURL();
	}
	
	public void createEndPointURL() {
		//TODO Add param : type = all if we want more repos. Default: type = owner
		this.endpointURL = BASE_USERS_URL+this.userName+"/"+this.endpointName+"?"+this.params;
		this.ghAPI = new GithubAPICore(this.endpointURL, this.endpointName,clientIdIndex);
	}

	@Override
	public String getCollectionName() {
		return MongoDBLayer.getCollectionName(owner, repo, EndPointNames.USER_REPOS_COLLECTION_PREFIX+endpointName);
	}

}
