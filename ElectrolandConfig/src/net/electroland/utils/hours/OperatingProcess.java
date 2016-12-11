package net.electroland.utils.hours;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.electroland.utils.process.TemplatedDateTime;

import org.apache.log4j.Logger;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class OperatingProcess {

    private static Logger logger = Logger.getLogger(OperatingProcess.class);
    protected String openingStr, closingStr;
    protected long openingTime, closingTime;
    protected Location location;
    protected TimeZone timeZone;
    protected String procName;

    /**
     * id is the name of the thing you want to test an opening hour for.
     * 
     * openingStr and closingStr use the following formats:
     * 
     *   02:00PM
     *   12:00AM
     *   sunset
     *   sunrise
     *   sunset + 90
     *   sunrise - 10
     *   
     * The latter two are adding/subtracting minutes
     * 
     * @param id
     * @param openingStr
     * @param closingStr
     */
    public OperatingProcess(String procName, TimeZone timeZone, Location location, String openingStr, String closingStr){
        this.procName = procName;
    	this.timeZone   = timeZone;
        this.location   = location;
        this.openingStr = openingStr;
        this.closingStr = closingStr;
        
    }

    public boolean shouldBeOpen(long time){

        if (time < openingTime){
            // before hours
            return false;
        }else if (time > closingTime) {

            // after hours
            openingTime = set(openingStr);
            closingTime = set(closingStr);

            if (openingTime > closingTime){
                openingTime -= 24 * 60 * 60 * 1000;
            }

            logger.info(procName + ": today's opening: " + new Date(openingTime));
            logger.info(procName + ": today's closing: " + new Date(closingTime));

            return false;
        }else{
            return true;
        }
    }

    protected long set(String time){

        if (time.toLowerCase().startsWith("sunrise")){

            Calendar gc  = GregorianCalendar.getInstance();
            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, timeZone);
            Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(gc);

            long millis = sunrise.getTimeInMillis() + getOffsetMinutes(time);
            return millis;

        }else if (time.toLowerCase().startsWith("sunset")){

            Calendar gc  = GregorianCalendar.getInstance(timeZone);
            SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, timeZone);
            Calendar sunset  = calculator.getOfficialSunsetCalendarForDate(gc);

            long millis = sunset.getTimeInMillis() + getOffsetMinutes(time);
            return millis;

        }else{
            TemplatedDateTime t = new TemplatedDateTime(TemplatedDateTime.DAILY, time);

            long millis = t.getNextDateTime().getTimeInMillis();
            return millis;
        }
    }

    protected int getOffsetMinutes(String time){
        if (time.indexOf('+') != -1){
            return new Integer(time.substring(time.indexOf('+') + 1, time.length()));
        }else if (time.indexOf('-') != -1){
            return -1 * new Integer(time.substring(time.indexOf('-') + 1, time.length()));
        }else{
            return 0;
        }
    }
}