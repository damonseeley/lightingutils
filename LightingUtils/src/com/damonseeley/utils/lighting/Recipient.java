package com.damonseeley.utils.lighting;

import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;


abstract public class Recipient {

    private String name;
    protected CanvasDetector[] channels;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    /** 
     * Takes the latest value from each ChannelDetectors and sends it to the
     * lights. Assumes ELUCanvas.sync(int[] pixels) was called first. 
     */
    protected void sync(){

        Byte[] b = new Byte[channels.length];
        
        for (int i = 0; i < channels.length; i++)
        {
            if (channels[i] != null){
                b[i] = channels[i].latestState;
            }
        }

        send(b);
    }

    protected void setAll(byte b){
        for (int i = 0; i < channels.length; i++)
        {
            if (channels[i] != null){
                channels[i].setValue(b);
            }
        }
    }

    public CanvasDetector[] getChannels()
    {
        return channels;
    }
    public void setChannels(CanvasDetector[] channels)
    {
        this.channels = channels;
    }

    // configure
    abstract public void configure(ParameterMap params) throws OptionException;

    // configure
    abstract public void map(int channel, CanvasDetector cd) throws OptionException;

    // send all "on" values
    abstract public void allOn();

    // send all "off" values
    abstract public void allOff();

    // protocol specific handling
    abstract public void send(Byte[] b);

    /**
     * print debugging info.
     */
    abstract public void debug();

}