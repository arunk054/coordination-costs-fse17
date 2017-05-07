package cmu.isr.arunkaly.tests;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class TestDBObject {

	private String login;
	private String _id;
	private Integer val;
	
	public Integer getVal() {
		return val;
	}
	public void setVal(Integer val) {
		this.val = val;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public List<Activity1> getActivities() {
		return activities;
	}
	public void setActivities(List<Activity1> activities) {
		this.activities = activities;
	}
	private List<Activity1> activities;
	
	public TestDBObject(String login) {
		this.login = login;
		//this._id = new ObjectId(toHex(login));
		this._id="345678aef23487";
		activities = new ArrayList<Activity1>();
		int count = 199;
		this.val = new Integer(count);
	}
	public String toHex(String arg) {
		return String.format("%02x", new BigInteger(1, arg.getBytes(Charset.forName("UTF-16"))));
	    //return arg.getBytes(Charset.defaultCharset());
	}
	
	public void addActivity(Activity1 a) {
		activities.add(a);
		System.out.println(activities.size());
	}
	
}
