package com.damonseeley.lightingutilsdemo.core;

import net.electroland.utils.ParameterMap;


abstract public class Cue {

    public String id;

    public Cue(ParameterMap p){}

    abstract public boolean ready(EventMetaData meta);

    abstract public void fire(EventMetaData meta, ClipPlayer cp);
}