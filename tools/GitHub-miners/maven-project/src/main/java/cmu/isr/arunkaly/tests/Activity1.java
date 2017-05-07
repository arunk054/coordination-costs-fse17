package cmu.isr.arunkaly.tests;

import java.util.Date;

public class Activity1{
	private Date date;
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	private String activity;
	public Activity1(String activity, Date date) {
		this.date = date;
		this.activity = activity;
	}
}
