package com.damonseeley.lightingutilsdemo.core;

import java.util.ArrayList;
import java.util.Collection;

import net.electroland.utils.ElectrolandProperties;

public class CueManager {

    public Collection<Cue> load(EventMetaData meta, ElectrolandProperties props){
        ArrayList<Cue> cues = new ArrayList<Cue>();

        // singlets
        SingletCue singlet = new SingletCue(props.getParams("cues", "singlet"));
        cues.add(singlet);

        // triplets
        ComboCue combo = new ComboCue(props.getParams("cues", "combo"));
        cues.add(combo);

        // trains
        TrainCue train = new TrainCue(props.getParams("cues", "trains")); 
        cues.add(train);

        // bigshows
        BigShowCue bigShow = new BigShowCue(props.getParams("cues", "bigshow"));
        meta.addEvent(new CueEvent(bigShow));
        cues.add(bigShow);

        // screensavers
        ScreenSaverCue screenSaver = new ScreenSaverCue(props.getParams("cues", "screensaver"));
        screenSaver.setExceptions(screenSaver, train, bigShow);
        cues.add(screenSaver);

        return cues;
    }
}