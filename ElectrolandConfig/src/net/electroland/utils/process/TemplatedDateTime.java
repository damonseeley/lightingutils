package net.electroland.utils.process;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * A time and/or date template for recurring tasks. Currently supports templates
 * for specifying hourly, daily, and weekly recurrance in human readable form.
 * 
 * hourly: "05" (5 minutes past the hour, every hour)
 * daily:  "12:05 PM" (that time, every day)
 * weekly: "Wed, 12:00 AM" (every wednesnay at midnight)
 * 
 * @author bradley
 *
 */
public class TemplatedDateTime {

    static Logger logger = Logger.getLogger(TemplatedDateTime.class);

    public final static String HOURLY = "hourly";
    public final static String DAILY  = "daily";
    public final static String WEEKLY = "weekly";

    public final static String HOURLY_FORMAT  = "mm";
    public final static String HOURLY_EXAMPLE = "05";
    public final static String DAILY_FORMAT   = "h:mm a";
    public final static String DAILY_EXAMPLE  = "12:05 PM";
    public final static String WEEKLY_FORMAT  = "EEE, h:mm a";
    public final static String WEEKLY_EXAMPLE = "Wed, 12:59 AM";

    private String repeatRate;
    private Calendar reference;

    public TemplatedDateTime(String repeatRate, String repeatDayTime){

        this.repeatRate = repeatRate;
        Date referenceDate = null;

        if (isHourly()){
            referenceDate = parse(HOURLY_FORMAT, repeatDayTime);
        } else if (isDaily()){
            referenceDate = parse(DAILY_FORMAT, repeatDayTime);
        } else if (isWeekly()){
            referenceDate = parse(WEEKLY_FORMAT, repeatDayTime);
        } else {
            throw new RuntimeException("Unknown type: " + repeatRate);
        }

        reference = Calendar.getInstance();
        reference.setTime(referenceDate);
    }

    // TODO: (allow specifying by TemplatedDateTime.HOURLY, etc.)
//    public TemplatedDateTime(int rate, String template){
//    }

    // unit tests (remember -ea as a VM param)
    public static void main(String[] args){

        TemplatedDateTime rdt0 = new TemplatedDateTime(TemplatedDateTime.HOURLY, "04");
        assert rdt0.isHourly();
        assert rdt0.getMinutes() == 4;

        TemplatedDateTime rdt1 = new TemplatedDateTime(TemplatedDateTime.DAILY, "9:30 AM");
        assert rdt1.isDaily();
        assert rdt1.getHour() == 9;
        assert rdt1.getMinutes() == 30;
        assert rdt1.getAmPm() == Calendar.PM;
        System.out.println(rdt1.getHour() + ":" + rdt1.getMinutes());

        TemplatedDateTime rdt2 = new TemplatedDateTime(TemplatedDateTime.WEEKLY, "Mon, 12:00 AM");

        assert rdt2.isWeekly();
        assert rdt2.getDay() == Calendar.MONDAY;
        assert rdt2.getHour() == 0;
        assert rdt2.getMinutes() == 00;
        assert rdt2.getAmPm() == Calendar.AM;
    }

    public Calendar getNextDateTime(){

        Calendar now     = Calendar.getInstance();
        Calendar nextRun = Calendar.getInstance();

        // make sure comparisons during the same minute don't botch up.
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        nextRun.set(Calendar.SECOND, 0);
        nextRun.set(Calendar.MILLISECOND, 0);

        // all restarts happen on a specified minute.
        nextRun.set(Calendar.MINUTE, this.getMinutes());

        // TODO: move to separate methods
        if (this.isHourly()){

            if (nextRun.before(now) || nextRun.equals(now)){
                nextRun.add(Calendar.HOUR, 1);
            }

        } else if (this.isDaily()){

            int hour = this.getHour();
            nextRun.set(Calendar.HOUR, hour);
            nextRun.set(Calendar.AM_PM, this.getAmPm());

            if (nextRun.before(now) || nextRun.equals(now)){
                nextRun.add(Calendar.DATE, 1);
            }

        } else if (this.isWeekly()){

            int hour = this.getHour();
            nextRun.set(Calendar.HOUR, hour);
            nextRun.set(Calendar.AM_PM, this.getAmPm());

            while (nextRun.get(Calendar.DAY_OF_WEEK) != this.getDay() ||
                   nextRun.get(Calendar.DATE) == now.get(Calendar.DATE)){

                nextRun.add(Calendar.DATE, 1);
            }

        }

        return nextRun;
    }
    public int getMinutes(){
        return reference.get(Calendar.MINUTE);
    }

    public int getHour(){
        return reference.get(Calendar.HOUR);
    }

    public int getAmPm(){
        return reference.get(Calendar.AM_PM);
    }

    public int getDay(){
        return reference.get(Calendar.DAY_OF_WEEK);
    }

    public String getType(){
        if (isDaily()) {
            return DAILY;
        } else if (isHourly()) {
            return HOURLY;
        } else if (isWeekly()) {
            return WEEKLY;
        } else return null;
    }

    public static Date parse(String format, String dateParam){
        try{
            SimpleDateFormat sdt = new SimpleDateFormat(format, Locale.ENGLISH);
            sdt.setLenient(false);
            return sdt.parse(dateParam);
        }catch(ParseException e){
            logger.error("The following date formats are supported:");
            logger.error("  " + HOURLY + ":\t" + HOURLY_FORMAT + "\tExample: " + HOURLY_EXAMPLE);
            logger.error("  " + DAILY  + ":\t" + DAILY_FORMAT  + "\tExample: " + DAILY_EXAMPLE);
            logger.error("  " + WEEKLY + ":\t" + WEEKLY_FORMAT + "\tExample: " + WEEKLY_EXAMPLE);

            throw new RuntimeException(e);
        }
    }

    public boolean isHourly(){
        return HOURLY.equalsIgnoreCase(repeatRate);
    }

    public boolean isDaily(){
        return DAILY.equalsIgnoreCase(repeatRate);
    }

    public boolean isWeekly(){
        return WEEKLY.equalsIgnoreCase(repeatRate);
    }
}