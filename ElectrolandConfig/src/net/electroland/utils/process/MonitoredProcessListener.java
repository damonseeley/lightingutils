package net.electroland.utils.process;

public interface MonitoredProcessListener {
    //TODO:
    // replace below with: public void executed(MonitoredProcess process, TemplatedDateTimeStart starter);
    public void executed(MonitoredProcess process);
    public void completed(MonitoredProcess process);
    public void killed(MonitoredProcess process, TemplatedDateTimeKill killer);
}