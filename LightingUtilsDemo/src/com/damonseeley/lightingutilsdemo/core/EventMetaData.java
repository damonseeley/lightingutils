package com.damonseeley.lightingutilsdemo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.electroland.eio.Channel;
import net.electroland.utils.ParameterMap;

public class EventMetaData {

    public Queue<NorfolkEvent> history;
    public Map<String, Long>lastCues =  new HashMap<String, Long>();
    private long historyMaxLengthMillis;

    public EventMetaData(int historyMaxLengthMillis){
        this.historyMaxLengthMillis = historyMaxLengthMillis;
        this.history = new LinkedBlockingQueue<NorfolkEvent>();
    }

    public static void main(String args[]) throws InterruptedException{
        EventMetaData meta = new EventMetaData(60000);
        ParameterMap p = new ParameterMap();
        p.put("tripinterval", "3000");
        p.put("timeout", "3000");
        p.put("cues", "a,b");
        p.put("clipName", "foo");
        ComboCue t = new ComboCue(p);
        SingletCue s = new SingletCue(p);
        TimedCue   x = new TimedCue(p);

        System.out.println("current time: " + System.currentTimeMillis());
        meta.addEvent(new CueEvent(t));
        Thread.sleep(1000);
        meta.addEvent(new CueEvent(s));
        meta.addEvent(new SensorEvent(new TestChannel("Train")));
        Thread.sleep(1000);

        System.out.println(meta.totalCueEventsOverLast(2500));      // 2
        System.out.println(meta.totalCueEventsOverLast(1500));      // 1
        System.out.println(meta.totalEventsPastOverLast(2500));     // 3
        System.out.println(meta.totalSensorsEventsOverLast(2500));  // 1
        System.out.println(meta.totalSensorsEventsOverLastExcluding(2500,"Train"));  // 0

        System.out.println(meta.getTimeSinceLastCue(t));            // ~2000
        System.out.println(meta.getTimeSinceLastCue(s));            // ~1000
        System.out.println(meta.getTimeSinceLastCue(x));            // max int
        System.out.println(meta.getTimeSinceLastCue(s, t));         // ~1000
        System.out.println(meta.getTimeSinceLastCue(s, t, x));      // ~1000
        System.out.println(meta.getTimeSinceLastCue(s, x));         // ~1000
        System.out.println(meta.getTimeSinceLastCueExcluding(t));   // ~1000
        System.out.println(meta.getTimeSinceLastCueExcluding(s));   // ~2000
        System.out.println(meta.getTimeSinceLastCueExcluding(x));   // ~1000
        System.out.println(meta.getTimeSinceLastCueExcluding(s,x)); // ~2000
        System.out.println(meta.getTimeSinceLastCueExcluding(s,t)); // max int
    }

    public int totalEventsPastOverLast(long millis){
        int total = 0;
        long current = System.currentTimeMillis();
        for (NorfolkEvent evt : history){
            if (current - evt.eventTime < millis){
                total++;
            }
        }
        return total;
    }

    public int totalSensorsEventsOverLast(long millis){
        int total = 0;
        long current = System.currentTimeMillis();
        for (NorfolkEvent evt : history){
            if (current - evt.eventTime < millis &&
                evt instanceof SensorEvent){
                    total++;
            }
        }
        return total;
    }

    public int totalSensorsEventsOverLastExcluding(long millis, String channelName){
        int total = 0;
        long current = System.currentTimeMillis();
        for (NorfolkEvent evt : history){
            if (current - evt.eventTime < millis &&
                evt instanceof SensorEvent &&
                ((SensorEvent)evt).sourceInputChannel != null &&
                !((SensorEvent)evt).sourceInputChannel.getId().equals(channelName)) { // exclude by channel name
                    total++;
            }
        }
        return total;
    }

    public int totalCueEventsOverLast(long millis){
        int total = 0;
        long current = System.currentTimeMillis();
        for (NorfolkEvent evt : history){
            if (current - evt.eventTime < millis &&
                evt instanceof CueEvent){
                    total++;
            }
        }

        return total;
    }

    public long getTimeSinceLastCue(Cue... cues){
        long overallLast = -1;
        for (Cue c : cues){
            Long lastRecordTime = lastCues.get(getKey(c));
            if (lastRecordTime != null){
                if (lastRecordTime > overallLast){
                    overallLast = lastRecordTime;
                }
            }
        }
        return System.currentTimeMillis() - overallLast;
    }

    public long getTimeSinceLastCueExcluding(Cue... cues){

        long overallLast = -1;

        for (String cueName : lastCues.keySet()){
            boolean include = true;
            for (Cue c : cues){
                if (cueName.equals(getKey(c))){
                    include = false;
                }
            }
            if (include){
                Long lastRecordTime = lastCues.get(cueName);
                if (lastRecordTime > overallLast){
                    overallLast = lastRecordTime;
                }
            }
        }

        return System.currentTimeMillis() - overallLast;
    }

    public void addEvent(NorfolkEvent evt){

        history.add(evt);
        
        if (evt instanceof CueEvent){
            lastCues.put(getKey(((CueEvent)evt).sourceCue), System.currentTimeMillis());
        }

        if (headEventIsTooOld(history, historyMaxLengthMillis)){
            history.remove();
        }
    }

    private static boolean headEventIsTooOld(Queue<NorfolkEvent> history, long maxAgeMillis){
        if (history.size() == 0){
            return false;
        }else{
            return System.currentTimeMillis() - history.peek().eventTime > maxAgeMillis;
        }
    }

    private String getKey(Cue c){
        return c.getClass().getName();
    }
}

class TestChannel extends Channel{
    public TestChannel(String id){
        this.id = id;
    }
}