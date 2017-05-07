package cmu.isr.arunkaly.miners;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.controller.GithubAPICore;
import cmu.isr.arunkaly.controller.RateLimitType;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class SearchAPIMiner extends GenericRepoMiner {

	
	private String searchParam;
	
	
	public SearchAPIMiner(String searchQuery, String owner, String repo, String endpointName, boolean isWriteIfCollectionExists) {
		super(owner, repo, endpointName, isWriteIfCollectionExists);
		this.searchParam = "q="+searchQuery;
		super.setParamAndCreateEndpointURL(searchParam);
	}

	public void createEndPointURL() {
		this.endpointURL = Constants.GITHUB_API_URL+this.endpointName+"?"+this.params;
		this.ghAPI = new GithubAPICore(this.endpointURL, this.endpointName,clientIdIndex);
		this.ghAPI.setRateLimitType(RateLimitType.SEARCH);
		this.ghAPI.setMaxRetries(3);
		this.ghAPI.setBufferWaitTime(2);
	}
	public int invokeOnce() {
		//Jsut download one page of results
		ghAPI.invokeEndpointUntilEnd(true, false, 1);
		if (ghAPI.getResponseJSONArray().length() == 0)//failure of the API
			return -1;//so return -1
		return getTotalCount((JSONObject)ghAPI.getResponseJSONArray().get(0));
	}
	public int getTotalCount(JSONObject searchJSON) {
		int totalCount = 0;
		try {
			totalCount = searchJSON.getInt("total_count");
			System.out.println("Total Count: = "+totalCount);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return totalCount;
	}
}
