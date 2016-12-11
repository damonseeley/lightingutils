package net.electroland.utils.process;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.electroland.utils.ElectrolandProperties;
import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;
import net.electroland.utils.process.io.ProcessList;

import org.apache.log4j.Logger;

// kind of hoaky, but this implements thread so it can be it's own shutdown hook.
public class ProcessManager extends Thread implements MonitoredProcessListener{

    static Logger logger = Logger.getLogger(ProcessManager.class);

    private Map <String, MonitoredProcess> processes;
    private TemplatedDateTimeScheduler scheduler;

    /**
     * This class will manage external processes. It will start them, restart
     * them if they die, or periodically restart them.
     * 
     * A couple things:
     * 
     * 1.) If double click on a .bat file, you'll get a terminal window.  If
     *     you close that window, no terminate command goes to this app, and
     *     (therefore) spawned processes will not die.
     *     
     * 2.) If you select restartOnTermination = false, and you kill the process,
     *     this app has no idea how many processes are left, so it will stay
     *     alive (doing nothing).
     *     
     * 3.) There's no check to see if you've done something dumb, like called
     *     DAILY and HOURLY restarts of the same process that would coincide.
     *     
     * 4.) There is no UI. There's no obvious way to list what processes are
     *     currently live.  Yadda, yadda.
     * 
     * @param args
     */
    public static void main(String[] args) {

        ProcessManager daemon = new ProcessManager();

        ElectrolandProperties ep = new ElectrolandProperties(args.length == 1 ? args[0] : "restart.properties");
        daemon.initScheduler(ep);
        daemon.processes = new HashMap<String, MonitoredProcess>();
        daemon.processes.putAll(daemon.startProcesses(ep));
        daemon.scheduleRestarts(ep, daemon.processes);
        Runtime.getRuntime().addShutdownHook(daemon);

        new ProcessList(daemon.processes, daemon.scheduler);
    }

    public void initScheduler(ElectrolandProperties ep){
        int poolSize = ep.getRequiredInt("settings", "global", "maxProcessThreads");
        if (scheduler == null){
            scheduler = new TemplatedDateTimeScheduler(poolSize);
        }
    }

    public Map <String, MonitoredProcess> startProcesses(ElectrolandProperties ep) {

        logger.debug("starting processes:");
        HashMap <String, MonitoredProcess>newProcs = new HashMap<String, MonitoredProcess>();
        Map <String, ParameterMap> allProcParams;
        try{
            allProcParams = ep.getObjects("process");
        } catch(OptionException e) {
            allProcParams = Collections.emptyMap();
        }
        for (String name : allProcParams.keySet()){
            logger.debug(" starting process." + name);
            ParameterMap params = allProcParams.get(name);
            MonitoredProcess mp = startProcess(name, params);

            mp.addListener(this);
            mp.startProcess();

            newProcs.put(name, mp);
        }
        return newProcs;
    }

    public static MonitoredProcess startProcess(String name, ParameterMap params){

        String command               = params.getRequired("startScript");
        String runDirFilename        = params.getRequired("rootDir");
        int startDelayMillis         = params.getDefaultInt("startDelayMillis", 0);
        int restartDelayMillis       = params.getDefaultInt("restartDelayMillis", 0);
        boolean restartOnTermination = params.getRequiredBoolean("restartOnTermination");

        return new MonitoredProcess(name, command, new File(runDirFilename), startDelayMillis, restartDelayMillis, restartOnTermination);
    }

    public void scheduleRestarts(ElectrolandProperties ep, Map <String, MonitoredProcess> processes) {

        logger.debug("starting restartTimers:");
        Map <String, ParameterMap> allRestartParams;
        try{
            allRestartParams = ep.getObjects("restart");
        } catch(OptionException e) {
            allRestartParams = Collections.emptyMap();
        }
        for (String name : allRestartParams.keySet()){
            logger.debug("  starting restart." + name);
            ParameterMap params = allRestartParams.get(name);
            scheduleRestart(params, processes);
        }
    }

    public void scheduleRestart(ParameterMap params, Map <String, MonitoredProcess> processes) {

        String repeat             = params.getRequired("repeat");
        String repeatDayTime      = params.getRequired("repeatDayTime");
        String processName        = params.getRequired("process");

        MonitoredProcess process  = processes.get(processName);
        TemplatedDateTime refDate = new TemplatedDateTime(repeat, repeatDayTime);

        scheduler.schedule(new TemplatedDateTimeKill(refDate, process, true));
    }

    public void shutdown() {

        logger.info("cancelling all restart timers.");
        scheduler.shutdownNow();

        logger.info("killing all processes.");
        for (MonitoredProcess proc : processes.values()){
            proc.kill(false, null);
        }
    }

    /**
     * executed by shutdownHook
     */
    public void run(){
        logger.info("system shutdown called...");
        shutdown();
    }

    @Override
    public void executed(MonitoredProcess process) {
        // do nothing for now.
    }

    @Override
    public void completed(MonitoredProcess process) {
        // do nothing for now.
    }

    @Override
    public void killed(MonitoredProcess process, TemplatedDateTimeKill killer) {
        TemplatedDateTimeKill nextKill = new TemplatedDateTimeKill(killer);
        if (process.restartOnTermination()){
            scheduler.schedule(nextKill);
        }
    }
}