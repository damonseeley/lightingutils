package com.damonseeley.lightingutilsdemo.core;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import net.electroland.eio.InputChannel;
import net.electroland.utils.ParameterMap;

public class ComboCue extends Cue implements ChannelDriven {

    private static Logger logger = Logger.getLogger(ComboCue.class);
    private int timeout, tripInterval;
    private List<String>shows;
    private Random random;

    public ComboCue(ParameterMap p) {
        super(p);
        tripInterval = p.getRequiredInt("tripinterval");
        timeout      = p.getRequiredInt("timeout");
        shows        = p.getRequiredList("cues");
        random       = new Random(); 
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        // this method will never be called, since its sensor driven.
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp, InputChannel channel) {
        // only the last channel fired is passed in
        logger.debug("run combo for channel " + channel);
        cp.play(shows.get(random.nextInt(shows.size())));
    }

    @Override
    public boolean ready(EventMetaData meta) {

        boolean isTriple      = meta.totalSensorsEventsOverLast(tripInterval * 2) > 3;
        boolean isNotTimedOut = meta.getTimeSinceLastCue(this) > timeout;

        return isTriple && isNotTimedOut;
    }
}