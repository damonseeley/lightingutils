package com.damonseeley.lightingutilsdemo.core;

import net.electroland.utils.ParameterMap;

public class ScreenSaverCue extends Cue {

    private boolean isSaving = false, firstRun = true;
    private int timeout, fadeout, fadein;
    private Cue[] exceptions;

    public ScreenSaverCue(ParameterMap p) {
        super(p);
        timeout = p.getRequiredInt("timeout");
        fadeout = p.getRequiredInt("fadeout");
        fadein = p.getRequiredInt("fadein");
    }

    @Override
    public void fire(EventMetaData meta, ClipPlayer cp) {
        if (isSaving){
            cp.enterScreensaverMode(fadein);
        }else{
            cp.exitScreensaverMode(fadeout);
        }
    }

    @Override
    public boolean ready(EventMetaData meta) {

        if (firstRun){

            firstRun = false;
            isSaving = true;
            return true;

        }else{
            boolean everythingInactive = meta.getTimeSinceLastCueExcluding(exceptions) > timeout;

            if (everythingInactive && !isSaving){
                isSaving = true;
                return true;
            }else if (!everythingInactive && isSaving){
                isSaving = false;
                return true;
            }else{
                return false;
            }
        }

    }

    public void setExceptions(Cue... exceptions){
        this.exceptions = exceptions;
    }
}