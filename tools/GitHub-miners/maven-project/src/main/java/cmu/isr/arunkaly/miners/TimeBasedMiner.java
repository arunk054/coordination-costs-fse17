package cmu.isr.arunkaly.miners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class TimeBasedMiner extends GenericRepoMiner{

	String dateISOSinceStr  = "";
	public TimeBasedMiner(String owner, String repo, String endpointName,
			boolean isWriteIfCollectionExists, String timeSince) {
		super(owner, repo, endpointName, isWriteIfCollectionExists);
		super.setParamAndCreateEndpointURL("since="+timeSince);
		//Bad way of duplicating code
		dateISOSinceStr = timeSince;
	}
	public String getDateISOSinceStr() {
		return dateISOSinceStr;
	}
	public void setDateISOSinceStr(String dateISOSinceStr) {
		this.dateISOSinceStr = dateISOSinceStr;
	}
	public TimeBasedMiner(String owner, String repo, String endpointName,
			boolean isWriteIfCollectionExists, double timeInYears) {
		//((params.isEmpty())?"":"&")+ TimeBasedMiner.getTimeParamSince(timeInYears)
		super(owner, repo, endpointName, isWriteIfCollectionExists);
		if (timeInYears >= 0)
			super.setParamAndCreateEndpointURL("since="+getTimeParamSince(timeInYears));
	}
	
	//gets the time in years, returns including the & prefix for convenience
	public String getTimeParamSince(double timeInYears) {

		long currentTimeMilis = System.currentTimeMillis();
		long millisSince  = (long)(timeInYears*getMillisPerYear());
		
		long millisTimeAtSince = currentTimeMilis - millisSince;
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		String dateAsISO = df.format(new Date(millisTimeAtSince));
		MyLogger.log("ISO date: "+dateAsISO,LogLevel.DEBUG);
		this.dateISOSinceStr = dateAsISO;
		return dateAsISO;
	}

	private static long getMillisPerYear() {
		
		return 365L * 24 * 3600 * 1000;
	}
//	
//	public static void main(String[] args) {
//		System.out.println(TimeBasedMiner.getTimeParamSince(0.5));
//	}

	@Override
	public String getCollectionName() {
		return MongoDBLayer.getCollectionName(owner, repo, endpointName+(this.dateISOSinceStr.isEmpty()?"":"__"+this.dateISOSinceStr));
	}
}
