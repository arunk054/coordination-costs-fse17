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

public class PyPiGitIssuesDownloader {

	
	public static void main(String[] args) {
		
		String fileName = "git_pypi_repos.csv";
		if (args.length > 0) {
			fileName = args[0];
		}
		System.out.println("File:"+fileName);
		String outDir = "pypiGitIssues";
		
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
		//Get Repos and their pkg names in two lists

		/*for (int i = 0; i < package2DArr[0].length; ++i) {
			System.out.println(package2DArr[0][i]+" : "+package2DArr[1][i]);
		}*/
		
		
		System.out.println("Total Git repos in file: "+reposArr.length);
		//Write header to file
		//File outFile = new File(outDir, "temp");
		//writeHeader(outFile);
		
		FileOutputLayer fileController = new FileOutputLayer(outDir);
		int count = 0;
		for (int i = 0; i < reposArr.length; ++i) {
			String[] ownerRepo = reposArr[i].split("/");
			if (ownerRepo.length!=2)
				continue;
			WrapperIssuesMinerForPyPi wp = new WrapperIssuesMinerForPyPi(ownerRepo[0], ownerRepo[1]);
			
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
			br.readLine();
			String line = null;
			while ((line = br.readLine())!= null){
				String[] sarr = line.split(",");
				String repoName = sarr[0].trim();
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
