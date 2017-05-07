package cmu.isr.arunkaly.dblayer;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bson.Document;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import cmu.isr.arunkaly.MyLogger;

public class GithubUser {

	//For deserialize
	public GithubUser() {
		// TODO Auto-generated constructor stub
	}
	private String login;
	private String fullName;
	
	public String getFullName() {
		return fullName;
	}
	
	//Not using get intentionally
	public List<Activity> acquireMatchingActivities(String[] activityNames) {
		List<Activity> returnActivities = new ArrayList<Activity>();
		for (Activity ac: activities) {
			for (String name: activityNames) {
				if (ac.getName().equals(name)) {
					returnActivities.add(ac);
					break;
				}
			}
		}
		return returnActivities;
		
	}

	@Override
	public boolean equals(Object obj) {
		return this.getLogin().equals(((GithubUser)obj).getLogin());
	}
	
	@Override
	public int hashCode() {
		return this.login.hashCode();
	}
	
	public boolean isMatchingActivities(String[] activityNames) {
		
		for (Activity ac: activities) {
			for (String name: activityNames) {
				if (ac.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
		
	}

	public void setFullName(String fullName) {
		if (this.fullName == null || this.fullName.isEmpty())
			this.fullName = (fullName == null)?null:fullName.replace(',', ' ');
	}

	public String getLogin() {
		return login;
	}

	
	public void setLogin(String login) {
		this.login = login;
	}

	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	private List<Activity> activities;
	private Set<String> emails;
	private List<String> emailsFromProfile;
	
		
	public List<String> getEmailsFromProfile() {
		return emailsFromProfile;
	}

	public Set<String> getEmails() {
		return emails;
	}

	public void setEmails(Set<String> emails) {
		this.emails = emails;
	}

	public GithubUser(String login){
		if (login == null || login.isEmpty())
			throw new NullPointerException();
		this.login = login;
		this.fullName = null;
		this.activities = new ArrayList<Activity>();
		this.emails = new HashSet<String>();
	}
	
	public void addActivity(Activity a) {
		this.activities.add(a);
	}
	
	public void mergeAnotherUser(GithubUser other) {
		this.activities.addAll(other.getActivities());
		this.emails.addAll(other.emails);
	}

	public void addEmail(String emailAuthor) {
		if (emailAuthor!=null && !emailAuthor.isEmpty())
			emails.add(emailAuthor);			
	}

	public static GithubUser getObject(Document existingDoc) {
		try {
			existingDoc = (Document) existingDoc.get("user");
			ObjectMapper mapper = new ObjectMapper();
			DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
			mapper.setDateFormat(df);
			GithubUser gu = mapper.readValue(existingDoc.toJson().toString(), GithubUser.class);
			return gu;
			/*String login = existingDoc.getString("login");
			GithubUser gu = new GithubUser(login);
			
			String fullName = existingDoc.getString("fullName");
			gu.setFullName(fullName);
			
			List activities = (List)existingDoc.get("activities");
			List<Activity> activityObjs = new ArrayList<Activity>();
			for (Document acDoc: activities) {
				Activity ac = new Activity();
			}
				
			gu.addActivities(activities);
			
			List emails = (List)existingDoc.get("emails");
			gu.addEmails(emails);
			return gu;*/
		} catch (Exception e) {
			e.printStackTrace();
			MyLogger.logError("FATAL ERROR: Cannot convert Document to GithubUser : "+existingDoc.toJson());
		}
		return null;
	}



	public void setEmailsFromProfile(List<String> listOfEmails) {
		this.emailsFromProfile = listOfEmails;
		
	}

	public String getEmailsFromProfileAsString() {
		return getStringFromList(this.emailsFromProfile);
	}
	
	private String getStringFromList(Collection<String> list) {
		if (list == null || list.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (String s : list)
			sb.append(s+";");
		return sb.substring(0, sb.length()-1);
	}

	public String getEmailsFromCommitsAsString() {
		return getStringFromList(this.emails);
	}
	
}
