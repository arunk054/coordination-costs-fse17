package cmu.isr.arunkaly.dblayer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class MemoryOutputLayer implements DBInterface{

	private ArrayList<JSONObject> listOfRecords;

	public ArrayList<JSONObject> getListOfRecords() {
		return listOfRecords;
	}
	public void setListOfRecords(ArrayList<JSONObject> listOfRecords) {
		this.listOfRecords = listOfRecords;
	}
	public MemoryOutputLayer() {
		this.listOfRecords = new ArrayList<JSONObject>();
	}
	public boolean writeRecords(JSONArray records, String collectionName) {
		for (int i = 0; i < records.length(); ++i){
			this.listOfRecords.add(records.getJSONObject(i));
		}
		return true;
	}

	public void closeDB() {
		// TODO Auto-generated method stub
		
	}

	public boolean isExists(String collectionName) {
		return false;
	}

}
