package cmu.isr.arunkaly.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.controller.ActivityNames;
import cmu.isr.arunkaly.controller.EndPointNames;
import cmu.isr.arunkaly.dblayer.Activity;
import cmu.isr.arunkaly.dblayer.ActivityUpdater;
import cmu.isr.arunkaly.dblayer.CombinationActivity;
import cmu.isr.arunkaly.dblayer.CommitPerformedUpdater;
import cmu.isr.arunkaly.dblayer.GithubUser;
import cmu.isr.arunkaly.dblayer.MongoDBLayer;

public class UserActivitiesExtracter {

	private String owner;
	private String repo;
	private String userActivitiesCollectionName;
	private Map<String, GithubUser> mapOfUsers;
	private String[] activityNames;
	
	public String[] getActivityNames() {
		return activityNames;
	}

	public void setActivityNames(String[] activityNames) {
		this.activityNames = activityNames;
	}

	public Map<String, GithubUser> getMapOfUsers() {
		return mapOfUsers;
	}

	public UserActivitiesExtracter(String owner, String repo) {
		this.owner = owner;
		this.repo = repo;
		this.userActivitiesCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.COLLECTION_USERS_ACTIVITIES);
		
	}
	
	//Call only after building the mapOfUsers
	public void computeEmailAddressesFromProfile() {
		String userCollectionName = MongoDBLayer.getCollectionName(owner, repo, EndPointNames.ENDPOINT_USERS);
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(userCollectionName);
		FindIterable<Document> iterable = col.find();
		iterable.forEach(new Block<Document>() {

			public void apply(Document doc) {
				String login = doc.getString("login");

				if (login == null | login.isEmpty())
					return;
				
				GithubUser gu = mapOfUsers.get(login);;
				if (gu == null)
					return;
				gu.setEmailsFromProfile(getListOfEmails(doc.getString("email")));
				gu.setFullName(doc.getString("name"));	
			}

			private List<String> getListOfEmails(String str) {
				if(str == null || str.isEmpty())
					return null;
				String[] emails = str.split(",");
				List<String> returnList = new ArrayList<String>();
				for (String email : emails) {
					returnList.add(email);
				}
				return returnList;
				
			}
		});
		
	}

	
	public void extractAllUserActivities() {
		MyLogger.log("Extracting users and activities from DB...");
		MongoCollection<Document> col = MongoDBLayer.getInstance().getCollection(this.userActivitiesCollectionName);
		this.mapOfUsers = new HashMap<String,GithubUser>();
		FindIterable<Document> iterable = col.find();
		iterable.forEach(new Block<Document>() {

			public void apply(Document doc) {
				GithubUser gu = GithubUser.getObject(doc);
				mapOfUsers.put(gu.getLogin(),gu);
			}
		});	
		MyLogger.log("Finished extracting all users:" +mapOfUsers.size());
	}
	
	public List<GithubUser> getUsersWithActivitiesBetween(int min, int max) {
		List<GithubUser> returnList = new ArrayList<GithubUser>();
		for (Map.Entry<String, GithubUser> entry: mapOfUsers.entrySet()) {
			GithubUser gu = entry.getValue();
			int len = gu.getActivities().size();

			if (len >= min && len <= max) 
				returnList.add(gu);
		}
		return returnList;
	}
	public List<GithubUser> getUsersWithActiveForks(List<GithubUser> usersWithForks) {
		List<GithubUser> returnList = new ArrayList<GithubUser>();
		for (GithubUser gu: usersWithForks) {
			String[] forkCreatedNames = {ActivityNames.FORK_CREATED};
			Activity forkCreatedAC = gu.acquireMatchingActivities(forkCreatedNames).get(0);
			String[] forkPushedNames= {ActivityNames.FORK_PUSHED};
			Activity forkPushedAC = gu.acquireMatchingActivities(forkPushedNames).get(0);
			if (forkPushedAC.getDate().after(forkCreatedAC.getDate()))
				returnList .add(gu);
		}
		return returnList;
	}
	public List<GithubUser> getMatchingUsers(Collection<GithubUser> inputList, String[] activityNames, boolean isFlip) {
		List<GithubUser> returnList = new ArrayList<GithubUser>();
		for (GithubUser gu: inputList) {				
			if (gu.isMatchingActivities(activityNames) ^ isFlip)
				returnList.add(gu);
		}
		return returnList;
	}
	public List<GithubUser> getUsersWithInActiveForks(List<GithubUser> usersWithForks) {
		List<GithubUser> returnList = new ArrayList<GithubUser>();
		for (GithubUser gu: usersWithForks) {
			String[] forkCreatedNames = {ActivityNames.FORK_CREATED};
			Activity forkCreatedAC = gu.acquireMatchingActivities(forkCreatedNames).get(0);
			String[] forkPushedNames= {ActivityNames.FORK_PUSHED};
			Activity forkPushedAC = gu.acquireMatchingActivities(forkPushedNames).get(0);
			if (forkPushedAC.getDate().before(forkCreatedAC.getDate()) || forkPushedAC.getDate().equals(forkCreatedAC.getDate()))
				returnList .add(gu);
		}
		return returnList;
	}
	
	public List<GithubUser> getUsersWithForks() {
		List<GithubUser> returnList = new ArrayList<GithubUser>();
		String[] activityNames = {ActivityNames.FORK_CREATED};
		for (Map.Entry<String, GithubUser> entry: mapOfUsers.entrySet()) {
			GithubUser gu = entry.getValue();
			if (gu.isMatchingActivities(activityNames))
				returnList.add(gu);
		}
		return returnList;
	}
	
	//	size of activityNames and int[] in map should be same 
	public List<GithubUser> getConstrainSatisfiedUsers (Map<GithubUser, int[]> mapOfActCounts, String[] activityNames, CombinationActivity[] combinations) {
		List<GithubUser> returnList = new ArrayList<GithubUser>(); 
		Map<String, Integer> mapOfIndexes = computeIndexes(activityNames);
		
		for (Map.Entry<GithubUser, int[]> entry: mapOfActCounts.entrySet()) {
			int i = 0;
			for (i = 0; i < combinations.length; ++i){
				Integer arrIndex = -1;
				if ((arrIndex = mapOfIndexes.get(combinations[i].getName()))!= null && !combinations[i].matches(entry.getValue()[arrIndex]))
					break;
			}
			if (i == combinations.length)
				returnList.add(entry.getKey());
		}
		return returnList;
	}
	
	public Map<GithubUser, int[]> getUsersWithActivityCounts(String[] activityNames, Collection<GithubUser> customList) {
		
		//This is important to maintain state
		this.activityNames = activityNames;
		
		Map<GithubUser, int[] > returnMap = new HashMap<GithubUser, int[]>();
		Map<String, Integer> indexMap = computeIndexes(activityNames);
		
		if (customList == null)
			customList = mapOfUsers.values();
		
		for (GithubUser gu : customList) {
			int[] activityCounts = new int[activityNames.length];
			for (Activity ac : gu.getActivities()) {
				Integer index = null;
				if ((index = indexMap.get(ac.getName())) != null)
					activityCounts[index]++;
			}
			returnMap.put(gu, activityCounts);
		}
		return returnMap;		
	}

	private Map<String, Integer> computeIndexes(String[] arr) {
		Map<String, Integer> returnMap = new HashMap<String, Integer>();
		for (int i = 0; i < arr.length; ++i) {
			returnMap.put(arr[i],i);
		}
		return returnMap;
	}
	private int getIndex(String name, String[] activityNames) {
		for (int i = 0; i < activityNames.length; ++i) 
			if (name.equals(activityNames[i]))
				return i;
		return -1;
	}

	public void updateActivityCounts(Map<GithubUser, int[]> usersWithCounts,
			 String activityName,
			ActivityUpdater activityUpdater) {
		
		int index = getIndex(activityName,activityNames);
		if (index == -1 )
			return;
		
		for (Map.Entry<GithubUser, int[]> entry: usersWithCounts.entrySet() ){
			entry.getValue()[index] = activityUpdater.getCount(entry.getKey());
		}
		
	}
	
}
