package com.damonseeley.lightingutilsdemo.core;

import org.apache.log4j.Logger;

import net.electroland.eio.InputChannel;
import net.electroland.utils.ParameterMap;

public class SingletCue extends Cue implements ChannelDriven {

    private static Logger logger = Logger.getLogger(SingletCue.class); 
    private String clipName;

    public SingletCue(ParameterMap p) {
        super(p);
        clipName = p.getRequired("clipName");
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        // this method will never be called, since its sensor driven.
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp, InputChannel channel) {
        logger.debug("run single for channel " + channel);
        cp.play(clipName, channel);
    }

    @Override
    public boolean ready(EventMetaData meta) {
        return true;
    }
}