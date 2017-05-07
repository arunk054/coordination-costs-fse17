package cmu.isr.arunkaly.pypi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import cmu.isr.arunkaly.dblayer.DBInterface;

public class FileOutputLayer implements DBInterface{

	private String path;

	public FileOutputLayer(String path) {
		this.path = path;
	}
	public boolean writeRecords(JSONArray records, String collectionName) {
		//Just the file Name
		BufferedWriter bw = null;
		File outFile = new File(path,collectionName);
		try {
			outFile.createNewFile();
			bw = new BufferedWriter(new FileWriter(outFile,true));
			//if (records.length() == 1)
			//	bw.write(records.get(0).toString());
			//else
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

}
