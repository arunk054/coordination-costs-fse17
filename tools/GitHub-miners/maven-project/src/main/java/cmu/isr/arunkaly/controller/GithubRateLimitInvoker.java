package cmu.isr.arunkaly.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.configs.Configurations;


public class GithubRateLimitInvoker {

	private String endpointURL;
	private Map<String, List<String>> headers;
	private int indexOfClient;
	private RateLimitType rateLimitType;
	private JSONObject responseJSON;
	
	public GithubRateLimitInvoker(int indexOfClient, RateLimitType rateLimitType) {
		this.indexOfClient = indexOfClient;
		String params = "client_id=" + Configurations.CLIENT_IDS[indexOfClient] + "&" + "client_secret=" + Configurations.CLIENT_SECRETS[indexOfClient];
		this.endpointURL  = Constants.GITHUB_API_URL+EndPointNames.ENDPOINT_RATE_LIMIT+"?"+params;
		this.rateLimitType = rateLimitType;
	}
	
	public void invokeRateLimitCheck() {
		GithubAPICore gAPI = new GithubAPICore(this.endpointURL, EndPointNames.ENDPOINT_RATE_LIMIT,indexOfClient);
		gAPI.invokeEndpointUntilEnd(true, true,Constants.MAX_PAGES_PER_BATCH_WRITE);
		headers= gAPI.getCurrentHeaders();
		 this.responseJSON = (JSONObject)gAPI.getResponseJSONArray().get(0);
	}
	
	public boolean isRateLimitExceeded() {
		if (this.rateLimitType == RateLimitType.SEARCH) {
			return isRateLimitExceededForSearch();
		}
		return isRateLimitExceededForCore();
	}
	
	private boolean isRateLimitExceededForSearch() {
		long remaining = 0;
		try {
			remaining = responseJSON.getJSONObject("resources").getJSONObject("search").getLong("remaining");
		} catch (JSONException e) {
			MyLogger.log("Unable to read Rate limit response: "+responseJSON.toString(),LogLevel.DEBUG);
		}
		return remaining <= 1;
	}

	private boolean isRateLimitExceededForCore() {
		long remaining = 0;
		try {
			remaining = responseJSON.getJSONObject("resources").getJSONObject("core").getLong("remaining");
		} catch (JSONException e) {
			MyLogger.log("Unable to read Rate limit response: "+responseJSON.toString(),LogLevel.DEBUG);
			return isRateLimitExceededWithHeaders();
		}
		return remaining <= Constants.RATE_LIMIT_MIN_THRESHOLD;
	}

	public boolean isRateLimitExceededWithHeaders(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Remaining".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						long remaining = Long.parseLong(val);
						if (remaining > Constants.RATE_LIMIT_MIN_THRESHOLD) {
							return false;
						} else {
							return true;
						}
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header  - remaining limit"+e);
						return false;
					}
				}
				return false;
			}
		}
		//did not find anything so no rate limit exceeded
		return false;
	}
	
	public long getNextResetTime(){
		if (this.rateLimitType == RateLimitType.SEARCH) {
			return getNextResetTimeForSearch();
		}
		return getNextResetTimeForCore();
	}
	
	private long getNextResetTimeForSearch() {
		long nextResetTime = -1;
		try {
			nextResetTime = responseJSON.getJSONObject("resources").getJSONObject("search").getLong("reset");
		} catch (JSONException e) {
			MyLogger.log("Unable to read Rate limit response reset time: "+responseJSON.toString(),LogLevel.DEBUG);
		}
		return nextResetTime;
	}

	private long getNextResetTimeForCore() {
		long nextResetTime = -1;
		try {
			nextResetTime = responseJSON.getJSONObject("resources").getJSONObject("core").getLong("reset");
		} catch (JSONException e) {
			MyLogger.log("Unable to read Rate limit response reset time: "+responseJSON.toString(),LogLevel.DEBUG);
			return getNextResetTimeWithHeaders();
		}
		return nextResetTime;
	}

	public long getNextResetTimeWithHeaders(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Reset".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						return Long.parseLong(val);
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header Reset time "+e);
						return -1;
					}
				}
				return -1;
			}
		}
		return -1;
	}
	
	public int getRemainingRateLimit(){
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String key = entry.getKey();
			
			if ("X-RateLimit-Remaining".equalsIgnoreCase(key)) {
				List<String> listVal = entry.getValue();
				if (listVal != null && listVal.size()>0){
					String val  = listVal.get(0);
					try {
						return Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Exception in parsing rate limit header Reset time "+e);
						return -1;
					}
				}
				return -1;
			}
		}
		return -1;
	}

	
}
