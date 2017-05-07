package cmu.isr.arunkaly.tests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SmallTests {

	public static void main(String[] args) throws ParseException {
		String startDateStr = "2015-03-07T19:09:29";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		df.setTimeZone(tz);
		Date startDate = null;
		Date newDate = df.parse(startDateStr);
		System.out.println("n "+newDate);
		Date curDate = new Date(System.currentTimeMillis());
		System.out.println(df.format(newDate));
	}
}
