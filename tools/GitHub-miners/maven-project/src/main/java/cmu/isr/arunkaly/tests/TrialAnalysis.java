package cmu.isr.arunkaly.tests;


import java.util.List;

import org.json.JSONObject;

import cmu.isr.arunkaly.analysis.UserActivitiesExtracter;
import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.dblayer.GithubUser;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class TrialAnalysis {

	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			System.out.println("Error. Mongodb not running");
			return;
		}
		String owner = "scipy";
		String repo = "scipy";
		UserActivitiesExtracter uex = new UserActivitiesExtracter(owner, repo);
		uex.extractAllUserActivities();
		System.out.println("TOTAL USERS: "+uex.getMapOfUsers().size());
		int[] range = new int[]{1,10};
		List<GithubUser> gUsers = uex.getUsersWithActivitiesBetween(range[0], range[1]);
		System.out.println("Users with Activities in the range:"+range[0]+", "+range[1]+": "+gUsers.size());
		range = new int[]{100,10000};
		gUsers = uex.getUsersWithActivitiesBetween(range[0], range[1]);
		System.out.println("Users with Activities in the range:"+range[0]+", "+range[1]+": "+gUsers.size());
//		for (GithubUser gu: gUsers) {
//			JSONObject jo = new JSONObject(gu);
//			System.out.println(jo.toString());
//		}
		List<GithubUser> usersWithForks = uex.getUsersWithForks();
		System.out.println("Total users who forked: "+usersWithForks.size());
		
		String[] activityNames = new String[] {ActivityNames.REUSE_IN_COMMIT};
		System.out.println("Forked but not reused (Weird): "+uex.getMatchingUsers(usersWithForks, activityNames, true).size());
		
		List<GithubUser> activeForks = uex.getUsersWithActiveForks(usersWithForks);
		System.out.println("Total users with active forks: "+activeForks.size());

		List<GithubUser> inActiveForks = uex.getUsersWithInActiveForks(usersWithForks);
		System.out.println("Total users with Inactive forks: "+inActiveForks.size());
		
		activityNames = new String[] {ActivityNames.PR_SUMBITTED, ActivityNames.CONTRIBUTOR, ActivityNames.COMMIT_AUTHOR, ActivityNames.COMMIT_COMMITTER};
		System.out.println("Users with Active forks but no code submitted: "+uex.getMatchingUsers(activeForks, activityNames, true).size());
		
		activityNames = new String[] {ActivityNames.REUSE_IN_COMMIT};
		List<GithubUser> reusedUsers = uex.getMatchingUsers(uex.getMapOfUsers().values(), activityNames, false);
		System.out.println("Users who have reused: "+reusedUsers.size());
		
		activityNames = new String[] {ActivityNames.PR_SUMBITTED, ActivityNames.CONTRIBUTOR, ActivityNames.COMMIT_AUTHOR, ActivityNames.COMMIT_COMMITTER};
		System.out.println("Among reused users submitted code: "+uex.getMatchingUsers(reusedUsers, activityNames, false).size());
		 List<GithubUser> reusedNotSubmitted = uex.getMatchingUsers(reusedUsers, activityNames, true);
		System.out.println("Among reused users NOT submitted code: "+reusedNotSubmitted.size());
		
		
		activityNames = new String[] {ActivityNames.ISSUE_REPORTED,ActivityNames.ISSUE_COMMENTED, ActivityNames.PR_COMMENTED};
		List<GithubUser> usersOnlyIssues = uex.getMatchingUsers(reusedNotSubmitted, activityNames, false);
		System.out.println("Among reused not submitted code, how many reported or commented issues: "+usersOnlyIssues.size());
		
		activityNames = new String[] {ActivityNames.FORK_CREATED};
		//reused but not forked
		System.out.println("Reused but not forked: "+uex.getMatchingUsers(reusedUsers, activityNames, true).size());
		
		activityNames = new String[] {ActivityNames.FORK_CREATED};
		
		//Select
		MongoDBLayer.getInstance().closeDB();
	}
}
