package cmu.isr.arunkaly.pypi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.miners.OrgMembersMiner;

public class OrgMembersDownloader {

	
	public static void main(String[] args) {
		
		String fileName = "orgs_list.txt";
		if (args.length > 0) {
			fileName = args[0];
		}
		System.out.println("File:"+fileName);
		String outDir = "MSOrgs";
		
		if (args.length > 1) {
			outDir= args[1];
		}
		System.out.println("Dir:"+outDir);
		//getCurrentDir();
		File curDir = new File(outDir);
		if (!curDir.exists())
			curDir.mkdirs();
		
		//open file
		String[] reposArr= getListOfOrgs(fileName);
		
		System.out.println("Total Orgs in file: "+reposArr.length);
		
		FileOutputLayer fileController = new FileOutputLayer(outDir);
		int count = 0;
		for (int i = 0; i < reposArr.length; ++i) {
			OrgMembersMiner om = new OrgMembersMiner(reposArr[i], false);
			
			if (!om.invokeAndWrite(fileController)) {
				MyLogger.logError("ERROR: Unable to extract members for ORG: "+reposArr[i]);
			} else {
				count++;
			}
		}
		System.out.println("Total orgs: "+reposArr.length);
		System.out.println("Total orgs successfully downloaded: "+ count);
	}

	private static String[] getListOfOrgs(String fileName) {
		//open file
		BufferedReader br = null;
		List<String> repos = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			//br.readLine();
			String line = null;
			while ((line = br.readLine())!= null){
				String repoName = line.trim();
				if (repoName.isEmpty())
					continue;
				repos.add(repoName);
			}
			br.close();
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return repos.toArray(new String[repos.size()]);
	}
	
	private static String getCurrentDir() {
		File f = new File(".");
		try {
			System.out.println(f.getCanonicalPath());
			return f.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
