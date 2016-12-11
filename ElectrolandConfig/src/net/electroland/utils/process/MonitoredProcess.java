package net.electroland.utils.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MonitoredProcess implements Runnable {

    static Logger logger = Logger.getLogger(MonitoredProcess.class);

    private String command, name;
    private File runDir;
    private long startDelayMillis, restartDelayMillis;
    private boolean restartOnTermination, firstRun = true;
    private InputToOutputThread pOut, pErr;
    private Process running;
    private List<MonitoredProcessListener> listeners;
    private Date startTime;

    public MonitoredProcess(String name, String command, File runDir, long startDelayMillis, long restartDelayMillis, boolean restartOnTermination){
        this.name                 = name;
        this.command              = command;
        this.runDir               = runDir;
        this.startDelayMillis     = startDelayMillis;
        this.restartDelayMillis   = restartDelayMillis;
        this.restartOnTermination = restartOnTermination;
        this.listeners            = new ArrayList<MonitoredProcessListener>();
    }

    public String getName(){
        return name;
    }

    public Date getStartTime(){
        return startTime;
    }

    public boolean restartOnTermination(){
        return restartOnTermination;
    }

    public void addListener(MonitoredProcessListener listener){
        listeners.add(listener);
    }

    public void removeListener(MonitoredProcessListener listener){
        listeners.remove(listener);
    }

    public void notifyComplete(){
        for (MonitoredProcessListener l : listeners){
            l.completed(this);
        }
    }

    public void notifyKilled(TemplatedDateTimeKill killer){
        for (MonitoredProcessListener l : listeners){
            l.killed(this, killer);
        }
    }

    public void notifyExecuted(){
        for (MonitoredProcessListener l : listeners){
            l.executed(this);
        }
    }

    @Override
    public void run() {

        while (restartOnTermination || firstRun){

            try {

                if (firstRun) {
                    logger.info("starting " + name + " in " + startDelayMillis + " millis.");
                    Thread.sleep(startDelayMillis);
                } else {
                    logger.info("restarting " + name + " in " + restartDelayMillis + " millis.");
                    Thread.sleep(restartDelayMillis);
                }

                logger.info(" starting " + name + " now.");
                logger.info(" exec " + runDir + "\\" + command);

                running = Runtime.getRuntime().exec(command, null, runDir);
                startTime = new Date();
                notifyExecuted();
                firstRun = false;

                pOut = new InputToOutputThread(running.getInputStream(), Logger.getLogger(command), Level.INFO);
                pOut.startPiping();

                pErr = new InputToOutputThread(running.getErrorStream(), Logger.getLogger(command), Level.ERROR);
                pErr.startPiping();

                logger.info("monitoring " + name);
                running.waitFor();
                startTime = null;

                logger.info("process died. ");
                if (restartOnTermination){
                    logger.info(" restart requested.");
                } else {
                    logger.info(" and NO restart requested.");
                    // TODO: if there are no processes left: System.exit(0);
                }

            } catch (IOException e) {
                restartOnTermination = false;
                e.printStackTrace();
                System.exit(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startProcess(){
        new Thread(this).start();
    }

    public void kill(boolean restartOnTermination, TemplatedDateTimeKill killer) {
        logger.info("kill called.");
        this.restartOnTermination = restartOnTermination;
        if (restartOnTermination){
            logger.info(" restart requested.");
        }

        if (running != null){
            running.destroy();
            if (killer == null){
                notifyComplete();
            } else {
                notifyKilled(killer);
            }
        }
    }
}