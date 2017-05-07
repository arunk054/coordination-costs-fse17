package cmu.isr.arunkaly.controller;


public class EndPointNames {

	public static final String ENDPOINT_COMMITS = "commits";
	public static final String ENDPOINT_STARS = "stargazers";
	public static final String ENDPOINT_FORKS = "forks";
	public static final String ENDPOINT_SUBSCRIBERS = "subscribers";
	public static final String ENDPOINT_RELEASES = "releases";
	public static final String ENDPOINT_CONTRIBUTORS = "contributors";
	public static final String ENDPOINT_ISSUES = "issues";
	public static final String ENDPOINT_ISSUES_COMMENTS = "issues/comments";
	public static final String ENDPOINT_COMMENTS = "comments";
	public static final String ENDPOINT_REPOSITORY = "";
	public static final String ENDPOINT_RATE_LIMIT = "rate_limit";
	public static final String ENDPOINT_SEARCH_CODE = "search/code";
	public static final String ENDPOINT_USERS = "users";
	
	public static final String COLLECTION_USERS_ACTIVITIES = "user_activities";

	public static final String USER_REPOS_COLLECTION_PREFIX = "users_";//used for repos of a user
	public static final String ENDPOINT_REPOS = "repos";//used for repos of a user with above prefix for clllection name
	public static final String ENDPOINT_ORG_MEMBERS = "members";
	
	public static String[] ALL_COLLECTION_SUFFIX = {EndPointNames.ENDPOINT_REPOSITORY,EndPointNames.ENDPOINT_CONTRIBUTORS,EndPointNames.ENDPOINT_RELEASES,
		EndPointNames.ENDPOINT_ISSUES,EndPointNames.ENDPOINT_ISSUES_COMMENTS,EndPointNames.ENDPOINT_SUBSCRIBERS,EndPointNames.ENDPOINT_STARS,
		EndPointNames.ENDPOINT_FORKS, EndPointNames.ENDPOINT_COMMITS,EndPointNames.COLLECTION_USERS_ACTIVITIES};
}
