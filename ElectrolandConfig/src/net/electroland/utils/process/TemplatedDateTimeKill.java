package net.electroland.utils.process;

import org.apache.log4j.Logger;

public class TemplatedDateTimeKill extends TemplatedDateTimeTask {

    static Logger logger = Logger.getLogger(TemplatedDateTimeTask.class);

    private MonitoredProcess process;
    private boolean restart;

    public TemplatedDateTimeKill(TemplatedDateTime timeTemplate, MonitoredProcess process, boolean restart) {
        super(timeTemplate);
        this.process            = process;
        this.restart            = restart;
    }

    public TemplatedDateTimeKill(TemplatedDateTimeKill original){
        super(original.getRepeatDateTimeTemplate());
        this.process            = original.process;
        this.restart            = original.restart;
    }

    @Override
    public void run() {
        process.kill(restart, this);
    }

    public String toString() {
        return "KILL " + process.getName() + (restart ? " (to be restarted)." : ".");
    }
}