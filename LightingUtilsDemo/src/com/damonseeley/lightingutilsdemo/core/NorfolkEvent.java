package com.damonseeley.lightingutilsdemo.core;

abstract class NorfolkEvent {
    protected long eventTime;
    public NorfolkEvent(){
        eventTime = System.currentTimeMillis();
    }
}