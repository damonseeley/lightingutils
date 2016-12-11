package com.damonseeley.lightingutilsdemo.core;

import net.electroland.eio.Channel;

public class SensorEvent extends NorfolkEvent {

    protected Channel sourceInputChannel;

    public SensorEvent(){
        super();
    }

    public SensorEvent(Channel sourceInputChannel){
        this.sourceInputChannel = sourceInputChannel;
    }
}