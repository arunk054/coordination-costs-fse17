package cmu.isr.arunkaly.pypi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.configs.Configurations;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MemoryOutputLayer;
import cmu.isr.arunkaly.miners.CommitsMiner;
import cmu.isr.arunkaly.miners.ContributorsMiner;
import cmu.isr.arunkaly.miners.GenericRepoMiner;
import cmu.isr.arunkaly.miners.IssueCommentsMiner;
import cmu.isr.arunkaly.miners.IssuesMiner;
import cmu.isr.arunkaly.miners.RepositoryMiner;


public class WrapperCommitsMiner {



	
	private String repo;
	private String owner;
	private boolean isWriteIfCollectionExists;
	private List<GenericRepoMiner> genericDataMiners;

	public WrapperCommitsMiner(String owner, String repo) {
		this(owner,repo,false);
	}
	public WrapperCommitsMiner(String owner, String repo, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo=repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;
		this.genericDataMiners = getListOfDataMiners();
	}
	//Ideally we want to return status
	public void invokeAllMiners(DBInterface databaseController) {
			for (GenericRepoMiner dataMiner: genericDataMiners) {
				if (!dataMiner.invokeAndWrite(databaseController)) {
					System.out.println("Error mining "+dataMiner.getCollectionName()+ " for: "+owner+"/"+repo);
				}
			}
	}	
	
	private List<GenericRepoMiner> getListOfDataMiners() {
		List<GenericRepoMiner> list = new ArrayList<GenericRepoMiner>();
		
		//Get repo start date
		RepositoryMiner rm = new RepositoryMiner(this.owner, this.repo, true);
		MemoryOutputLayer curOutput = new MemoryOutputLayer();
		rm.invokeAndWrite(curOutput);
		if (curOutput.getListOfRecords().isEmpty())
		{
			MyLogger.log("ERROR: Unable to Extract Repo Details for "+ owner+"/"+repo, LogLevel.ERROR);
			return list;
		}
		
		JSONObject repoJson = curOutput.getListOfRecords().get(0);
		String startDateStr = repoJson.getString("created_at");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		Date startDate = null;
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			MyLogger.log("ERROR: Unable to Extract Start Date: "+startDateStr+ " for "+ owner+"/"+repo, LogLevel.ERROR);
			return list;
		}
		//For consistency
		startDateStr = df.format(startDate);
		Date curDate = new Date(System.currentTimeMillis());
		Date nextDate = DateUtils.addDays(startDate, Configurations.DAYS_INCREMENT);
		while (!nextDate.after(curDate)) {
			String nextDateStr = df.format(nextDate);
			list.add(new CommitsMiner(owner, repo, this.isWriteIfCollectionExists, startDateStr, nextDateStr));
			nextDate = DateUtils.addSeconds(nextDate,1);
			startDate = nextDate;
			startDateStr = df.format(startDate);
			nextDate = DateUtils.addDays(nextDate, Configurations.DAYS_INCREMENT);
		}
		if (!curDate.equals(startDate)) {
			list.add(new CommitsMiner(owner, repo, this.isWriteIfCollectionExists, startDateStr, df.format(curDate)));
		}		
		return list;
	}
}
