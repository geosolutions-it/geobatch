package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeTests {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSSz");
 public static void main(String [] args) throws ParseException{
     
     GregorianCalendar gc=new GregorianCalendar(1970,Calendar.JANUARY,1,0,0,0);
     gc.setTimeZone(TimeZone.getTimeZone("GMT+0"));
     System.out.println("TIME_1:"+gc.getTimeInMillis()/1000+" timeZone "+gc.getTimeZone());
     gc.clear();
     gc.set(70,Calendar.JANUARY,1,0,0,0);
     gc.setTimeZone(TimeZone.getTimeZone("GMT+0"));
     System.out.println("TIME_2:"+gc.getTime()+" timeZone "+gc.getTimeZone());
     gc.clear();
     Calendar gc2=new GregorianCalendar(1900,0,1,0,0,0);
     gc2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
     sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
     System.out.println("TIME_3:"+sdf.format(gc2.getTime()));
     gc.set(0,1,1);
     System.out.println("TIME_4:"+gc2.getTimeInMillis()/1000);
     
 }
}
