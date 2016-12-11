package com.damonseeley.lightingutilsdemo.core;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import net.electroland.utils.ParameterMap;

public class BigShowCue extends Cue {

    private static Logger logger = Logger.getLogger(BigShowCue.class);
    private int waitMillis;
    private List<String>cues;
    private String trainChannelId;
    private Random random;

    public BigShowCue(ParameterMap p) {
        super(p);
        waitMillis      = p.getRequiredInt("waitMillis");
        cues            = p.getRequiredList("cues");
        trainChannelId  = p.getRequired("trainChannelId");
        random          = new Random();
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        logger.debug("fire big show.");
        cp.play(cues.get(random.nextInt(cues.size())));
    }

    @Override
    public boolean ready(EventMetaData meta) {
    	boolean isInactive = meta.totalSensorsEventsOverLastExcluding(60000, trainChannelId) == 0;
        return isInactive && meta.getTimeSinceLastCue(this) > waitMillis;
    }
}