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

public class PeriodicIssuesDownloader {

	
	public static void main(String[] args) {
		
		String fileName = "list_of_repos.txt";
		if (args.length > 0) {
			fileName = args[0];
		}
		System.out.println("File:"+fileName);
		String outDir = "MSIssues";
		
		if (args.length > 1) {
			outDir= args[1];
		}
		System.out.println("Dir:"+outDir);
		//getCurrentDir();
		File curDir = new File(outDir);
		if (!curDir.exists())
			curDir.mkdirs();
		
		//open file
		String[] reposArr= getListOfRepos(fileName);
		
		System.out.println("Total Git repos in file: "+reposArr.length);
		
		FileOutputLayer fileController = new FileOutputLayer(outDir);
		int count = 0;
		for (int i = 0; i < reposArr.length; ++i) {
			String[] ownerRepo = reposArr[i].split("/");
			if (ownerRepo.length!=2)
				continue;
			WrapperIssuesMinerPeriodic wp = new WrapperIssuesMinerPeriodic(ownerRepo[0], ownerRepo[1]);
			
			wp.invokeAllMiners(fileController);
			count++;
		}
		System.out.println("Total Git repos in file: "+reposArr.length);
		System.out.println("Total repos with issues downloaded: "+ count);
	}

	private static String[] getListOfRepos(String fileName) {
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
