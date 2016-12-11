package com.damonseeley.lightingutilsdemo.core;

import java.util.List;
import java.util.Random;

import net.electroland.eio.InputChannel;
import net.electroland.utils.ParameterMap;

public class TrainCue extends Cue implements ChannelDriven {

    private List<String> shows;
    private int timeout;
    private Random random;

    public TrainCue(ParameterMap p) {
        super(p);
        shows   = p.getRequiredList("cues");
        timeout = p.getRequiredInt("timeout");
        random  = new Random();
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        // this method will never be called, since its sensor driven.
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp, InputChannel channel) {
        cp.play(shows.get(random.nextInt(shows.size())));
    }

    @Override
    public boolean ready(EventMetaData meta) {
        boolean isNotTimedOut = meta.getTimeSinceLastCue(this) > timeout;
        return isNotTimedOut;
    }
}