package cmu.isr.arunkaly.dblayer;

import java.util.Date;

public class Activity {

	private String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	private Date date;
	
	public Activity(String name, Date date) {
		this.name = name;
		this.date = date;
	}
	//	For deserialize
	public Activity() {
		// TODO Auto-generated constructor stub
	}
	private Integer reuseCount;
	public Integer getReuseCount() {
		return reuseCount;
	}

	public void setReuseCount(Integer reuseCount) {
		this.reuseCount = reuseCount;
	}

	public void setReuseCount(int count) {
		this.reuseCount = new Integer(count);
	}
}
