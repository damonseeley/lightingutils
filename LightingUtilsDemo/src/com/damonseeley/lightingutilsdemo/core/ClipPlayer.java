package com.damonseeley.lightingutilsdemo.core;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import net.electroland.ea.Animation;
import net.electroland.ea.AnimationListener;
import net.electroland.ea.Clip;
import net.electroland.ea.Sequence;
import net.electroland.eio.InputChannel;
import net.electroland.utils.ElectrolandProperties;
import net.electroland.utils.ParameterMap;

import org.apache.log4j.Logger;

import com.damonseeley.lightingutilsdemo.sound.SimpleSoundManager;
import com.damonseeley.utils.lighting.ELUManager;
import com.damonseeley.utils.lighting.Fixture;

public class ClipPlayer implements AnimationListener {

    private static Logger logger = Logger.getLogger(ClipPlayer.class);
    private Animation eam;
    private SimpleSoundManager ssm;
    private ELUManager elu;
    private Map<String, Fixture> channelToFixture;
    private Collection<Method> globalClips;

    private Timer chordTimer;
    int chordIndex;
    int chordIndexMax;
    long chordDur;

    // screensaver
    int screensaverFadeOutMillis, screensaverFadeInMillis;

    // ripple
    float rippleMultiplier, rippleDBrightness, rippleFloor;
    int rippleHold, rippleFadein, rippleFadeout;

    private enum Message {SCREENSAVER, IVASE_THROB, SSVASE_THROB, COBRA_THROB, LEAVES, SPARKLE, COBRACLOUDS}

    //private int detOffset = 2;

    public ClipPlayer(Animation eam, SimpleSoundManager ssm, ELUManager elu, ElectrolandProperties props){

        this.eam = eam;
        this.eam.addListener(this);
        this.ssm = ssm;
        this.elu = elu;
        this.ssm.load(props);
        this.configure(props);

        chordIndex = 1;
        chordIndexMax = 3;
        chordDur = 5000; //4 seconds for each chord
        chordTimer = new Timer();
        chordTimer.schedule(new chordTimerTask(), chordDur, chordDur);

        initMasterClips();
        initScreensaver();
        initInteractive();
    }


    class chordTimerTask extends TimerTask{

        public void run(){
            if (chordIndex < chordIndexMax ) {
                chordIndex++;
            } else {
                chordIndex = 1;
            }
            //logger.info("Changed chordIndex to " + chordIndex);
        }
    }

    public void configure(ElectrolandProperties props){

        channelToFixture = new HashMap<String, Fixture>();

        for (ParameterMap mappings : props.getObjects("channelFixture").values()){
            String channelId = mappings.get("channel");
            String fixtureId = mappings.get("fixture");
            Fixture fixture = this.getFixture(fixtureId); // can be null
            channelToFixture.put(channelId, fixture);
        }

        globalClips = getGlobalClips(true);

        // screensaver
        screensaverFadeInMillis     = props.getRequiredInt("cues", "screensaver", "fadein");
        screensaverFadeOutMillis    = props.getRequiredInt("cues", "screensaver", "fadeout");

        // ripple
        rippleMultiplier            = props.getRequiredDouble("cues", "ripple", "rippleMultiplier").floatValue();
        rippleHold                  = props.getRequiredInt("cues", "ripple", "hold");
        rippleFadein                = props.getRequiredInt("cues", "ripple", "fadein");
        rippleFadeout               = props.getRequiredInt("cues", "ripple", "fadeout");
        rippleDBrightness           = props.getRequiredDouble("cues", "ripple", "dbrightness").floatValue();
        rippleFloor                 = props.getRequiredDouble("cues", "ripple", "floor").floatValue();
    }



    /**
     * The boolean is irrelevent. Just trying to get it not to show up on the
     * list of methods with no args.
     * @param foo
     * @return
     */
    public Collection<Method> getGlobalClips(boolean foo){
        Method[] methods = this.getClass().getDeclaredMethods();
        ArrayList<Method> globalClips = new ArrayList<Method>();
        for (Method method:methods)
        {
            if (method.getParameterTypes().length == 0){
                globalClips.add(method);
            }
        }
        return globalClips;
    }

    public void play(String globalClipName){
        for (Method method  : globalClips){
            if (method.getName().equals(globalClipName)){
                try {
                    logger.debug("play '" + globalClipName + "'");
                    method.invoke(this);
                } catch (IllegalArgumentException e) {
                    logger.warn(e);
                } catch (IllegalAccessException e) {
                    logger.warn(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    logger.warn(e);
                }
            }
        }
    }

    public void play(String clipName, InputChannel channel){

        Fixture f = channelToFixture.get(channel.getId());

        try {

            logger.debug("play '" + clipName + "' for channel " + channel);
            Method method = this.getClass().getMethod(clipName, Fixture.class);
            if (f == null){
                method.invoke(this);
            }else{
                method.invoke(this, f);
            }
        } catch (IllegalArgumentException e) {
            logger.warn(e);
        } catch (IllegalAccessException e) {
            logger.warn(e);
        } catch (InvocationTargetException e) {
            logger.warn(e);
        } catch (NoSuchMethodException e){
            logger.warn(e);
        }
    }


    /** CLIP SETUP ****************************/

    private void initMasterClips(){
        //add interactive first so screensaver covers it up
        interactive = eam.addClip(null, null, 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 1.0f);
        screensaver = eam.addClip(null, null, 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 1.0f);

        interactive.keepAlive();
        screensaver.keepAlive();
    }

    /** SCREENSAVER LOGIC ****************************/

    public void enterScreensaverMode(int millis){
        logger.debug("enter screensaver in " + millis);
        screensaver.fadeIn(millis);
    }

    public void enterScreensaverMode(){
        logger.debug("force enter screensaver in " + screensaverFadeInMillis);
        screensaver.fadeIn(screensaverFadeInMillis);
    }
    
    public void exitScreensaverMode(int millis){
        logger.debug("exit screensaver in " + millis);
        screensaver.fadeOut(millis);
    }

    public void exitScreensaverMode(){
        logger.debug("exit screensaver in " + screensaverFadeOutMillis);
        screensaver.fadeOut(screensaverFadeOutMillis);
    }

    /**
     * When screensavers complete, this gets called. This is the chance
     * to decide what screensaver to play next.
     */
    @Override
    public void messageReceived(Object message) {

        if (message instanceof Message){

            switch((Message)message){
            case SCREENSAVER:
                //ssMultiClouds();
            	//2016
                //ssOrangeYellow();
                //ssFrozen();
                ssCandyCane();

            	break;
            case SSVASE_THROB:
            	defaultClip();
            	break;
            case COBRA_THROB:
            	defaultClip();
            case LEAVES:
            	defaultClip();
                break;
            case SPARKLE:
                ssSparkle();
                break;
            case IVASE_THROB:
            	defaultClip();
                break;
            case COBRACLOUDS:
            	defaultClip();
                break;
            }
        }
    }


    /** SCREENSAVER CLIPS ****************************/

    //nested clips
    private Clip screensaver; //master clip for all screensaver animation
    private Clip ssVase;
    private Clip ssFlora;
    private Clip ssCobras;
    private Clip ssLeaves;

    //2016
    private Clip ssAll;
    
    
    // locations of stuff
    private int vaseVMin = 0;
    private int vaseVMax = 17;
    private int elementsVMax = 175;
    private int cobrasVMin = 176;
    private int cobrasVMax = 400;
    private int leavesX = 130; //not right
    private int leavesY = 28;
    private int leavesWidth = 45;
    private int leavesHeight = 20;

    // alpha min max
    private float ssVaseThrobMax = 1.0f;
    private float ssVaseThrobMin = 0.25f;

    //overall throb timing for ss elements
    private int throbPeriod = 2000;
    private int holdPeriod = 500;


    private void initScreensaver() {
    	
    	//2016
        ssAll = screensaver.addClip(null, null, 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 1.0f);       

    	ssVase = screensaver.addClip(null, null, 0, vaseVMin, eam.getFrameDimensions().width, vaseVMax, 1.0f);       
        ssFlora = screensaver.addClip(null, null, 0, vaseVMax, eam.getFrameDimensions().width, elementsVMax-vaseVMax, 1.0f);
        ssCobras = screensaver.addClip(null, null, 0, elementsVMax, eam.getFrameDimensions().width, cobrasVMax-elementsVMax, 1.0f);
        ssLeaves = screensaver.addClip(null, null, leavesX, leavesY, leavesWidth, leavesHeight, 1.0f);

        ssVase.keepAlive();
        ssFlora.keepAlive();
        ssCobras.keepAlive();
        ssLeaves.keepAlive();
        
        //TESTS for regions
        //ssVase.addClip(null, Color.getHSBColor(.0f, 1.0f, 1.0f), 0, 0, eam.getFrameDimensions().width, vaseVMax, 1.0f);
        //ssFlora.addClip(null, Color.getHSBColor(.3f, 1.0f, 1.0f), 0, 0, eam.getFrameDimensions().width, elementsVMax, 1.0f);
        //ssCobras.addClip(null, Color.getHSBColor(.6f, 1.0f, 1.0f), 0, 0, eam.getFrameDimensions().width, cobrasVMax, 1.0f);
        //ssLeaves.addClip(null, Color.getHSBColor(.33f, 1.0f, 1.0f), 0, 0, leavesWidth, leavesHeight, 0.5f);

        //init
        //ssInitCobras();
        
        //start the constant clips

        //2016
        //ssStripeTest();
        //ssOrangeYellow();
        //ssFrozen();
        ssCandyCane();
        //ssMultiClouds(); //OR
        //ssSparkle();
        
        
    }

    public void defaultClip() {
    	// play whatever is default
    	ssFrozen();
    	
    }
    
    public void ssSparkle(){

        int delay = 0;
        ArrayList<Clip> clips = new ArrayList<Clip>();
        Clip last = null;

        for (Fixture f : elu.getFixtures()){
            if (f.getName().toLowerCase().startsWith("f") 
                || (f.getName().toLowerCase().startsWith("b") 
                		&& !f.getName().toLowerCase().startsWith("base"))){
                last = sparklet(f, delay += this.throbPeriod);
                clips.add(last);
            }
        }
        for (Clip c : clips){
            if (c == last){
                c.announce(Message.SPARKLE);
            }
        }
    }

    public Clip sparklet(Fixture fixture, int pause){

        logger.trace("start sparkle on " + fixture.getName() + " with pause of " + pause);

        Clip f = eam.addClip(null, randomHue(0.7f, 0.5f, .99f, .99f),(int)fixture.getLocation().x - 4,(int)fixture.getLocation().y - 4, 10, 10, 0.0f);

        Sequence huechange = new Sequence();
        float hueChangeRange = 0.25f;
        float huernd = hueChangeRange - (float)(Math.random() * hueChangeRange * 2);
        huechange.hueBy(huernd);
        huechange.duration(throbPeriod);
        huechange.alphaTo(0.0f);

        f.pause(pause).fadeIn(throbPeriod).pause(holdPeriod).queue(huechange);

        return f;
    }

    
    
    public void ssOrangeYellow(){

        int duration   = 60000;
        int height     = 800;

   
        Clip gradient    = ssAll.addClip(eam.getContent("gradient_1200_organgeyellow_noise"), 
                null, 
                0, -height, 
                eam.getFrameDimensions().width, height, 
                0.0f);

        int fadeInTime = 4000;
        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.yTo(-height + (height * fadeInTime/duration));
        sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        sweep.yTo(0).duration(duration-fadeInTime);

        gradient.queue(sweep).announce(Message.SCREENSAVER).fadeOut(fadeInTime*2);
    }
    
    public void ssFrozen(){

        int duration   = 30000;
        int height     = 800;

   
        Clip gradient    = ssAll.addClip(eam.getContent("frozen_anna_elsa_proc"), 
                null, 
                0, -height, 
                eam.getFrameDimensions().width, height, 
                0.0f);

        int fadeInTime = 4000;
        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.yTo(-height + (height * fadeInTime/duration));
        sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        sweep.yTo(0).duration(duration-fadeInTime);

        gradient.queue(sweep).announce(Message.SCREENSAVER).fadeOut(fadeInTime*2);
    }
    
    public void ssCandyCane(){

        int duration   = 20000;
        int height     = 768;
        int width     = 512;

   
        Clip candycane    = ssAll.addClip(eam.getContent("candy_cane_512"), 
                null, 
                0, -height, 
                width, height, 
                0.0f);

        int fadeInTime = 4000;
        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.yTo(-height + (height * fadeInTime/duration));
        sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        sweep.yTo(0).duration(duration-fadeInTime);

        candycane.queue(sweep).announce(Message.SCREENSAVER).fadeOut(fadeInTime*2);
    }
    
    
    
    
    
    
    
    public void ssMultiClouds(){

        int duration   = 60000;
        int height     = 800;

        
        //Clip clouds    = ssFlora.addClip(eam.getContent("clouds_200x800_multi_angle"), 

        Clip clouds    = ssAll.addClip(eam.getContent("clouds_200x800_multi_angle"), 
                null, 
                0, -height, 
                eam.getFrameDimensions().width, height, 
                0.0f);

        int fadeInTime = 4000;
        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.yTo(-height + (height * fadeInTime/duration));
        sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        sweep.yTo(0).duration(duration-fadeInTime);

        clouds.queue(sweep).announce(Message.SCREENSAVER).fadeOut(fadeInTime*2);
    }
    
    //2016
    public void ssStripeTest(){

        int duration   = 60000;
        int height     = 800;

      
        Clip stripe    = ssAll.addClip(eam.getContent("orange"), 
                null, 
                0, -height, 
                300, height, 
                0.0f);

        int fadeInTime = 4000;
        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.yTo(-height + (height * fadeInTime/duration));
        sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        sweep.yTo(0).duration(duration-fadeInTime);

        stripe.queue(sweep).announce(Message.SCREENSAVER).fadeOut(fadeInTime*2);
    }


    /*
    public void ssVaseThrob() {
        Clip vaseBlack = ssVase.addClip(null, Color.getHSBColor(.0f, .0f, .0f), 0, vaseVMin, eam.getFrameDimensions().width, vaseVMax, 1.0f);
        Clip vaseBlue = ssVase.addClip(null, Color.getHSBColor(.55f, .99f, .99f), 0, vaseVMin, eam.getFrameDimensions().width, vaseVMax, ssVaseThrobMin);

        Sequence slowPulseOut = new Sequence();
        slowPulseOut.hueBy(0.05f).duration(throbPeriod);
        slowPulseOut.alphaTo(ssVaseThrobMin).duration(throbPeriod);

        Sequence slowPulseIn = new Sequence();
        slowPulseIn.hueBy(-0.05f).duration(throbPeriod);
        slowPulseIn.alphaTo(ssVaseThrobMax).duration(throbPeriod);

        //delete the black background
        vaseBlack.pause(throbPeriod*2).pause(1000);
        vaseBlue.queue(slowPulseIn).queue(slowPulseOut).announce(Message.SSVASE_THROB).pause(500);    
    }
    */


    private int cobraIndex = 0;
    HashMap<Integer,Point2d> cobraLocs = new HashMap<Integer, Point2d>();

    /*
    public void ssCobraClouds(){
 
        logger.info("COBRACLOUDS");
        int duration   = 50000;
        int width     = 800;

        Clip clouds    = ssCobras.addClip(eam.getContent("clouds_800x200_blue"), 
                null, 
                -width, 0, 
                width, cobrasVMax, 
                1.0f);

        Sequence sweep = new Sequence();
        //sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.xTo(0).duration(duration);

        clouds.queue(sweep).announce(Message.COBRACLOUDS).fadeOut(3000);
    }
    */

    public void ssInitCobras() {

        ArrayList<Fixture> fixs = new ArrayList<Fixture>();
        for (Fixture f : elu.getFixtures()){
            fixs.add(f);
        }

        Point2d c1 = new Point2d();
        Point2d c2 = new Point2d();
        Point2d c3 = new Point2d();

        for (Fixture f : elu.getFixtures()){
            if (f.getName().equals("c01a")) {
                c1.x = f.getLocation().x;
                c1.y = f.getLocation().y;
                //logger.info("c1 " + c1);
            } else if (f.getName().equals("c02a")) {
                c2.x = f.getLocation().x;
                c2.y = f.getLocation().y;
                //logger.info("c2 " + c2);
            } else if (f.getName().equals("c03a")) {
                c3.x = f.getLocation().x;
                c3.y = f.getLocation().y;
                //logger.info("c3 " + c3);
            }
        }

        cobraLocs.put(0, c1);
        cobraLocs.put(1, c2);
        cobraLocs.put(2, c3);
    }

    /*
    public void ssCobraThrob() {
        //logger.info("COBRATHROB at x: " + cobraLocs.get(cobraIndex).x);

        int margin = 2;
        int width = 12;
        int height = 24;
        Clip cobraBlue = ssCobras.addClip(null, Color.getHSBColor(.6f, .99f, .99f),
                (int)(cobraLocs.get(cobraIndex).x) - margin,
                (int)(cobraLocs.get(cobraIndex).y) - margin - cobrasVMin,
                width,
                height,
                0.0f);

        Sequence slowPulseIn = new Sequence();
        slowPulseIn.hueBy(-0.05f).duration(throbPeriod);
        slowPulseIn.alphaTo(1.0f).duration(throbPeriod);

        Sequence slowPulseOut = new Sequence();
        slowPulseOut.hueBy(0.05f).duration(throbPeriod);
        slowPulseOut.alphaTo(0.0f).duration(throbPeriod);

        cobraBlue.queue(slowPulseIn).pause(holdPeriod).queue(slowPulseOut).announce(Message.COBRA_THROB);

        if (cobraIndex < 2) {
            cobraIndex++;
        } else {
            cobraIndex = 0;
        }
    }
    */






    /** BIG SHOWS AND COMBO CUES ****************************/

    /** Accents ****************************/

    private float sensorPulseMax= 0.8f;
    private float sensorPulseMin= 0.0f;

    /* public void iPulseVaseSensor() {
        Clip vasePulse = interactive.addClip(null, Color.getHSBColor(.55f, .99f, .99f), 0, vaseVMin, eam.getFrameDimensions().width, vaseVMax, sensorPulseMin);

        int dur = 150;
        Sequence pulseIn = new Sequence();
        pulseIn.alphaTo(sensorPulseMax).duration(dur);
        Sequence pulseOut = new Sequence();
        pulseOut.alphaTo(sensorPulseMin).duration(dur*2);

        vasePulse.queue(pulseIn).queue(pulseOut).fadeOut(300);    
    }
    */
    
    /* public void iPulseCobrasSensor() {
        
        //modify to 
        Clip cobrasPulse = interactive.addClip(null, Color.getHSBColor(.55f, .99f, .99f), 0, cobrasVMin, eam.getFrameDimensions().width, elementsVMax, sensorPulseMin);

        int dur = 150;
        Sequence pulseIn = new Sequence();
        pulseIn.alphaTo(sensorPulseMax).duration(dur);
        Sequence pulseOut = new Sequence();
        pulseOut.alphaTo(sensorPulseMin).duration(dur*2);

        cobrasPulse.queue(pulseIn).queue(pulseOut).fadeOut(300);    
    }
    */

    /* public void iPulseCobrasSensor(Fixture fixture) {
        
        Fixture nearestCobra = getNearestCobra(fixture.getLocation());

        int width = 10;
        int x = (int)(nearestCobra.getLocation().x - (width / 2));

        //modify to 
        Clip cobrasPulse = interactive.addClip(null, 
                Color.getHSBColor(.55f, .99f, .99f), x, 
                cobrasVMin, width, elementsVMax, sensorPulseMin);

        int dur = 150;
        Sequence pulseIn = new Sequence();
        pulseIn.alphaTo(sensorPulseMax).duration(dur);
        Sequence pulseOut = new Sequence();
        pulseOut.alphaTo(sensorPulseMin).duration(dur*2);

        cobrasPulse.queue(pulseIn).queue(pulseOut).fadeOut(300);    
    }
    */

    private Fixture getNearestCobra(Point3d point){
        Fixture nearest = null;
        for (Fixture fixture : elu.getFixtures()){
            if (isCobra(fixture)){
                
                if (nearest == null){
                    nearest = fixture;
                } else {
                    double curr = wrappedDistance(point, nearest.getLocation(), eam.getFrameDimensions().width);
                    double next = wrappedDistance(point, fixture.getLocation(), eam.getFrameDimensions().width);
                    if (curr > next){
                        nearest = fixture;
                    }
                }                
            }
        }
        return nearest;
    }

    private boolean isCobra(Fixture fixture){
        return fixture.getName().toLowerCase().startsWith("c");
    }
    
    /** Vase ****************************/

    //nested clips
    private Clip interactive; 
    private Clip iVase;
    //private Clip iFlora;
    float iVaseMin = 0.6f;
    float iVaseMax = 0.7f;

    public void initInteractive(){
        //add iVase
        iVase = interactive.addClip(null, null, 0, vaseVMin, eam.getFrameDimensions().width, vaseVMax, 1.0f);
        iVase.keepAlive();

        //iFlora = interactive.addClip(null, null, 0, vaseVMax, eam.getFrameDimensions().width, elementsVMax, 1.0f);
        //iFlora.keepAlive();

        //call vase throb
        //iVaseThrob();
    }


    /** Bigger shows ****************************/

    public void trainComing(){

        int halfWidth = eam.getFrameDimensions().width / 2;
        int halfHeight = eam.getFrameDimensions().height / 2;
        float on = 1.0f;
        float off = .0f;
        int pause = 500;
        int p1 = pause/2;
        int p2 = pause/2;

        // 630ms x 7
        ssm.playSound("trainbell");
        Clip bg = eam.addClip(Color.BLACK, 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 1.0f);
        Clip tl = eam.addClip(Color.YELLOW, 0, 0, halfWidth, halfHeight, 1.0f);
        Clip tr = eam.addClip(Color.WHITE, halfWidth, 0, halfWidth, halfHeight, 1.0f);
        Clip bl = eam.addClip(Color.WHITE, 0, halfHeight, halfWidth, halfHeight, 1.0f);
        Clip br = eam.addClip(Color.YELLOW, halfWidth, halfHeight, halfWidth, halfHeight, 1.0f);

        Sequence bing = new Sequence();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();
        bing.alphaTo(on).pause(p1).newState().alphaTo(off).pause(p2).newState();

        Sequence bong = new Sequence();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();
        bong.alphaTo(off).pause(p1).newState().alphaTo(on).pause(p2).newState();

        tl.queue(bong).fadeOut(500);
        br.queue(bong).fadeOut(500);
        tr.queue(bing).fadeOut(500);
        bl.queue(bing).fadeOut(500);

        bg.pause(8 * pause).fadeOut(500);
    }

    public void freakOut() {

        ssm.playSound("freakout");

        int stripWidth    = 20;
        int enterDuration = 1000;
        int danceDuration = 6000;
        int width = eam.getFrameDimensions().width;
        int height = eam.getFrameDimensions().height;

        // enter
        Clip bgbk = eam.addClip(Color.BLACK, 0, 0, width, height, 0.0f);
        Clip bgor = eam.addClip(Color.ORANGE, 0, 0, width, height, 0.0f);

        Sequence lightUp = new Sequence();
        lightUp.alphaTo(1.0f).duration(enterDuration);

        bgbk.queue(lightUp).pause(10000).fadeOut(500);
        bgor.queue(lightUp).pause(danceDuration).fadeOut(500);

        // dance
        Clip stage = eam.addClip(-width, 0, width * 2, height, 1.0f);
        ArrayList<Clip>oddClips = new ArrayList<Clip>();
        ArrayList<Clip>evenClips = new ArrayList<Clip>();

        for (int i = 0; i < ((width * 2) / stripWidth); i ++){
            if (i % 2 == 0){
                evenClips.add(stage.addClip(Color.RED, i * stripWidth, 0, stripWidth, height, 1.0f));
            }else{
                oddClips.add(stage.addClip(Color.RED, i * stripWidth, 0, stripWidth, height, 0.0f));
            }
        }

        // move the whole thing
        Sequence shiftRight = new Sequence();
        shiftRight.xTo(0).duration(4 * danceDuration);
        stage.pause(enterDuration).queue(shiftRight).fadeOut(500);

        Sequence flash = new Sequence();
        flash.alphaTo(1.0f).duration(150).newState();
        flash.alphaTo(0.0f).duration(150).newState();
        flash.alphaTo(1.0f).duration(150).newState();
        flash.alphaTo(0.0f).duration(250).newState();
        

        // flash events
        for (Clip c : evenClips){
            c.queue(new Sequence().pause(enterDuration + 200));
            c.queue(flash).pause(400).queue(flash).queue(new Sequence().hueBy((float)Math.random()));
            c.queue(new Sequence().pause(500));
            c.queue(flash).pause(400).queue(flash).queue(new Sequence().hueBy((float)Math.random()));
        }

        // flash odds
        for (Clip c : oddClips){
            c.queue(new Sequence().pause(enterDuration));
            c.queue(flash).pause(400).queue(flash).queue(new Sequence().hueBy((float)Math.random()));
            c.queue(new Sequence().pause(500));
            c.queue(flash).pause(400).queue(flash).queue(new Sequence().hueBy((float)Math.random()));
            c.queue(new Sequence().pause(500));
            c.queue(flash).pause(400).queue(flash).queue(new Sequence().hueBy((float)Math.random()));
        }

        // some brighter lights
        Clip brights = eam.addClip(Color.WHITE, 0, 0, width, height, 0.0f);
        
        brights.pause(2000).queue(flash).pause(400).queue(flash);
        brights.pause(2000).queue(flash).pause(400).queue(flash);
        brights.pause(750).queue(flash);
    }


    public void comboLeavesGreen(){
        ssm.playGroupRandom("7");

        int duration = 3000;
        int width = 600;
        int vLoc = 30; // start of cobras
        int vHeight = 12; // cover all cobras

        Clip parent = eam.addClip(null, null, -width + 200, vLoc, width, vHeight, 1.0f);
        parent.addClip(eam.getContent("gradient_600_greenyellow2"), null, 0, 0, width, vHeight, 1.0f);

        Sequence sweep = new Sequence();
        sweep.xTo(eam.getFrameDimensions().width).duration(duration);
        //sweep.xTo(0).duration(duration);

        parent.queue(sweep).fadeOut(500); 

    }

    /*public void allElementMulti() {
        ssm.playGroupRandom("6");
        
        Clip c1 = eam.addClip(eam.getContent("allelements_multi"), Color.getHSBColor(.0f, .99f, .0f), 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 1.0f);

        Sequence fade = new Sequence();
        fade.brightnessTo(0.0f).duration(2000);

        c1.pause(2000).queue(fade).fadeOut(1500);
    }
    */


    public void vertWavesRedMag(){
        ssm.playGroupRandom("6");
        
        //need black in here
        Clip black = eam.addClip(null, Color.getHSBColor(.0f, .99f, .0f), 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height-vaseVMax, 1.0f);

        int duration = 5000;

        int height = 600;
        //Clip c = eam.addClip(Color.getHSBColor(.9f, .8f, .7f), 0, -height, eam.getFrameDimensions().width, height, 1.0f);
        Clip c1 = black.addClip(eam.getContent("grad1200_vert_three_red_mag"), Color.getHSBColor(.4f, .99f, .99f), 0, -height, eam.getFrameDimensions().width, height, 1.0f);
        Clip c2 = black.addClip(eam.getContent("grad1200_vert_three_red_mag"), Color.getHSBColor(.5f, .99f, .99f), 0, -height, eam.getFrameDimensions().width, height, 1.0f);        //Clip c4 = eam.addClip(eam.getContent("gradientinvert"), Color.getHSBColor(.7f, .99f, .99f), 0, -height, eam.getFrameDimensions().width, height, 1.0f);

        Sequence sweep = new Sequence();
        sweep.yTo(eam.getFrameDimensions().height).duration(duration);
        sweep.hueBy(0.2f);

        c1.queue(sweep).fadeOut(500);
        c2.pause(duration-duration/4).queue(sweep).fadeOut(500);
        black.pause(duration*2).fadeOut(1500);
    }    


    public void radialOrange(){
        ssm.playGroupRandom("55");

        int duration = 3000;
        int width = 600;
        Clip c1 = eam.addClip(eam.getContent("grad1200_one_org"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);
        Clip c2 = eam.addClip(eam.getContent("grad1200_one_org"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);

        Sequence sweep = new Sequence();
        sweep.xTo(eam.getFrameDimensions().width + width).duration(duration);
        sweep.hueBy(0.2f);

        c1.queue(sweep).fadeOut(500);    
        c2.pause(duration/2).queue(sweep).fadeOut(500);    

    }

    public void radialRedMag(){
        ssm.playGroupRandom("9");

        int duration = 3000;
        int width = 600;
        Clip c1 = eam.addClip(eam.getContent("grad1200_one_red_mag"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);

        Sequence sweep = new Sequence();
        sweep.xTo(eam.getFrameDimensions().width + width).duration(duration);
        sweep.hueBy(0.2f);

        Sequence reset = new Sequence();
        reset.xTo(-width).hueBy(-0.2f).duration(0);

        c1.queue(sweep).queue(reset).queue(sweep).fadeOut(500);    
    }

    public void fadeOrangeSlow(){
        ssm.playGroupRandom("55");

        int duration = 8000;
        int width = 600;
        Clip c1 = eam.addClip(eam.getContent("orange"), Color.getHSBColor(.4f, .99f, .99f), -width/2, 0, width, eam.getFrameDimensions().height, 1.0f);

        //sweep.alphaTo(1.0f).duration(fadeInTime).newState();
        c1.pause(2500).fadeOut(duration-2500);    
    }

    public void fadeBluePurpleSlow(){
        ssm.playSound("HornCombo_C2");

        int duration = 10000;
        Clip c1 = eam.addClip(eam.getContent("bluePurple"), Color.getHSBColor(.0f, .99f, .99f), 0, 0, eam.getFrameDimensions().width, eam.getFrameDimensions().height, 0.0f);

        int pulseDur = 200;
        Sequence pulseIn = new Sequence();
        pulseIn.alphaTo(1.0f).duration(pulseDur);
        Sequence pulseOut = new Sequence();
        pulseOut.alphaTo(0.0f).duration(duration - pulseDur);

        //c1.pause(2500).fadeOut(duration-2500);    
        c1.queue(pulseIn).queue(pulseOut);    
    }

    public void radialBlueGreen3(){
        ssm.playGroupRandom("10");

        int duration = 6000;
        int width = 600;
        //Clip c1 = eam.addClip(eam.getContent("grad1200_three_blue_green"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);
        //Clip c2 = eam.addClip(eam.getContent("grad1200_three_blue_green"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);
        Clip c1 = eam.addClip(eam.getContent("grad1200_three_blue_green"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);
        Clip c2 = eam.addClip(eam.getContent("grad1200_three_blue_green"), Color.getHSBColor(.4f, .99f, .99f), -width, 0, width, eam.getFrameDimensions().height, 1.0f);

        Sequence sweep = new Sequence();
        sweep.xTo(eam.getFrameDimensions().width + width).duration(duration);
        sweep.hueBy(0.2f);

        c1.queue(sweep).fadeOut(500);    
        c2.pause(duration/2).queue(sweep).fadeOut(500);    
    }





    /** INTERACTIVE FEEDBACK CUES  ****************************/    

    /*
    public void randColor(Fixture fixture){
        red(fixture);
    }

    public void train(Fixture fixture) { 
        vertWavesRedMag();
    }

    public void red(Fixture fixture){

        ssm.playSound("002");

        Clip c = eam.addClip(eam.getContent("red"),
                (int)fixture.getLocation().x - 4,
                (int)fixture.getLocation().y - 4, 10, 10, 1.0f);

        Sequence huechange = new Sequence();
        huechange.hueBy(0.3f);

        c.queue(huechange).pause(800).fadeOut(1000);
    }

    public void randomVibraSound(){

        //ssm.playSound(getRandVibra());
        ssm.playGroupRandom(chordIndex + "");
    }

    public void randomVibraTest(){

        //ssm.playSound(getRandVibra());

        ssm.playGroupRandom(chordIndex + "");
    }
    
    */
    
    /*
    public void floraRand(Fixture fixture){

        randomVibraSound();
        //iPulseVaseSensor();
        iPulseCobrasSensor(fixture);

        /* ranges
         * 0-0.2
         * .77-1.0
         */
    
    /*
        float hueMin = 0.7f;
        float hueDelta = 0.5f;
        float randHue = hueMin + (float)((Math.random() * hueDelta));
        //logger.info("RANDOM HUE = " + randHue);
        Clip c = eam.addClip(null, Color.getHSBColor(randHue, .99f, .99f),(int)fixture.getLocation().x - 4,(int)fixture.getLocation().y - 4, 10, 10, 1.0f);

        Sequence huechange = new Sequence();
        float hueChangeRange = 0.25f;
        float huernd = hueChangeRange - (float)(Math.random() * hueChangeRange * 2);
        //logger.info("Random hue change is " + huernd);
        huechange.hueBy(huernd);
        huechange.duration(2000);
        huechange.alphaTo(0.5f);
        c.queue(huechange).fadeOut(1000);
    }
	*/

    /*
    public void testRipple(){
        floraRandRipple(this.getFixture("f12"));
    }

    public void floraRandRipple(Fixture tripped){
        randomVibraSound();

        Point3d center = tripped.getLocation();
        Color color = randomHue(0.7f, 0.5f, 1.0f, 1.0f);

        scheduleRipplet(tripped, color, 0, 100, rippleHold, rippleFadeout, 1.0f);
        iPulseCobrasSensor(tripped);

        for (Fixture fixture : elu.getFixtures()){
            if (isFlora(fixture) && fixture != tripped){
                double dist = wrappedDistance(center, fixture.getLocation(), eam.getFrameDimensions().width);
                scheduleRipplet(fixture, color, (int)(dist * rippleMultiplier), rippleFadein, rippleHold, rippleFadeout, 1.0f - (float)(rippleDBrightness * dist));
            }
        }
    }
    
    */

    public void scheduleRipplet(Fixture fixture, Color color, int pause, int fadeIn, int hold, int fadeOut, float brightness){

        logger.trace("start ripple on " + fixture.getName() + " with pause of " + pause);

        Clip f = interactive.addClip(null, color,
                             (int)fixture.getLocation().x - 4,
                             (int)fixture.getLocation().y - 4, 10, 10, 0.0f);

        Sequence reduceBrightness = new Sequence();
        reduceBrightness.alphaTo(brightness > rippleFloor ? brightness : rippleFloor).duration(fadeIn);

        f.pause(pause).queue(reduceBrightness).pause(hold).fadeOut(fadeOut);
    }

    private boolean isFlora(Fixture fixture){
        // TODO: real test: spec the flora fixture ID list in norfolk.properties.
        return fixture.getName().toLowerCase().startsWith("f") ||
               (fixture.getName().toLowerCase().startsWith("b") &&
                !fixture.getName().toLowerCase().startsWith("base"));
    }

    private static double wrappedDistance(Point3d one, Point3d two, int width){
        Point3d wrap;
        if (one.x > two.x){
            wrap = new Point3d(one.x - width, one.y, one.z);
        }else{
            wrap = new Point3d(one.x + width, one.y, one.z);
        }
        double normalDist = one.distance(two);
        double wrapDist = wrap.distance(two);

        return (normalDist < wrapDist) ? normalDist : wrapDist;
    }

    private static Color randomHue(float hueMin, float hueDelta, float brightness, float saturation){
        float randHue = hueMin + (float)((Math.random() * hueDelta));
        return Color.getHSBColor(randHue, brightness, brightness);
    }
    
    /*
    public void redRandBlur(Fixture fixture){

        randomVibraSound();
        iPulseVaseSensor();

        int dia = 64;
        Clip c = eam.addClip(eam.getContent("blurDisc_32_red"),null,(int)fixture.getLocation().x - dia/2 + detOffset,(int)fixture.getLocation().y - dia/2 + detOffset, dia, dia, 1.0f);

        Sequence huechange = new Sequence();
        //float hueShift = 0.2f;
        float hueShift = 0.8f;
        float huernd = hueShift/2 - (float)(Math.random() * hueShift);
        //logger.info("Random hue change is " + huernd);
        huechange.hueBy(huernd).duration(2000);
        huechange.alphaTo(0.5f).duration(2000);
        c.queue(huechange).fadeOut(1000);

    }

    public void redRandBlurAnim(Fixture fixture){

        randomVibraSound();
        iPulseVaseSensor();

        int dia = 64;
        Clip c = eam.addClip(eam.getContent("blurdisc_hue40"),null,(int)fixture.getLocation().x - dia/2 + detOffset,(int)fixture.getLocation().y - dia/2 + detOffset, dia, dia, 1.0f);

        Sequence huechange = new Sequence();
        //float hueShift = 0.2f;
        float hueShift = 0.8f;
        float huernd = hueShift/2 - (float)(Math.random() * hueShift);
        //logger.info("Random hue change is " + huernd);
        //huechange.hueBy(huernd).duration(2000);
        huechange.alphaTo(0.5f).duration(3000);
        c.queue(huechange).fadeOut(1000);

    }
    

    public void redRandBlurAnimNeg(Fixture fixture){

        randomVibraSound();
        iPulseVaseSensor();

        int dia = 64;
        Clip c = eam.addClip(eam.getContent("blurdisc_hue-40"),null,(int)fixture.getLocation().x - dia/2 + detOffset,(int)fixture.getLocation().y - dia/2 + detOffset, dia, dia, 1.0f);

        Sequence huechange = new Sequence();
        //float hueShift = 0.2f;
        float hueShift = 0.8f;
        float huernd = hueShift/2 - (float)(Math.random() * hueShift);
        //logger.info("Random hue change is " + huernd);
        //huechange.hueBy(huernd).duration(2000);
        huechange.alphaTo(0.5f).duration(3000);
        c.queue(huechange).fadeOut(1000);

    }
    */




/*
    public void green(Fixture fixture){

        randomVibraSound();

        Clip c = eam.addClip(eam.getContent("green"),
                (int)fixture.getLocation().x - 4,
                (int)fixture.getLocation().y - 4, 10, 10, 1.0f);

        c.pause(800).fadeOut(1000);
    }
    */




    public void displayClipCount(){
        logger.error("  total clips in memory: " + eam.countClips());
    }



    /*************************/    


    private Fixture getFixture(String id){
        for (Fixture f : elu.getFixtures()){
            if (f.getName().equals(id)){
                return f;
            }
        }
        return null;
    }
}