package com.damonseeley.lightingutilsdemo.core.viz;

import java.awt.Color;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.electroland.utils.ElectrolandProperties;

import org.apache.log4j.Logger;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class VizOSCSender {

    private static Logger logger = Logger.getLogger(VizOSCSender.class);
    private InetAddress inetAddress;
    private int port;
    private boolean isEnabled = false;
    public final static String ALL_LIGHTS   = "/all";       // send all lights at once
    public final static String LIGHT        = "/light";     // send a single light
    public final static String SENSORS      = "/sensors";   // send a single sensor
    public final static String REMOTE       = "/remote";    // send a call from a remote OSC client


    public void load(ElectrolandProperties props){

        this.isEnabled = props.getDefaultBoolean("settings", "osc", "enabled", true);
        if (isEnabled){
            this.port = props.getRequiredInt("settings", "osc", "sendport");

            try {
                this.inetAddress = InetAddress.getByName(props.getRequired("settings", "osc", "inetAdress"));
            } catch (UnknownHostException e) {
                logger.fatal(e);
                throw new RuntimeException(e);
            }
        }
    }

    public void setLights(HashMap<String, Color> colors){
        ArrayList<Object> args = new ArrayList<Object>();
        for (String key : colors.keySet()){
            args.add(key);
            args.add(colors.get(key).getRGB());
        }
        send(ALL_LIGHTS, args);
    }

    public void setLightColor(String id, Color color){

        ArrayList<Object> args = new ArrayList<Object>();
        args.add(id);
        args.add(new Integer(color.getRed()));
        args.add(new Integer(color.getBlue()));
        args.add(new Integer(color.getGreen()));

        send(LIGHT, args);
    }

    public void setSensorState(String id, boolean isOn){
        ArrayList<Object> args = new ArrayList<Object>();
        args.add(id);
        args.add(new Integer(isOn ? 1 : 0));

        send(SENSORS, args);
    }

    public boolean isEnabled(){
        return isEnabled;
    }

    public void send(String message, Collection<Object> args){

        if (isEnabled){
            OSCMessage msg = new OSCMessage(message, args);
            OSCPortOut sender;

            try {
                sender = new OSCPortOut(inetAddress, port);
                sender.send(msg);
            } catch (SocketException e) {
                logger.error("OSC error: " + e);
            } catch (Exception e) {
                logger.error("OSC error: " + e);
            }
        }
    }
}