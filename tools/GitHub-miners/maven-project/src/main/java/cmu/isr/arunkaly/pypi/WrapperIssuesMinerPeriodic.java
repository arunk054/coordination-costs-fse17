package cmu.isr.arunkaly.pypi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import cmu.isr.arunkaly.LogLevel;
import cmu.isr.arunkaly.MyLogger;
import cmu.isr.arunkaly.dblayer.DBInterface;
import cmu.isr.arunkaly.dblayer.MemoryOutputLayer;
import cmu.isr.arunkaly.miners.ContributorsMiner;
import cmu.isr.arunkaly.miners.GenericRepoMiner;
import cmu.isr.arunkaly.miners.IssueCommentsMiner;
import cmu.isr.arunkaly.miners.IssueCommentsMinerPeriodic;
import cmu.isr.arunkaly.miners.IssuesMiner;
import cmu.isr.arunkaly.miners.IssuesMinerPeriodic;
import cmu.isr.arunkaly.miners.RepositoryMiner;
import cmu.isr.arunkaly.miners.TimeBasedMiner;


public class WrapperIssuesMinerPeriodic {

	private String repo;
	private String owner;
	private boolean isWriteIfCollectionExists;

	public WrapperIssuesMinerPeriodic(String owner, String repo) {
		this(owner,repo,false);
	}
	public WrapperIssuesMinerPeriodic(String owner, String repo, boolean isWriteIfCollectionExists) {
		this.owner = owner;
		this.repo=repo;
		this.isWriteIfCollectionExists = isWriteIfCollectionExists;

	}
	private void performMining(boolean isComments, String sinceDateStr, DBInterface databaseController){
		GenericRepoMiner genericDataMiner = getGenericDataMiner(isComments);
		((TimeBasedMiner)genericDataMiner).setDateISOSinceStr(sinceDateStr);
		Date curDate = new Date(System.currentTimeMillis());
		do {
			Date sinceDate = null;
			if (!genericDataMiner.invokeAndWrite(databaseController)) {
				MyLogger.log("Error mining "+genericDataMiner.getCollectionName()+ " for: "+owner+"/"+repo);
				if (!isComments) {
					return;
				}
				//increment the date by a day and try again, until we reach current day.
				sinceDate = DateUtils.addDays(getDate(sinceDateStr),1);
				MyLogger.log("RETRYING with a new date "+sinceDate);
				if (sinceDate.after(curDate)) {
					MyLogger.log("Aborting RETRY since date past current date");
					return;
				}
			}
			if (sinceDate == null) {
				JSONObject lastObj = genericDataMiner.getLastJSONObj();
				if (lastObj == null){
					return;
				}
				sinceDateStr = lastObj.getString("updated_at");
				sinceDate = getDate(sinceDateStr);
				sinceDate = DateUtils.addSeconds(sinceDate, 1);
			}
			sinceDateStr = getDateStr(sinceDate);
			genericDataMiner = getGenericDataMiner(isComments, sinceDateStr);
		}while(true);
	}
	//Ideally we want to return status
	public void invokeAllMiners(DBInterface databaseController) {
		//for consistency
		String sinceDateStr  = getDateStr(getDate(getRepoStartDateStr()));
		performMining(false,sinceDateStr,databaseController);
		performMining(true,sinceDateStr,databaseController);
	}	

	private String getDateStr(Date sinceDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		return df.format(sinceDate);
	}
	private GenericRepoMiner getGenericDataMiner(boolean isComments) {
		if (isComments) {
			return new IssueCommentsMinerPeriodic(this.owner,this.repo,this.isWriteIfCollectionExists);
		} else {
			return new IssuesMinerPeriodic(this.owner,this.repo,this.isWriteIfCollectionExists);
		}
	}

	private GenericRepoMiner getGenericDataMiner(boolean isComments, String sinceDateStr) {
		if (isComments) {
			return new IssueCommentsMinerPeriodic(this.owner,this.repo,this.isWriteIfCollectionExists,sinceDateStr);
		} else {
			return new IssuesMinerPeriodic(this.owner,this.repo,this.isWriteIfCollectionExists,sinceDateStr);
		}
	}
	private Date getDate(String str) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		Date startDate = null;
		try {
			startDate = df.parse(str);
		} catch (ParseException e) {
			MyLogger.log("ERROR: Unable to Extract Start Date: "+str+ " for "+ owner+"/"+repo, LogLevel.ERROR);
			return null;
		}
		return startDate;
	}
	private String getRepoStartDateStr() {
		//Get repo start date
		RepositoryMiner rm = new RepositoryMiner(this.owner, this.repo, true);
		MemoryOutputLayer curOutput = new MemoryOutputLayer();
		rm.invokeAndWrite(curOutput);
		if (curOutput.getListOfRecords().isEmpty())
		{
			MyLogger.log("ERROR: Unable to Extract Repo Details for "+ owner+"/"+repo, LogLevel.ERROR);
			return null;
		}
		JSONObject repoJson = curOutput.getListOfRecords().get(0);
		String startDateStr = repoJson.getString("created_at");
		return startDateStr;

	}

}
