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

public class PyPiGitReposDownloader {

	
	public static void main(String[] args) {
		
		String fileName = "out5.csv";
		if (args.length > 0) {
			fileName = args[0];
		}
		System.out.println("File:"+fileName);
		String outDir = "pypiGitRepos";
		
		if (args.length > 1) {
			outDir= args[1];
		}
		System.out.println("Dir:"+outDir);
		//getCurrentDir();
		File curDir = new File(outDir);
		if (!curDir.exists())
			curDir.mkdirs();
		
		//open file
		String[][] package2DArr = getListOfRepos(fileName);
		//Get Repos and their pkg names in two lists

		/*for (int i = 0; i < package2DArr[0].length; ++i) {
			System.out.println(package2DArr[0][i]+" : "+package2DArr[1][i]);
		}*/
		
		System.out.println("Total Git repos in pypi: "+package2DArr[0].length);
		
		//Write header to file
		//File outFile = new File(outDir, "temp");
		//writeHeader(outFile);
		
		FileOutputLayer fileController = new FileOutputLayer(outDir);
		int count = 0;
		for (int i = 0; i < package2DArr[0].length; ++i) {
			String[] ownerRepo = package2DArr[1][i].split("/");
			if (ownerRepo.length!=2)
				continue;
			WrapperMinerForPyPi wp = new WrapperMinerForPyPi(ownerRepo[0], ownerRepo[1]);
			
			wp.invokeAllMiners(fileController);
			count++;
		}
		System.out.println("Repos size: "+package2DArr[0].length);
		System.out.println("Total count of repos downloaded: "+ count);
	}

	private static void writeHeader(File outFile) {
		// TODO Auto-generated method stub
		BufferedWriter bw = null;
		
		try {
			outFile.createNewFile();
			bw = new BufferedWriter(new FileWriter(outFile));
			bw.write("pkg,repo,stargazers_count,watchers_count,forks_count,fork,size,has_issues,created_at,pushed_at,contributors\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static String[][] getListOfRepos(String fileName) {
		//open file
		BufferedReader br = null;
		List<String> pkgs= new ArrayList<String>(), repos = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			br.readLine();
			String line = null;
			while ((line = br.readLine())!= null){
				String[] sarr = line.split(",");
				if (sarr.length < 2)
					continue;
				sarr[1] = sarr[1].trim();
				if (sarr[1].isEmpty())
					continue;
				pkgs.add(sarr[0].trim());
				repos.add(sarr[1]);
			}
			br.close();
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[][] returnArr = new String[2][pkgs.size()];
		returnArr[0] = pkgs.toArray(new String[pkgs.size()]);
		returnArr[1] = repos.toArray(new String[repos.size()]);
		return returnArr;
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
