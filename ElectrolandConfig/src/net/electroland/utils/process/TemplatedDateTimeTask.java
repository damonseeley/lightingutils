package net.electroland.utils.process;


abstract public class TemplatedDateTimeTask implements Runnable {

    private TemplatedDateTime timeTemplate;

    public TemplatedDateTimeTask(TemplatedDateTime timeTemplate){
        this.timeTemplate = timeTemplate;
    }

    final public TemplatedDateTime getRepeatDateTimeTemplate(){
        return timeTemplate;
    }
}