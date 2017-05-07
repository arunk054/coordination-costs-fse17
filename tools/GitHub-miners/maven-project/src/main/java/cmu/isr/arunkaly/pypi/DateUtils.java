package cmu.isr.arunkaly.pypi;

import java.util.Calendar;
import java.util.Date;

//Adopted from :http://stackoverflow.com/questions/428918/how-can-i-increment-a-date-by-one-day-in-java
class DateUtils
{
    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
    public static Date addSeconds(Date date, int seconds)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds); //minus number would decrement the days
        return cal.getTime();
    }
}
