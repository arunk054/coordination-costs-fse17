package cmu.isr.arunkaly.controller;

public class Constants {
	public static final String GITHUB_API_URL = "https://api.github.com/";

	public static final int MAX_GITHUB_API_RETRIES=5;
	public static int RATE_LIMIT_MIN_THRESHOLD = 5;
	public static final String CONTINUE = "CONTINUE";
	public static final String DO_NOT_CONTINUE = "DO_NOT_CONTINUE";
	public static final String[] LINK_REL_VALID_HEADERS = {"next","last"};
	
	public static final String OWNER_REPO_SEPARATOR = "__R__";
	public static final String REPO_ENDPOINT_SEPARATOR = "__E__";

	public static final int MAX_PER_PAGE = 100;
	public static final int MAX_PAGE_LIMIT = 40000/MAX_PER_PAGE;
	public static final int MAX_PAGES_PER_BATCH_WRITE = 10000000;

	public static final int MAX_RETRIES_PER_REPO = 5;

	
	
}
