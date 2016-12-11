package net.electroland.utils.hours;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import net.electroland.utils.ElectrolandProperties;
import net.electroland.utils.ParameterMap;

import com.luckycatlabs.sunrisesunset.dto.Location;

public class OperatingHours {

    private Map<String, OperatingProcess> processHours;
    private boolean debug;

    public void load(ElectrolandProperties props){

        debug = props.getDefaultBoolean("settings", "global", "debug", false);
        TimeZone timeZone = TimeZone.getTimeZone(props.getRequired("settings", "global", "timezone"));

        double lat = props.getRequiredDouble("settings", "global", "latitude");
        double lng = props.getRequiredDouble("settings", "global", "longitude");
        Location location = new Location(lat, lng);

        processHours = new HashMap<String, OperatingProcess>();

        for (String procName : props.getObjectNames("process")){
            ParameterMap procParams = props.getParams("process", procName);
            String open = procParams.get("open");
            String close = procParams.get("close");
            processHours.put(procName, new OperatingProcess(procName, timeZone, location, open, close));
        }
    }

    public static void main(String args[]){
        OperatingHours o = new OperatingHours();
        o.load(new ElectrolandProperties("hours.properties"));
        long reference = System.currentTimeMillis() + 60 * 60 * 1000 * 5;
        System.out.println("reference: " + new Date(reference));
        System.out.println("is sound open? " + o.shouldBeOpen("sound", reference));
        System.out.println("are lights open? " + o.shouldBeOpen("lights", reference));
    }

    public boolean shouldBeOpen(String id, long time){
        if (debug == true){
            return true;
        }

        OperatingProcess op = processHours.get(id);

        if (op != null){
            return op.shouldBeOpen(time);
        }else{
            throw new RuntimeException("no hours available for '" + id + "'");
        }        
    }
    
    public boolean shouldBeOpenNow(String id){
        return shouldBeOpen(id, System.currentTimeMillis());
    }
}