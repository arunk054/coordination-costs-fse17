package cmu.isr.arunkaly.pypi.issues;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cmu.isr.arunkaly.dblayer.DBInterface;

public class StatefulOutputLayer implements DBInterfaceStateful{

	private String path;

	private boolean isWriteEnabled ;

	private ArrayList<JSONObject> listOfRecords;
	
	public StatefulOutputLayer(String path) {
		this.path = path;
		this.isWriteEnabled = false;
		this.listOfRecords = new ArrayList<JSONObject>();
	}
	
	private boolean storeRecords(JSONArray records){
		for (int i = 0; i < records.length(); ++i)
			listOfRecords.add(records.getJSONObject(i));
		return true;
	}
	public boolean writeRecords(JSONArray records, String collectionName) {
		if (!isWriteEnabled ){
			return storeRecords(records);
		}
		
		//Just the file Name
		BufferedWriter bw = null;
		File outFile = new File(path,collectionName);
		try {
			if (!outFile.createNewFile())//File exists so we are doing nothing as of now this is not supported
				return true;
			bw = new BufferedWriter(new FileWriter(outFile,true));
			//if (records.length() == 1)
			//	bw.write(records.get(0).toString());
			//else
			records = new JSONArray(listOfRecords);
			bw.write(records.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void closeDB() {
		//not used

	}

	public boolean isExists(String collectionName) {
		File f = new File(path,collectionName);
		return f.exists();
	}
	public void setIsWriteEnabled(boolean b) {
		this.isWriteEnabled = true;
	}

}
