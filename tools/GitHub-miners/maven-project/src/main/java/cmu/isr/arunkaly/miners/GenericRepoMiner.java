package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;



//super class of all miners
public class GenericRepoMiner extends GenericGithubMiner{
	


	public static final String BASE_REPO_URL = Constants.GITHUB_API_URL+"repos/";
	protected String repo;
	protected String owner;
	
	public GenericRepoMiner(String owner, String repo, String endpointName, boolean isWriteIfCollectionExists) {
		super(endpointName,isWriteIfCollectionExists);
		this.owner = owner;
		this.repo = repo;
		createEndPointURL();
	}
	public void createEndPointURL() {
		this.endpointURL = BASE_REPO_URL+owner+"/"+repo+((this.endpointName.isEmpty())?"":"/"+this.endpointName)+"?"+this.params;
		this.ghAPI = new GithubAPICore(this.endpointURL, this.endpointName,clientIdIndex);
	}
	
		
	public String getCollectionName() {
		return MongoDBLayer.getCollectionName(this.owner,this.repo,this.endpointName);
	}
	
}
