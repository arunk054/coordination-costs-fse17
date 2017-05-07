package cmu.isr.arunkaly.miners;

import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public abstract class GenericGithubMiner {


	
	protected String endpointURL;
	protected String params;  
	protected String endpointName;
	protected boolean isWriteIfCollectionExists;
	protected GithubAPICore ghAPI;
	protected int clientIdIndex;
	private JSONObject lastJSONObj;
	
	public GenericGithubMiner( String endpointName, boolean isWriteIfCollectionExists) {
		this.clientIdIndex = Configurations.getNextClientIndex();
		this.params= "client_id=" + Configurations.CLIENT_IDS[this.clientIdIndex] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[this.clientIdIndex];
		this.endpointName = endpointName;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
	}
	protected abstract void createEndPointURL();
	
	public void setParamAndCreateEndpointURL(String newParams) {
		if (newParams != null && !newParams.isEmpty()) {
			this.params+="&"+newParams;
		}
		createEndPointURL();
	}

	
	public boolean invokeAPI(boolean isNonArrayResponse) {
		MyLogger.log("Downloading : "+endpointURL);
		return ghAPI.invokeEndpointUntilEnd(isNonArrayResponse,false, Constants.MAX_PAGES_PER_BATCH_WRITE);
	}
	public boolean invokeAPIUntilEnd(boolean isNonArrayResponse) {
		MyLogger.log("Downloading : "+endpointURL);
		return ghAPI.invokeEndpointUntilEnd(isNonArrayResponse,false);
	}
	
	public boolean writeToDB(DBInterface databaseController) {
		MyLogger.log("Writing to DB : "+getCollectionName());
		boolean returnVal = databaseController.writeRecords(ghAPI.getResponseJSONArray(), this.getCollectionName());
		if (ghAPI.getResponseJSONArray() != null && ghAPI.getResponseJSONArray().length() > 0)
			this.lastJSONObj = ghAPI.getResponseJSONArray().getJSONObject(ghAPI.getResponseJSONArray().length()-1);
		else
			this.lastJSONObj = null;
		return returnVal;
	}
	
	public JSONObject getLastJSONObj() {
		return lastJSONObj;
	}
	public void setLastJSONObj(JSONObject lastJSONObj) {
		this.lastJSONObj = lastJSONObj;
	}
	public abstract String getCollectionName();

	public boolean invokeAndWrite(DBInterface databaseController, boolean isNonArrayResponse) {
		
		//Check if we should invoke this or not
		if (!this.isWriteIfCollectionExists && databaseController.isExists(this.getCollectionName())) {
			MyLogger.log("Skipping : "+endpointName + " already exists!");
			return true;
		}
		
		do {
			if (!invokeAPI(isNonArrayResponse) || !writeToDB(databaseController)) {
				MyLogger.log("ERROR: API: "+ this.endpointURL,LogLevel.ERROR);
				return false;
			}
			ghAPI.clearResponseArray();
				
		}while (!ghAPI.isComplete());
		
		return true;
	}

	public boolean invokeAndWrite(DBInterface databaseController) {
		return invokeAndWrite(databaseController, false);
	}
}
