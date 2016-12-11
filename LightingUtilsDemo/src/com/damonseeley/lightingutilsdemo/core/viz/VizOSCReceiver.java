package com.damonseeley.lightingutilsdemo.core.viz;

import java.awt.Color;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.electroland.utils.ElectrolandProperties;

import org.apache.log4j.Logger;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

public class VizOSCReceiver implements OSCListener {

    private static Logger logger = Logger.getLogger(VizOSCReceiver.class);
    private int port;
    private OSCPortIn client;
    private Collection<VizOSCListener>listeners = new ArrayList<VizOSCListener>();

    public void load(ElectrolandProperties props){
        this.port = props.getRequiredInt("settings", "osc", "receiveport");
    }
    
    public void addListener(VizOSCListener l){
        logger.debug("addListener(" + l +");");
        listeners.add(l);
    }

    public void start(){

        logger.info("start();");
        if (client != null){
            stop();
        }

        try {
            logger.info(" listening on port " + port);
            client = new OSCPortIn(port);
            client.addListener(VizOSCSender.ALL_LIGHTS, this);
            client.addListener(VizOSCSender.LIGHT, this);
            client.addListener(VizOSCSender.SENSORS, this);
            client.addListener(VizOSCSender.REMOTE, this);
            client.startListening();
        } catch (SocketException e) {
            logger.fatal(e);
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        logger.info("stop();");
        client.stopListening();
        client = null;
    }

    @Override
    public void acceptMessage(Date arriveTime, OSCMessage message) {

        for (VizOSCListener l : listeners){

            Object[] args = message.getArguments();

            if (message.getAddress().equals(VizOSCSender.LIGHT)){

                l.setLightColor((String)args[0], new Color((Integer)args[1], 
                                                           (Integer)args[2], 
                                                           (Integer)args[3]));

            } else if (message.getAddress().equals(VizOSCSender.SENSORS)){

                l.setSensorState((String)args[0], (Integer)args[1] == 0 ? false : true);

            } else if (message.getAddress().equals(VizOSCSender.ALL_LIGHTS)){

                StringBuffer sb = new StringBuffer();

                for (int i = 0; i < message.getArguments().length - 1; i += 2){

                    String id = (String)message.getArguments()[i];
                    int c     = (Integer)message.getArguments()[i+1];
                    Color color = new Color(c);

                    sb.append(id).append('[').append(color.getRed())
                      .append(',').append(color.getGreen())
                      .append(',').append(color.getBlue()).append(']').append(',');

                    l.setLightColor(id, color);
                }

                logger.trace(sb.toString());
            } else if (message.getAddress().startsWith(VizOSCSender.REMOTE)){

                logger.debug("remote play request received:" + message.getAddress());
                if ((Float)message.getArguments()[0] == 1.0f){
                    logger.debug(" invoke " + message.getAddress());
                    l.remoteInvoked();
                }
            }
        }
    }
}