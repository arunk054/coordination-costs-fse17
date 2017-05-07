package cmu.isr.arunkaly.tests;

import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.dblayer.ActivityUpdater;
import cmu.isr.arunkaly.dblayer.GithubUser;

public class IssueReportedAndCommentedUpdater implements ActivityUpdater {

	public int getCount(GithubUser gu) {
		int count= gu.acquireMatchingActivities(new String[] {ActivityNames.ISSUE_REPORTED}).size();
		count += gu.acquireMatchingActivities(new String[] {ActivityNames.ISSUE_COMMENTED}).size();
		count+= gu.acquireMatchingActivities(new String[] {ActivityNames.PR_COMMENTED}).size();
		return count; 
	}

}
