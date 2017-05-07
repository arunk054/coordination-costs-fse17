package cmu.isr.arunkaly.tests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleTests {

	public static void main(String[] args) throws ParseException {
		Date d = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
		
		System.out.println(formatter.format(d));
		System.out.println(d);
		String dateStr = d.toString();
		System.out.println(dateStr);
		System.out.println(df.format(d));
		Date newDate = df.parse(dateStr);
		System.out.println("new "+newDate);
		
	}
}
