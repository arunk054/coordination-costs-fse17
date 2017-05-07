package cmu.isr.arunkaly.pypi.issues;

import java.util.List;

import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.miners.GenericRepoMiner;
import cmu.isr.arunkaly.miners.IndividualCommentsMiner;

public class IssueCommentsIterator {

	private String owner;
	private String repo;
	private List<Long> issueIds;
	//Given a list of issueIds and a repo name - this iterates through them and writes the output to a file
	public IssueCommentsIterator(String owner, String repo, List<Long> issueIds2) {
		this.owner = owner;
		this.repo = repo;
		this.issueIds = issueIds2;
		
		
	}
	
	public void iterateAndWrite(DBInterfaceStateful outputController){
		//Iterate through the issueIds
		for (Long issueId: issueIds){
			String endpointName = EndPointNames.ENDPOINT_ISSUES+"/"+String.valueOf(issueId)+"/"+EndPointNames.ENDPOINT_COMMENTS;
			IndividualCommentsMiner commentMiner = new IndividualCommentsMiner(owner, repo, endpointName, false);
			if (!commentMiner.invokeAndWrite(outputController)) {
				System.out.println("Error mining "+commentMiner.getCollectionName()+ " for: "+owner+"/"+repo);
			}
		}
		outputController.setIsWriteEnabled(true);
		IndividualCommentsMiner tempMiner = new IndividualCommentsMiner(owner, repo, "", false);
		tempMiner.writeToDB(outputController);
	}
}
