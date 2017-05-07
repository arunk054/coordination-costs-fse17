package cmu.isr.arunkaly.dblayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.miners.RepoElement;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.Mongo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class UniqueUsersExtracter {

	
	
	//Identify the list of users from each collection
	//Create the user object and list of activities
	//Write all of them to the DB
	//repeat above for other collections
	
	
	private String repo;
	private String owner;
	private String usersCollectionName;

	public UniqueUsersExtracter(RepoElement re) {
		this.owner = re.getOwner();
		this.repo = re.getRepo();
		this.usersCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
	}
	
	public void writeMapOfUsers(Map<String,GithubUser> mapOfUsers) {
		//iterate map entry and valu
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(this.usersCollectionName);
		
		for(Map.Entry<String, GithubUser> entry: mapOfUsers.entrySet()) {
			String login = entry.getKey();
			Document existingDoc = MongoDBLayer.getInstance().getDocument(login,col);
			
			if (existingDoc != null) {
				GithubUser existingUser = GithubUser.getObject(existingDoc);
				existingUser.mergeAnotherUser(entry.getValue());
				updateToDB(col,existingUser);
			}else {
				insertToDB(col, entry.getValue());
			}
			
		}		
	}
	
	public static void insertToDB(MongoCollection<Document> col, GithubUser gu) {
		String key = gu.getLogin();
		JSONObject userJO = new JSONObject(gu);
		JSONObject insertJO = new JSONObject();
		insertJO.put("_id", key);
		insertJO.put("user", userJO);
		//Make sure we set the correct date format
		//This one : DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		//Default is "EEE MMM dd kk:mm:ss z yyyy"
		Document newDoc = Document.parse(insertJO.toString());
		col.insertOne(newDoc);
	}

	public static void updateToDB(MongoCollection<Document> col,
			GithubUser existingUser) {
		String key = existingUser.getLogin();
		Document newDoc = Document.parse((new JSONObject(existingUser)).toString());
		col.updateOne(new BasicDBObject("_id",key), new BasicDBObject("$set",new BasicDBObject("user",newDoc)));
	}

	public void removeUsersActivitiesCollection () {
		//remove the Collection since this will be new data
		MongoDBLayer.getInstance().removeCollection(this.usersCollectionName);
	}
	public void populateUniqueUsers()  {
		//call removeCollection from outside if needed
		writeMapOfUsers(getUniqueUsers());
	}
	
	private Map<String, GithubUser>  getUniqueUsers() {
		Map<String,GithubUser> mapOfUsers = new HashMap<String, GithubUser>();

		getUniqueUsersFromStars(owner,repo,mapOfUsers);
		
		getUniqueUsersFromForks(owner, repo, mapOfUsers);
		
		
		getUniqueUsersFromSubscribers(owner, repo, mapOfUsers);

		getUniqueUsersFromReleases(owner, repo,mapOfUsers);

		getUniqueUsersFromContributors(owner, repo, mapOfUsers);

		getUniqueUsersFromCommits(owner, repo,mapOfUsers);

		
		getUniqueUsersFromIssues(owner, repo,mapOfUsers);

		getUniqueUsersFromIssuesComments(owner, repo,mapOfUsers);
		
		return mapOfUsers;
	}
	
	private boolean isPullRequestComment(Document document) {
		try {
			String url = document.getString("html_url");
			if (url.contains("/pull/")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	private void getUniqueUsersFromIssuesComments(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {

		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_ISSUES_COMMENTS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				boolean isPullRequest = isPullRequestComment(document);
				String user;
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");
					String dateStr = document.getString("created_at");
					GithubUser gu = new GithubUser(user);
					String activity = ActivityNames.ISSUE_COMMENTED;
					if (isPullRequest)
						activity = ActivityNames.PR_COMMENTED;
					gu.addActivity(new Activity(activity,MongoDBLayer.getDateFromString(dateStr)));
					addUser(mapOfUsers,gu);
				} catch (Exception e) {
					MyLogger.logError("Error reading user login from "+collectionName);
				}
			}
		});
	}

	
	private boolean isPullRequestIssue(Document document) {
		try {
			Document pr = null;
			if ((pr=(Document) document.get("pull_request")) != null && pr.getString("url")!=null) {
				return true;
			}
		} catch (Exception e) {

		}
		return false;
	}
	public void getUniqueUsersFromIssues(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {

		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_ISSUES); 
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				boolean isPullRequest = isPullRequestIssue(document);
				String dateStr = document.getString("created_at");
				try {
					Document userDoc = (Document) document.get("user");
					user = userDoc.getString("login");

					GithubUser gu = new GithubUser(user);
					String activity = ActivityNames.ISSUE_REPORTED;
					if (isPullRequest)
						activity = ActivityNames.PR_SUMBITTED;
					
					gu.addActivity(new Activity(activity,MongoDBLayer.getDateFromString(dateStr)));
					addUser(mapOfUsers,gu);
				} catch (Exception e) {
					//Just skip
					MyLogger.logError("Error reading user login from "+collectionName);
				}

				//extract the assignees
				try {
					Document userDoc = (Document) document.get("assignee");
					user = userDoc.getString("login");
					
					GithubUser gu = new GithubUser(user);
					String activity = ActivityNames.ISSUE_ASSIGNED;
					if (isPullRequest)
						activity = ActivityNames.PR_ASSIGNED;
					
					gu.addActivity(new Activity(activity,MongoDBLayer.getDateFromString(dateStr)));
					addUser(mapOfUsers,gu);
					
				} catch (Exception e) {
				}
				
				try {
					List<Document> userDocList = (List<Document>)document.get("assignees");
					for (Document userDoc: userDocList) {
						user = userDoc.getString("login");
						GithubUser gu = new GithubUser(user);
						String activity = ActivityNames.ISSUE_ASSIGNED;
						if (isPullRequest)
							activity = ActivityNames.PR_ASSIGNED;
						
						gu.addActivity(new Activity(activity,MongoDBLayer.getDateFromString(dateStr)));
						addUser(mapOfUsers,gu);
					}
				} catch (Exception e) {
					//Just skip since this wil be absent for most issues
				}


			}

		});
	}

	private void getUniqueUsersFromCommits(String owner, String repo, final Map<String,GithubUser> mapOfUsers  ) {

		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_COMMITS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {

				GithubUser authorGU = null,committerGU = null;
				//for debugging
				Document authorDoc=null;
				try {
					authorDoc = (Document) document.get("author");
					String user = null;
					if (authorDoc != null)
						user = authorDoc.getString("login");
					String dateStr = null;
					String emailAuthor = null;
					String fullName = null;
					try {
	 					Document commitAuthor = (Document)((Document)document.get("commit")).get("author");
						dateStr = commitAuthor.getString("date");
						emailAuthor = commitAuthor.getString("email");
						fullName = commitAuthor.getString("name");
					}catch (Exception e){
					}
					//some sort of hack when author block is null but the commit block is non null and contains the email
					if (user == null)
						user = emailAuthor;
					
					authorGU = new GithubUser(user);
					authorGU.addActivity(new Activity(ActivityNames.COMMIT_AUTHOR,MongoDBLayer.getDateFromString(dateStr)));
					authorGU.addEmail(emailAuthor);
					authorGU.setFullName(fullName);
					addUser(mapOfUsers,authorGU);
				} catch (Exception e) {
					//Just skip
					MyLogger.logError("Error reading commits author from "+collectionName);
					MyLogger.log("Document: "+document.toJson().toString(),LogLevel.DEBUG);
				}

				try {
					Document committerDoc = (Document) document.get("committer");
					String user = null;
					if (committerDoc != null)
						user = committerDoc.getString("login");
					
					String dateStr = null;
					String emailAuthor = null;
					String fullName = null;
					try {
	 					Document commitAuthor = (Document)((Document)document.get("commit")).get("committer");
						dateStr = commitAuthor.getString("date");
						emailAuthor = commitAuthor.getString("email");
						fullName = commitAuthor.getString("name");
					}catch (Exception e){
					}
					if (user == null)
						user = emailAuthor;
					committerGU = new GithubUser(user);
					committerGU.addEmail(emailAuthor);
					committerGU.setFullName(fullName);
					committerGU.addActivity(new Activity(ActivityNames.COMMIT_COMMITTER,MongoDBLayer.getDateFromString(dateStr)));
					addUser(mapOfUsers, committerGU);
				} catch (Exception e) {
				}
			}
		});
	}

	
	public void  getUniqueUsersFromContributors(String owner, String repo, final Map<String,GithubUser> mapOfUsers ) {

		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_CONTRIBUTORS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(new Activity(ActivityNames.CONTRIBUTOR,null));
					addUser(mapOfUsers, gu);
				} catch (Exception e) {
					MyLogger.logError("Error reading user login from "+collectionName);
				}
			}
		});
	}
	
	private void getUniqueUsersFromReleases(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {
		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_RELEASES); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					Document authorDoc = (Document) document.get("author");
					user = authorDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(new Activity(ActivityNames.RELEASE_MANAGER, 
							MongoDBLayer.getDateFromString(document.getString("created_at"))));
					addUser(mapOfUsers,gu);
				} catch (Exception e) {
					//Just skip
					MyLogger.log("Error reading user login from "+collectionName,LogLevel.ERROR);
				}

				try {
					List<Document> assetArr = (List<Document>) document.get("assets");
					for (Document assedDoc: assetArr) {
						Document uploaderDoc = (Document) assedDoc.get("uploader");
						user = uploaderDoc.getString("login");
						GithubUser gu = new GithubUser(user);
						gu.addActivity(new Activity(ActivityNames.RELEASE_MANAGER,MongoDBLayer.getDateFromString((String)assedDoc.get("created_at"))));
						addUser(mapOfUsers,gu);
					}
				} catch (Exception e) {
					MyLogger.logError("Error reading user login from assets in "+collectionName);
				}

			}
		});
	}

	private void getUniqueUsersFromSubscribers(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {

		//get the collectionName
		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_SUBSCRIBERS);
		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();


		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(new Activity(ActivityNames.WATCH,null));
					addUser(mapOfUsers,gu);
					
				} catch (Exception e) {
					MyLogger.log("Error reading user login from "+collectionName, LogLevel.ERROR);
				}
			}
		});
	}

	public void getUniqueUsersFromForks(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {


		//get the collectionName
		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_FORKS); 

		//Read each record one by one and extract the username and any other relevant information
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		FindIterable<Document> iterable = collection.find();
		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					Document ownerDoc = (Document) document.get("owner");
					user = ownerDoc.getString("login");
					GithubUser gu = new GithubUser(user);
					
					gu.addActivity(new Activity(ActivityNames.FORK_CREATED,MongoDBLayer.getDateFromString(document.getString("created_at"))));
					gu.addActivity(new Activity(ActivityNames.FORK_PUSHED,MongoDBLayer.getDateFromString(document.getString("pushed_at"))));
					gu.addActivity(new Activity(ActivityNames.FORK_UPDATED,MongoDBLayer.getDateFromString(document.getString("updated_at"))));					
					addUser(mapOfUsers,gu);
				} catch (Exception e) {
					//Just skip
					MyLogger.log("Error reading user login from "+collectionName,LogLevel.ERROR);
				}
			}
		});
	}
	public void getUniqueUsersFromStars(String owner, String repo, final Map<String,GithubUser> mapOfUsers) {

		
		//get the collectionName
		final String collectionName = MongoDBLayer.getCollectionName(owner,repo,EndPointNames.ENDPOINT_STARS); 
		MongoCollection<Document> collection = MongoDBLayer.getInstance().getCollection(collectionName);
		//Read each record one by one and extract the username and any other relevant information
		FindIterable<Document> iterable = collection.find();

		iterable.forEach(new Block<Document>() {
			public void apply(Document document) {
				String user;
				try {
					user = document.getString("login");
					GithubUser gu = new GithubUser(user);
					gu.addActivity(new Activity(ActivityNames.STAR,null));
					addUser(mapOfUsers,gu);
				} catch (Exception e) {
					//Just skip
					MyLogger.log("Error reading user login from "+collectionName, LogLevel.ERROR);
				}
			}
		});
	}

	public void addUser(Map<String,GithubUser> mapOfUsers, GithubUser curUser){
		GithubUser existing = mapOfUsers.get(curUser.getLogin());
		if (existing != null) {
			existing.mergeAnotherUser(curUser);
		} else {
			mapOfUsers.put(curUser.getLogin(), curUser);
		}
	}
}
