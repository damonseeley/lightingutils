package net.electroland.utils.process;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Schedules tasks. Unlike ScheduledThreadPoolExecutor (which reschedules on
 * recurring, even intervals), this should work across things like daylight
 * savings time or updates to the system clock. It recalculates the time 
 * required to get to the next templated time each iteration. See
 * TemplatedDateTime.
 * 
 * @author bradley
 *
 */
public class TemplatedDateTimeScheduler {

    static Logger logger = Logger.getLogger(TemplatedDateTimeScheduler.class);

    private ScheduledThreadPoolExecutor scheduler;

    public TemplatedDateTimeScheduler(int maxSimultaneousTasks) {
        scheduler = new ScheduledThreadPoolExecutor(maxSimultaneousTasks);
    }

    public void schedule(TemplatedDateTimeTask task){
        Date nextRestart = task.getRepeatDateTimeTemplate().getNextDateTime().getTime();
        logger.info("scheduling " + task + " at " + nextRestart);
        scheduler.schedule(task, nextRestart.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    
    public void shutdownNow() {
        scheduler.shutdownNow();
    }

    public long getTaskCount(){
        return scheduler.getTaskCount();
    }

    public BlockingQueue<Runnable> getActive(){
        return scheduler.getQueue();
    }
}