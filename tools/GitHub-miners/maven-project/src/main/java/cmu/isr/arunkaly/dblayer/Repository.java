package cmu.isr.arunkaly.dblayer;

import java.util.Date;

import org.bson.Document;

public class Repository {

	
	private String owner;
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getRepo() {
		return repo;
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}
	public boolean isFork() {
		return isFork;
	}
	public void setFork(boolean isFork) {
		this.isFork = isFork;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	private String repo;
	private boolean isFork;
	private Date createdAt;
	private Date updatedAt;

	public Repository(String owner, String repo, boolean isFork, Date createdAt, Date updatedAt) {
		this.owner = owner;
		this.repo = repo;
		this.isFork = isFork;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	public String getFullName() {
		return this.owner +"/"+this.repo;
	}
	
	@Override
	public int hashCode() {
		return this.getFullName().hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		Repository other = (Repository) obj;
		return this.getFullName().equals(other.getFullName());
	}
	public static Repository getObject(Document doc) {
		String fullName = doc.getString("full_name");
		int ownerIndex = fullName.indexOf("/");
		String owner = null;
		String repo = null;
		if (ownerIndex == -1) {
			Document ownerDoc = (Document) doc.get("owner");
			if (ownerDoc != null) {
				owner = ownerDoc.getString("login");
			}
		} else {
			owner = fullName.substring(0,ownerIndex);
		}
		if (owner == null)
			return null;
		
		repo = fullName.substring(ownerIndex+1);
		boolean isFork = doc.getBoolean("fork");
		String dateStr = doc.getString("created_at");
		Date createdAt = MongoDBLayer.getDateFromString(dateStr);
		dateStr = doc.getString("pushed_at");
		Date pushedAt = MongoDBLayer.getDateFromString(dateStr);
		return new Repository(owner, repo, isFork, createdAt,pushedAt);
	}
}
