package cmu.isr.arunkaly.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.analysis.UserActivitiesExtracter;
import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.dblayer.ActiveForkUpdater;
import cmu.isr.arunkaly.dblayer.CombinationActivity;
import cmu.isr.arunkaly.dblayer.CommitPerformedUpdater;
import cmu.isr.arunkaly.dblayer.GithubUser;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class TrialAnalyzeAll {

	
	//Get the Map of user and activities
	//requiredActivities = []
	//Get the Map of user and count for each required activity
	//combinationActivities = []; CombinationActivity {String name, int minCount, int maxCount} //-1: dont care 
	//get a list of users for a given combination activities 
	public static void main(String[] args) {
		if (!MongoDBLayer.getInstance().isRunning()) {
			MyLogger.log("Error. Mongodb not running");
			return;
		}
		String owner = "scipy";
		String repo = "scipy";
		UserActivitiesExtracter uex = new UserActivitiesExtracter(owner, repo);
		uex.extractAllUserActivities();
		MyLogger.log("TOTAL USERS: "+uex.getMapOfUsers().size());
		
		String[] activityNames = new String[] {ActivityNames.FORK_CREATED, ActivityNames.ACTIVE_FORK, ActivityNames.COMMIT_AUTHOR, 
				ActivityNames.COMMIT_COMMITTER, ActivityNames.COMMIT_PERFORMED, ActivityNames.ISSUE_REPORTED, ActivityNames.ISSUE_COMMENTED,
				ActivityNames.ISSUE_ASSIGNED, ActivityNames.PR_SUMBITTED, ActivityNames.PR_COMMENTED, ActivityNames.PR_ASSIGNED,
				ActivityNames.REUSE_IN_COMMIT, ActivityNames.RELEASE_MANAGER, ActivityNames.ISSUE_REPORTED_COMMENTED};
		
		Map<GithubUser, int[]> usersWithCounts = uex.getUsersWithActivityCounts(activityNames, null);
		MyLogger.log("Users with Counts: "+ usersWithCounts.size());
		//Update active forks
		uex.updateActivityCounts(usersWithCounts,  ActivityNames.ACTIVE_FORK, new ActiveForkUpdater());
		uex.updateActivityCounts(usersWithCounts, ActivityNames.COMMIT_PERFORMED, new CommitPerformedUpdater());
		uex.updateActivityCounts(usersWithCounts, ActivityNames.ISSUE_REPORTED_COMMENTED, new IssueReportedAndCommentedUpdater());
		
		//Add Email Addresses from profile to our map
		uex.computeEmailAddressesFromProfile();
		
		String[] filterActivities = {};
		int[] min = {};
		int[] max = {};
		Set<GithubUser> filteredUsersSet;
		CombinationActivity[] combinations ;
		List<GithubUser> filteredUsersList;
		
		//Complete Set
		filterActivities =  new String[]{};
		min = new int[]{};
		max = new int[]{};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Complete List with counts of activities", "complete_list.csv");
		
		//Active forks with either commit authored or commit committed
		filterActivities =  new String[]{ActivityNames.ACTIVE_FORK, ActivityNames.COMMIT_PERFORMED};
		min = new int[]{1,1};
		max = new int[]{-1,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Active forks who have made at least one commit", "active_fork_commits.csv");
		
		//Active forks who have made commits or submitted PRs
		filteredUsersSet = new HashSet<GithubUser>();
		filteredUsersSet.addAll(filteredUsersList);
		filterActivities =  new String[]{ActivityNames.ACTIVE_FORK,ActivityNames.PR_SUMBITTED};
		min = new int[]{1,1};
		max = new int[]{-1,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		filteredUsersSet.addAll(filteredUsersList);
		writeToFile(filteredUsersSet, usersWithCounts,activityNames, "Active forks who have made at least one commit or Submitted PRs", "active_fork_commits_PRS.csv");

		//Active fork but no commits and PRs
		filterActivities =  new String[]{ActivityNames.ACTIVE_FORK,ActivityNames.PR_SUMBITTED,ActivityNames.COMMIT_PERFORMED };
		min = new int[]{1,0,0};
		max = new int[]{-1,0,0};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Active forks with no commits or PR submitted", "active_fork_no_commits_PRS.csv");

		//Active fork and no commits and PRs but submitted Issues and/or comments to issues and PR		
		filterActivities =  new String[]{ActivityNames.ACTIVE_FORK,ActivityNames.PR_SUMBITTED,ActivityNames.COMMIT_PERFORMED, 
				ActivityNames.ISSUE_REPORTED_COMMENTED};
		min = new int[]{1,0,0,1};
		max = new int[]{-1,0,0,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);		
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Active forks with no commits or PR submitted but Reported issues or commented on Issues or PRs", "active_fork_no_commits_PRS_yes_issues.csv");

		// forks with reuse signs but not commits or PRs - contains both active and inactive forks
		filterActivities =  new String[]{ActivityNames.FORK_CREATED,ActivityNames.REUSE_IN_COMMIT, ActivityNames.PR_SUMBITTED, ActivityNames.COMMIT_PERFORMED };
		min = new int[]{1,1,0,0};
		max = new int[]{-1,-1,0,0};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Forked and signs of reuse but no PR or commits - could indicate private usage of forks", "fork_reuse_no_commits_PRs.csv");
		
		//forks without reuse - indicate private scripts or other forms of reuse
		filterActivities =  new String[]{ActivityNames.FORK_CREATED,ActivityNames.REUSE_IN_COMMIT};
		min = new int[]{1,0};
		max = new int[]{-1,0};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Forked and no signs of reuse - indicate private scripts or other forms of reuse", "fork_no_reuse.csv");
		
		//No reuse - need to understand if they have private scripts or other forms of reuse or technical problem in our approach
		filterActivities =  new String[]{ActivityNames.REUSE_IN_COMMIT};
		min = new int[]{0};
		max = new int[]{0};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "No Signs of reuse - private scripts? or other forms of reuse? or technical problem in our mining", "no_reuse.csv");

		//PRs but no commits - indicates Rejected PRs or Open PRs
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED, ActivityNames.COMMIT_PERFORMED};
		min = new int[]{1,0};
		max = new int[]{-1,0};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "PRs but not commits - rejected or open PRs", "PRs_no_commits.csv");

		//Commit performed but no PR submitted -> indication of commit rights and hence no PRs required, depends on process
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED, ActivityNames.COMMIT_PERFORMED};
		min = new int[]{0,1};
		max = new int[]{0,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "commits but no PRs - users commit rights; depends on process", "commits_no_PRs.csv");
		
		//Users with commit rights or committers
		filterActivities =  new String[]{ActivityNames.COMMIT_COMMITTER};
		min = new int[]{1};
		max = new int[]{-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Users with Commit rights or committers", "commit_committers.csv");
		
		//People who have reported issues or comments but no commits or PRs
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED,ActivityNames.COMMIT_PERFORMED, 
				ActivityNames.ISSUE_REPORTED_COMMENTED };
		min = new int[]{0,0,1};
		max = new int[]{0,0,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);	
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "no commits or PR submitted but Reported issues or commented on Issues or PRs", "no_commits_PRS_yes_issues.csv");

		//Users who did everything - issues reported, pr submitted, commented and committed - DOnt care about forked, starred, watched or reused
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED, ActivityNames.COMMIT_PERFORMED, ActivityNames.ISSUE_REPORTED_COMMENTED};
		min = new int[]{1,1,1};
		max = new int[]{-1,-1,-1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Users who have done everything - commits; PRs and issues report or comment", "did_everything_commits_PRs_issues.csv");

		//Users with exactly 1 Commit or one PR
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED};
		min = new int[]{1};
		max = new int[]{1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		filteredUsersSet = new HashSet<GithubUser>();
		filteredUsersSet.addAll(filteredUsersList);
		filterActivities =  new String[]{ActivityNames.COMMIT_AUTHOR};
		min = new int[]{1};
		max = new int[]{1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		filteredUsersSet.addAll(filteredUsersList);
		writeToFile(filteredUsersSet, usersWithCounts,activityNames, "Users with exactly one PR or one Commit", "one_commit_or_PR.csv");
		
		//Users with exactly 1 Issue reported
		filterActivities =  new String[]{ActivityNames.ISSUE_REPORTED};
		min = new int[]{1};
		max = new int[]{1};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Users with exactly one Issue reported", "one_issue.csv");
		
		//Users with 2 - 5 commits or PRs
		filterActivities =  new String[]{ActivityNames.PR_SUMBITTED};
		min = new int[]{2};
		max = new int[]{5};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		filteredUsersSet = new HashSet<GithubUser>();
		filteredUsersSet.addAll(filteredUsersList);
		filterActivities =  new String[]{ActivityNames.COMMIT_AUTHOR};
		min = new int[]{2};
		max = new int[]{5};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		filteredUsersSet.addAll(filteredUsersList);
		writeToFile(filteredUsersSet, usersWithCounts,activityNames, "Users with 2 - 5 PR or Commits", "two_to_five_commits_PRs.csv");
		
		//Users with 2 - 5 issues reported
		filterActivities =  new String[]{ActivityNames.ISSUE_REPORTED};
		min = new int[]{2};
		max = new int[]{5};
		combinations = getFilteredCombinations(filterActivities	,min,max);
		filteredUsersList = uex.getConstrainSatisfiedUsers(usersWithCounts, activityNames, combinations);
		writeToFile(filteredUsersList, usersWithCounts,activityNames, "Users with two to five issues reported", "two_to_five_issues.csv");				
		
	}

	private static void writeToFile(Collection<GithubUser> filteredUsers,
			Map<GithubUser, int[]> usersWithCounts, String[] activityNames,
			String title, String fileName) {
		//First row is the title
		//Fields: login, Full Name, emails from profile, email from commit, activity names
		BufferedWriter bw = null ;
		MyLogger.log("Writing to file: "+fileName);
		
		try {
			bw = new BufferedWriter(new FileWriter(new File(fileName)));
			//Write Title
			bw.write(title+"\n");
			//write headers
			bw.write(getColumnHeader(activityNames)+"\n");
			for (GithubUser gu : filteredUsers) {
				StringBuilder sb = new StringBuilder();
				sb.append(gu.getLogin()+","+gu.getFullName()+",");
				sb.append(gu.getEmailsFromProfileAsString()+",");
				sb.append(gu.getEmailsFromCommitsAsString()+",");
				sb.append(getCountsAsString(usersWithCounts.get(gu)));
				bw.write(sb.toString()+"\n");
			}
			MyLogger.log("Number of records written: "+filteredUsers.size());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeQuietly(bw);
		}
		

		
	}

	private static void closeQuietly(BufferedWriter bw) {
		try {
			bw.close();
		} catch (IOException e) {
		}
		
	}

	private static String getCountsAsString(
			int[] counts) {
		StringBuilder sb = new StringBuilder();
		
		for (int val : counts)
			sb.append(val+",");
		
		return sb.substring(0,sb.length()-1);
	}

	private static String getColumnHeader(String[] activityNames) {
		StringBuilder sb = new StringBuilder();
		sb.append("login,Full Name,Emails from Profile,Emails from Commits,");
		for (String activity: activityNames) {
			sb.append(activity+",");
		}
		return sb.substring(0, sb.length()-1);
	}

	private static CombinationActivity[] getFilteredCombinations(
			String[] filterActivities, int[] min, int[] max) {
		CombinationActivity[] combinations = new CombinationActivity[filterActivities.length];
		for (int i = 0; i < filterActivities.length; ++i) {
			combinations[i] = new CombinationActivity(filterActivities[i],min[i],max[i]);
		}
		return combinations;
	}
		
	
	//Start creating different combinations or stratifying
	
	//Users who have active forks and made commits
	//Users who have active forks and made commits or submitted PR
	//Users who have active forks and made no commits or PR
	//among above users, those who submitted issues and not
	//Users who have inactive forks and reuse => indicates people using the fork as a private way of reusing
	//users who have inactive forks but no reuse => Need to first figure out if we have a technical problem in detecting reuse.
		//or could be users who have private copies of their scripts
	//Inactive forks users with reuse
	
	//Users who submitted PRs but have no commits => Could be people with open PRs or rejected PRS

	//Users who have commit rights => based on number of commits
	//users who have commit rights and make lots ofcomments on PRs => Indicate core teams
	

	//The following two among people who have reused and not reused
		//users who have reported issues but no commits or PRs (sort of end users but people who reported issues)
		//Users who have reported or commented on Issues and PRs but no commits or PRs submitted (another sort of end users)
	
	//Reused and made commits or PRs submitted
	//Reused and made no commits or PRs submitted
	
	//General users who reused, and committed, PRs submitted, ISSues submitted  and commented.
	//Did not reuse but did all the above

	//People making just 1 commit, or 1 PR or reporting  just one Issue
	//People make 2 - 5 commits, PR or reporting 2 - 5 issues.
	
}
