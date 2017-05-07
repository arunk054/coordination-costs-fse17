package cmu.isr.arunkaly.miners;

public class TimeBasedUntilMiner extends TimeBasedMiner {

	public TimeBasedUntilMiner(String owner, String repo, String endpointName,
			boolean isWriteIfCollectionExists, double timeSince, double timeUntil) {
		super(owner, repo, endpointName, isWriteIfCollectionExists, timeSince);
		if (timeUntil >= 0) {
			super.setParamAndCreateEndpointURL("until="+getTimeParamSince(timeUntil));
		}
	}
}
