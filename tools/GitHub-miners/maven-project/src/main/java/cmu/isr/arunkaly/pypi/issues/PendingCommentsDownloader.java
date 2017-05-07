package cmu.isr.arunkaly.pypi.issues;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cmu.isr.arunkaly.controller.Constants;
import cmu.isr.arunkaly.miners.RepoElement;

public class PendingCommentsDownloader {

	public static List<RepoElement>  getListOfRepos(String fileName) {
		BufferedReader br = null;

		List<RepoElement> returnList = new ArrayList<RepoElement>();
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));
			String line = null;
			while ((line = br.readLine())!= null ) {
				int index = line.indexOf('/');
				RepoElement elem = new RepoElement(line.substring(0,index), line.substring(index+1));
				returnList.add(elem);
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}

		return returnList;
	}

	public static void main(String[] args) {
		//Input: Directory containing the issues already downloaded

		String inputFile = "errored-issues-repos.txt";
		if (args.length == 0)
		{
			System.out.println("ERROR - no input file ");
			System.exit(1);
		}
		if (args.length > 0 )
			inputFile = args[0];

		String gitIssuesDir = System.getProperty("user.dir")+"/pypiGitIssues";
		if (args.length > 1 )
			gitIssuesDir = args[1];

		String outDir = "pypiGitComments";
		if (!new File(outDir).exists()) {
			System.out.println("Error Output Dir not found "+outDir);
			System.exit(1);
		}
		List<RepoElement> listOfRepos = getListOfRepos(inputFile);

		for (RepoElement re : listOfRepos) {
			System.out.println("Searching repo: "+re.getOwner()+"/"+re.getRepo());
			List<Long> issueIds = getListOfIssueIds(re,gitIssuesDir);
			if (issueIds == null || issueIds.isEmpty()) {
				continue;
			}

			System.out.println("Total Issues: "+issueIds.size());
			IssueCommentsIterator ic = new IssueCommentsIterator(re.getOwner(), re.getRepo(), issueIds);
			StatefulOutputLayer outputController = new StatefulOutputLayer(outDir);
			ic.iterateAndWrite(outputController);
			System.out.println();
		}
	}

	private static List<Long> getListOfIssueIds(RepoElement re,
			String gitIssuesDir) {


		File curDir = new File(gitIssuesDir);
		File[] directoryListing = curDir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String issueFileName = child.getName();
				if (issueFileName.endsWith("__issues")) {
					RepoElement curRepo = getRepoElem(issueFileName);
					if (curRepo.equals(re)) {
						System.out.println("Found Repo: "+re.getOwner()+"/"+re.getRepo());
						return getIssueIdsFromJsonFile(child);
					}
				}
			}
		}
		System.out.println("Could not find repo: "+re.getOwner()+"/"+re.getRepo());
		return null;
	}

	private static List<Long> getIssueIdsFromJsonFile(File issueFile) {
		// TODO Auto-generated method stub
		JSONParser jp = new JSONParser();
		JSONArray ja = null;
		List<Long> returnList = new ArrayList<Long>();
		try {
			ja = (JSONArray)jp.parse(new FileReader(issueFile));
			int len = ja.size();
			for (int i = 0; i < len;++i){
				JSONObject jo = (JSONObject)ja.get(i);
				long issueId = -1;
				try {
					issueId = (Long) jo.get("number");
				} catch (ClassCastException e) {
					String s = (String)jo.get("number");
					issueId = Long.parseLong(s);
				}
				if (issueId == -1)
					System.out.println("***Invalid JSON Object - could not get issue id");
				else {
					//System.out.println("Found Issue ID: "+issueId);
					returnList.add(issueId);
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnList;
	}

	private static RepoElement getRepoElem(String issueFileName) {
		int index1 = issueFileName.indexOf(Constants.OWNER_REPO_SEPARATOR);
		int index2 = issueFileName.indexOf(Constants.REPO_ENDPOINT_SEPARATOR);
		String owner = issueFileName.substring(0,index1);
		index1 += Constants.OWNER_REPO_SEPARATOR.length();
		String repo = issueFileName.substring(index1,index2);
		return new RepoElement(owner,repo);
	}
}
