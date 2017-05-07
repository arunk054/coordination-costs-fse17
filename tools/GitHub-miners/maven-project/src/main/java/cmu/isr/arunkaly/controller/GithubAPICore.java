package cmu.isr.arunkaly.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;


/*
 * This class does the basic API invocation 
 */
public class GithubAPICore {

	//Construct the URL

	//Each one runs in a separate thread and writes the records to the DB
	//The thread never ends until it gets all records for this API call 

	//Invoke the API and get all the records
	//For each invocation check for rate limit
	//Get rate limit end time and create a wait till then
	//If starting request, then get the rate limit of the next account from the array of client id and secret
	//Dont forget to change the header accordingly - Have an array of headers as well.

	private RateLimitType rateLimitType;
	private String endpointURL ;
	public String getEndpointName() {
		return endpointName;
	}

	public JSONArray getResponseJSONArray() {
		return responseJSONArray;
	}

	public Map<String, List<String>> getCurrentHeaders() {
		return currentHeaders;
	}


	private String endpointName;
	private JSONArray responseJSONArray;
	//the headers of the last request call.
	private Map<String, List<String>> currentHeaders;
	private int maxRetries;
	

	//endpointSuffix contains the name of repo and the endpointName, and the URL params 
	//We need this second param to keep track of which collection in DB should we put this into
	//we might remove this endpointName in future
	public GithubAPICore(String endpointURL, String endpointName,int currentClientIndex) {

		this.endpointName=endpointName;
		this.endpointURL = endpointURL;
		this.responseJSONArray = new JSONArray();
		
		//create a new client id index and use it for the rest of the API
		this.currentClientIndex = currentClientIndex;
		
		this.rateLimitType = RateLimitType.CORE;
		this.maxRetries = Constants.MAX_GITHUB_API_RETRIES;
		this.bufferWaitTime = (long)5;
	}
	
	public long getBufferWaitTime() {
		return bufferWaitTime;
	}

	public void setBufferWaitTime(long bufferWaitTime) {
		this.bufferWaitTime = bufferWaitTime;
	}

	public void setRateLimitType(RateLimitType newType) {
		this.rateLimitType = newType;
	}
	private int currentClientIndex = 0;

	private int pageCounter = 1;
	private int maxPage = 0;
	private long bufferWaitTime;
	
	public boolean isComplete() {
		return (pageCounter>maxPage);
	}

	public boolean invokeEndpointUntilEnd(boolean isNonArrayResponse, boolean skipRateLimit) {
		
		do {
			if (!invokeEndpointUntilEnd(isNonArrayResponse, skipRateLimit, Constants.MAX_PAGES_PER_BATCH_WRITE))
				return false;
			
		}while (!isComplete());
		return true;
	}
	public boolean invokeEndpointUntilEnd(boolean isNonArrayResponse, boolean skipRateLimit, int maxLimitPerCall) { 

		maxLimitPerCall+=pageCounter;
		int retries = 0;
		do {
			long nextInvocationAt = 0;
			while (!skipRateLimit && nextInvocationAt != -1){
				nextInvocationAt = rateLimitInvokeAt();
				//Just sleep this thread until the next invocation
				if (nextInvocationAt != -1) {
					MyLogger.log("Rate Limit exceeded so sleeping for (seconds): "+ nextInvocationAt );
					
					try {
						Thread.sleep(nextInvocationAt*(long)1000);
						MyLogger.log("Coming out of sleep...");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						MyLogger.log("Sleep interrupted.. trying again");
					}
				}			
			}
			String newURL = this.endpointURL;
			if (!isNonArrayResponse)
				newURL+= getPageCounterParam(pageCounter);

			//indesx: 0 is response, and 1 is the header 
			Response response = null;
			try {
				//if (!skipRateLimit)
				response = getResponse(newURL);

			} catch (IOException e) {

				MyLogger.log("Error receiving response for "+endpointName+" : "+e.getMessage()+ ": "+e.toString(),LogLevel.ERROR);
				retries++;
				MyLogger.log("Retry "+retries + " Max retries : "+this.maxRetries);
				if (retries <= this.maxRetries) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				} else {
					MyLogger.log(" Exiting : Max retries exceeded: "+this.endpointURL,LogLevel.ERROR);
					return false;
				}
			}
			currentHeaders = response.getHeaders();

			if (isNonArrayResponse) {
				responseJSONArray.put(new JSONObject(response.getResponse()));
				break;
			} else  {
				JSONArray curResponse = new JSONArray(response.getResponse());
				MyLogger.log("Page: "+pageCounter + " Response Array size: "+curResponse.length(), LogLevel.DEBUG);
				
				//Add response to existing jsonArray
				for(Iterator<Object> iterator = curResponse.iterator();iterator.hasNext();) {
					responseJSONArray.put(iterator.next());
				}
			}		
//			if (maxPage == -1) {
//				maxPage = getLinkHeaderLastPage(response.getHeaders());
//				MyLogger.log("***Maximum num of pages for : "+endpointName +" is "+maxPage,LogLevel.INFO);
//				//This is a hack because even if github api gives the last page it does not work if it is more thatn around 1200.
//				//TD investigate later
//				//if (maxPage > Constants.MAX_PAGE_LIMIT) {
//				//	maxPage = Constants.MAX_PAGE_LIMIT;
//				//	MyLogger.log("**Maximum num of pages after Github hack : is "+maxPage,LogLevel.INFO);
//				//}
//			
//			}
			
			pageCounter++;

			//Even if we have a lot of pages Github limits to about 1300 pages of data that you can parse.
			if (Constants.DO_NOT_CONTINUE.equals(getLinkHeaderRel(response.getHeaders()))) {
				maxPage = pageCounter - 1;//just a hack to reset isComplete
				break;
			} else if (pageCounter == maxLimitPerCall || (maxPage != 0 && pageCounter > maxPage)) {
				break;
			} 
			

			//Sleep for a few milli seconds before next API call - just to not overwhelm the Github server
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}while(true);

		MyLogger.log("Call ended at Page: "+pageCounter + " : records downloaded: "+responseJSONArray.length(), LogLevel.DEBUG);
		return true;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	private String getPageCounterParam(int pageCounter) {
		return "&per_page="+Constants.MAX_PER_PAGE+"&page="+pageCounter;
	}

	private Response getResponse(String url) throws IOException {
		MyLogger.log("*** Invoking API: "+url,LogLevel.DEBUG);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Study Upstream Downstream CMU");

		int responseCode = con.getResponseCode();
		

		//throw an exception if error code
		if (responseCode != 200) {
			int MAX=256;
			char[] errorChar = new char[MAX];
			int count = 0;
			if (con.getErrorStream()!=null) {
				InputStreamReader ir = new InputStreamReader(con.getErrorStream());
				count = ir.read(errorChar,0,MAX);
			}
			throw new IOException("Error response code "+responseCode + " "+String.valueOf(errorChar,0,(count<=0)?1:count));
		}

		//read response
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));

		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseBuffer.append(inputLine);			
		}

		return new Response (responseBuffer.toString(),con.getHeaderFields());

	}

	private String getLinkHeaderRel(Map<String, List<String>> headerFields) {
		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			if ("Link".equalsIgnoreCase(entry.getKey())) {
				MyLogger.log("Link header found "+entry.getValue(),LogLevel.DEBUG);
				List<String> listVal = entry.getValue();
				String relStr = "rel=";
				if (listVal!=null && listVal.size()>0) {
					String strVal = listVal.get(0);
					int indexOfRel = 0;
					int prevIndexOfRel = 0;
					boolean foundValid = false;
					while((indexOfRel = strVal.indexOf(relStr, indexOfRel)) != -1) {
						indexOfRel += relStr.length()+1;
						String strNextRel = strVal.substring(indexOfRel, strVal.indexOf('\"',indexOfRel));
						if (isValidString(strNextRel,Constants.LINK_REL_VALID_HEADERS)) {
							foundValid = true;
						}	
						if (maxPage == 0 && strNextRel.contains("last")){
							String pageStr = "&page=";
							try {
								int startIndex = strVal.indexOf(pageStr,prevIndexOfRel)+pageStr.length();
								int endIndex = strVal.indexOf('>',startIndex);
								maxPage = Integer.parseInt(strVal.substring(startIndex,endIndex));
								//This is sort of a hack
								if (maxPage > Constants.MAX_PAGE_LIMIT ) {
									MyLogger.log("Possible ERROR due to large number of pages "+maxPage+" hence setting it to 400 for: "+endpointURL);
									maxPage = Constants.MAX_PAGE_LIMIT;//Store this value in a constant
								}
								MyLogger.log("Found Last Page = "+maxPage);
							}catch (Exception e) {
								
							}
						}
						prevIndexOfRel = indexOfRel;
					}
					if (foundValid)
						return Constants.CONTINUE;
				}
			}
		}
		return Constants.DO_NOT_CONTINUE;
	}

/*	private int getLinkHeaderLastPage(Map<String, List<String>> headerFields) {
		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			System.out.println("Header field "+entry.getKey() + " "+entry.getValue());
			try {
				if ("Link".equalsIgnoreCase(entry.getKey())) {
					List<String> listVal = entry.getValue();
					if (listVal!=null && listVal.size()>0) {
						String strVal = listVal.get(0);
						String pageStr = "&page=";
						int indexOfLastPage = strVal.lastIndexOf(pageStr);
						if (indexOfLastPage != -1) {
							String lastPageVal = strVal.substring(indexOfLastPage + pageStr.length(), strVal.indexOf('>',indexOfLastPage));;
							return Integer.parseInt(lastPageVal);
						}
					}
				}
			}catch(Exception e) {
				//do nothing but continue
				MyLogger.log("error parsing headers: "+e,LogLevel.ERROR);
				//e.printStackTrace();
			}
		}
		return Constants.MAX_PAGE_LIMIT;
	}*/

	private boolean isValidString(String strNextRel, String[] linkRelValidHeaders) {
		for (int i = 0; i < linkRelValidHeaders.length; i++) {
			if (strNextRel.contains(linkRelValidHeaders[i])) {
				return true;
			}
		}
		return false;
	}

	//returns -1 if no rate limit exceeded, else the time when you should invoke again.
	private long rateLimitInvokeAt() {
		GithubRateLimitInvoker ghRateLimit = new GithubRateLimitInvoker(this.currentClientIndex, this.rateLimitType);
		ghRateLimit.invokeRateLimitCheck();
		int remaining = ghRateLimit.getRemainingRateLimit();
		/*if (remaining < Constants.RATE_LIMIT_MIN_THRESHOLD*2) {
			MyLogger.log("Remaining rate limit : " + remaining);
		}*/

		if (!ghRateLimit.isRateLimitExceeded())
			return -1;
		long nextResetTime = ghRateLimit.getNextResetTime();
		if (nextResetTime <= 0) {
			return -1;
		}
		long curTimeSecs = System.currentTimeMillis() /(long)1000;

		MyLogger.log("next invoke at: "+new Date(nextResetTime*(long)1000));
		//Add some buffer wait time
		long waitTime = nextResetTime - curTimeSecs + this.bufferWaitTime; 
		return waitTime;
		//return the wait time in secs.

	}

	public void clearResponseArray() {
		responseJSONArray = new JSONArray();
	}


}
