package com.damonseeley.lightingutilsdemo.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JFrame;

import net.electroland.ea.Animation;
import net.electroland.utils.ElectrolandProperties;
import net.electroland.utils.OptionException;
import net.electroland.utils.ShutdownThread;
import net.electroland.utils.Shutdownable;
import net.electroland.utils.Util;
import net.electroland.utils.hours.OperatingHours;

import org.apache.log4j.Logger;	

import com.damonseeley.lightingutilsdemo.core.viz.Raster2dViz;
import com.damonseeley.lightingutilsdemo.core.viz.VizOSCListener;
import com.damonseeley.lightingutilsdemo.core.viz.VizOSCReceiver;
import com.damonseeley.lightingutilsdemo.core.viz.VizOSCSender;
import com.damonseeley.lightingutilsdemo.sound.SimpleSoundManager;
import com.damonseeley.utils.lighting.CanvasDetector;
import com.damonseeley.utils.lighting.ELUCanvas;
import com.damonseeley.utils.lighting.ELUManager;
import com.damonseeley.utils.lighting.Fixture;
import com.damonseeley.utils.lighting.ui.ELUControls;

public class Conductor implements Runnable, Shutdownable, VizOSCListener{

    private static Logger       logger = Logger.getLogger(Conductor.class);
    private Animation           eam;
    private ELUManager          elu;
    private OperatingHours      hours;
    private ClipPlayer          clipPlayer;
    private VizOSCSender        viz;
    private Thread              thread;
    private int                 fps = 30;
    private JFrame              mainControls;
    private Raster2dViz         renderArea;
    private boolean             isHeadless = false;
    private static boolean      showSensors = false;
    private Collection<Cue>     cues;
    private EventMetaData       meta;
    private String              trainChannelId;
    private String              remoteClip;
    
    public static void main(String args[]) throws OptionException, IOException{

        Conductor c = new Conductor();
        c.init(); // need a way to turn multiple args into multiple props file names- or just put them all in one file?

        if (!c.isHeadless){
            c.mainControls = new JFrame();
            c.mainControls.setBackground(Color.BLACK);
            c.mainControls.setSize(c.eam.getFrameDimensions().width + 200, 
                    c.eam.getFrameDimensions().width + 100);
            c.mainControls.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // controls
            ELUControls eluControls = new ELUControls(c.elu);
            c.mainControls.setLayout(new BorderLayout());
            c.mainControls.add(eluControls, BorderLayout.PAGE_END);

            c.renderArea = new Raster2dViz();
            c.renderArea.setPreferredSize(c.eam.getFrameDimensions());
            c.mainControls.add(c.renderArea, BorderLayout.CENTER); 
            c.mainControls.setVisible(true);

            c.mainControls.setLocation(0, 80);
            c.mainControls.setBackground(Color.black);

        }
    }

    public void init() throws OptionException, IOException{

        elu = new ELUManager();
        elu.load(new ElectrolandProperties("HeronTree-ELU2.properties"));

        ElectrolandProperties mainProps = new ElectrolandProperties("demo.properties");
        if (mainProps.getRequiredBoolean("settings", "global", "headless")) {
            isHeadless = true;
        }
        
        if (mainProps.getRequiredBoolean("settings", "global", "showsensors")) {
            showSensors = true;
        }

        eam = new Animation();
        eam.load(mainProps);
        eam.setBackground(Color.BLACK);
        fps = mainProps.getDefaultInt("settings", "global", "fps", 30);

        hours = new OperatingHours();
        hours.load(new ElectrolandProperties("hours.properties"));

        clipPlayer = new ClipPlayer(eam, new SimpleSoundManager(hours), elu, mainProps);
        new ClipPlayerGUI(clipPlayer);

        meta = new EventMetaData(mainProps.getRequiredInt("cues", "global", "maxHistoryMillis"));
        cues = new CueManager().load(meta, mainProps);
        trainChannelId = mainProps.getRequired("cues", "global", "trainChannelId");

        viz = new VizOSCSender();
        viz.load(mainProps);

        remoteClip = mainProps.getRequired("cues", "remote", "clipName"); 
        VizOSCReceiver listener = new VizOSCReceiver();
        listener.load(mainProps);
        listener.addListener(this);
        listener.start();

        start();
    }

    public void start(){


        if (thread == null){
            thread = new Thread(this);
            thread.start();
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
    }

    public void stop(){
        elu.allOff();
        elu.stop();
        thread = null;
    }

    @Override
    public void run() {
        while (thread != null){

            // ELU and visualization rendering
            long startRender = System.currentTimeMillis();

            // Practically speaking, there's only one canvas, so we don't need
            // to do this iterator. Could just get it by name.
            for (ELUCanvas canvas : elu.getCanvases().values()){

                Dimension d = eam.getFrameDimensions();
                BufferedImage frame = eam.getFrame();

                // sync with ELU
                int pixels[] = new int[d.width * d.height];
                frame.getRGB(0, 0, d.width, d.height, pixels, 0, d.width);

                if (hours.shouldBeOpenNow("lights")){
                    CanvasDetector[] detectors = canvas.sync(pixels);

                    if (renderArea != null){
                        renderArea.update(frame, detectors, elu);
                        renderArea.repaint();
                    }

                    // sync with viz
                    if (viz.isEnabled()){
                        syncViz(detectors);
                    }

                }else{
                    elu.allOff();
                }

            }

            // Cues
            for (Cue c : cues){
                if (c.ready(meta) && !(c instanceof ChannelDriven)){
                    meta.addEvent(new CueEvent(c));
                    c.fire(meta, clipPlayer);
                }
            }

            // FPS management
            try{

                long currentTime = System.currentTimeMillis();
                int renderTime = (int)(currentTime - startRender);

                int sleepTime = (int)(1000.0/fps) - renderTime;
                if (sleepTime < 1){
                    sleepTime = 1;
                }

                Thread.sleep(sleepTime);

            }catch(InterruptedException e){
                logger.error(e);
            }

            if (mainControls != null){
                mainControls.setTitle("HeronTree: fps = " + (int)elu.getMeasuredFPS());
            }
        }
    }

    // YUCK! (ELU actually has this already, and should allow getting it).
    private HashMap<String, Fixture> fixtures;
    private HashMap<String, Fixture> fixtureMap(){
        if (fixtures == null){
            fixtures = new HashMap<String, Fixture>();
            for (Fixture f : elu.getFixtures()){
                fixtures.put(f.getName(), f);
            }
        }
        return fixtures;
    }

    // very inefficient. would be nice if ELU merged RGB values smartly when
    // it has them all together.
    public void syncViz(CanvasDetector[] detectors){

        HashMap<String, RGB> fixtureColors = new HashMap<String, RGB>();

        for (CanvasDetector cd : detectors){

            String fixtureId = null;
            Integer r = null,g = null,b = null;
            for (String tag : cd.getTags()){
                if (tag.equals("red")){
                    r = Util.unsignedByteToInt(cd.getLatestState());
                } else if (tag.equals("green")){
                    g = Util.unsignedByteToInt(cd.getLatestState());
                } else if (tag.equals("blue")){
                    b = Util.unsignedByteToInt(cd.getLatestState());
                } else if (fixtureMap().containsKey(tag)){ // if this tag is a Fixture name
                    fixtureId = tag;
                }
            }

            // at this point we should have a fixture name, and a single color. need to put
            // it into fixtureColors, awaiting the other two colors.
            if (fixtureId != null){
                RGB rgb = fixtureColors.get(fixtureId);
                if (rgb == null){
                    rgb = new RGB();
                    fixtureColors.put(fixtureId, rgb);
                }
                if (r != null){
                    rgb.r = r;
                }
                if (g != null){
                    rgb.g = g;
                }
                if (b != null){
                    rgb.b = b;
                }
            }
        }

        // at this point fixtureColors should contain a color per fixture.
        HashMap<String, Color> dataToSend = new HashMap<String, Color>();

        for (String fixtureId : fixtureColors.keySet()){
            RGB rgb = fixtureColors.get(fixtureId);
            if (rgb != null){
                dataToSend.put(fixtureId, new Color(rgb.r, rgb.g, rgb.b));
            }
        }

        // 2016 commented out
        // viz.setLights(dataToSend);
    }

    // used by syncViz only.
    class RGB{
        int r,g,b;
    }


    @Override
    public void shutdown() {
        stop();
    }

    @Override
    public void setSensorState(String id, boolean isOn) {
        // do nothing (this is from VizOSCListener);
    }

    @Override
    public void setLightColor(String id, Color color) {
        // do nothing (this is from VizOSCListener);
    }

    @Override
    public void remoteInvoked() {
        clipPlayer.play(remoteClip);
    }
}