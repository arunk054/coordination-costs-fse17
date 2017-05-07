package cmu.isr.arunkaly.dblayer;

import cmu.isr.arunkaly.controller.ActivityNames;

public class CommitPerformedUpdater implements ActivityUpdater {

	public int getCount(GithubUser gu) {
		int countAuthor = gu.acquireMatchingActivities(new String[] {ActivityNames.COMMIT_AUTHOR}).size();
		int countCommitter = gu.acquireMatchingActivities(new String[] {ActivityNames.COMMIT_COMMITTER}).size();
		return countAuthor + countCommitter;
	}

}
