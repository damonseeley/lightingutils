package com.damonseeley.lightingutilsdemo.core;

import net.electroland.utils.ParameterMap;

public class TimedCue extends Cue {

    public TimedCue(ParameterMap p) {
        super(p);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean ready(EventMetaData meta) {
        // TODO Auto-generated method stub
        return false;
    }
}