package cmu.isr.arunkaly.controller;

public class ActivityNames {


	public static final String STAR = "starred";
	public static final String FORK_CREATED = "fork_created";
	public static final String FORK_PUSHED = "fork_pushed";
	public static final String FORK_UPDATED = "fork_updated";
	public static final String ACTIVE_FORK = "active_fork";
	
	public static final String COMMIT_AUTHOR = "commit_authored";
	public static final String COMMIT_COMMITTER = "commit_committed";
	public static final String COMMIT_PERFORMED = "commit_performed"; //sum of authored and committed

	public static final String ISSUE_REPORTED = "issue_reported";
	public static final String PR_SUMBITTED = "pr_submitted";

	public static final String ISSUE_COMMENTED = "issue_commented";
	public static final String PR_COMMENTED = "pr_commented";
	public static final String ISSUE_REPORTED_COMMENTED = "issue_reported_plus_commented"; //also included PR comments but not PR reports
	
	public static final String ISSUE_ASSIGNED = "issue_assigned";
	public static final String PR_ASSIGNED = "pr_assigned";
	
	public static final String WATCH = "watched";
	public static final String CONTRIBUTOR = "contributor";
	
	public static final String REUSE_IN_COMMIT = "reuse_in_commit";
	public static final String REUSE_IN_REPO = "reuse_in_repo";

	public static final String RELEASE_MANAGER = "release_manager";
}
