package cmu.isr.arunkaly.miners;

import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.dblayer.DBInterface;

public class UsersMiner extends GenericRepoMiner{

	protected static String ENDPOINT_NAME = EndPointNames.ENDPOINT_USERS;
	private String userName;
	
	public UsersMiner(String owner, String repo, String userName, boolean isWriteIfCollectionExists) {
		super(owner, repo,ENDPOINT_NAME,isWriteIfCollectionExists);
		this.userName = userName;
		createEndPointURL();
	}
	public void createEndPointURL() {
		//TODO Add param : type = all if we want more repos. Default: type = owner
		this.endpointURL = Constants.GITHUB_API_URL+this.endpointName+"/" +this.userName+"?"+this.params;
		this.ghAPI = new GithubAPICore(this.endpointURL, this.endpointName,clientIdIndex);
	}
	public boolean invokeAndWrite(DBInterface databaseController) {
		return super.invokeAndWrite(databaseController, true);
	}
}
