package cmu.isr.arunkaly.dblayer;

public interface TemporaryCache {

	//returns success if added
	public boolean addToCache(String key, boolean isRetryAgain);
	public boolean isExistsInCache(String key);
	public boolean isExistsAndNoRetry(String key);
	public void createNewCache(boolean deleteIfExists);
	public void removeCache();
}
