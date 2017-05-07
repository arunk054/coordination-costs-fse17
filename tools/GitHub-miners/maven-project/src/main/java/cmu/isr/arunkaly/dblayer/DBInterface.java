package cmu.isr.arunkaly.dblayer;

import org.json.JSONArray;

public interface DBInterface {
	
	public boolean writeRecords(JSONArray records, String collectionName) ;
	public void closeDB();

	public boolean isExists(String collectionName);
	
}
