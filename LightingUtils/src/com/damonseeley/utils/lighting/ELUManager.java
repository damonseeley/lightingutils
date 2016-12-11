package com.damonseeley.utils.lighting;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.vecmath.Point3d;

import net.electroland.utils.ElectrolandProperties;
import net.electroland.utils.FrameRateRingBuffer;
import net.electroland.utils.OptionException;
import net.electroland.utils.ReferenceDimension;

import org.apache.log4j.Logger;

public class ELUManager implements Runnable, TestSuiteCompletionListener {

    // TODO: move into an Enum.
    final public static String FIXTURE = "fixture"; 
    final public static String FIXTURE_TYPE = "fixtureType"; 
    final public static String FPS = "fps"; 
    final public static String RECIPIENT = "recipient"; 
    final public static String DETECTOR = "detector"; 
    final public static String CANVAS = "canvas"; 
    final public static String CANVAS_FIXTURE = "canvasFixture"; 
    final public static String TEST = "test";
    final public static String TEST_SUITE = "testSuite";

    private static Logger logger = Logger.getLogger(ELUManager.class);    

    private int fps;

    private Map<String, Recipient> recipients;
    private Map<String, ELUCanvas>canvases;
    private Map<String, Test>tests;
    private Map<String, TestSuite>suites;
    private Map<String, Fixture>fixtures;

    private List<ConfigurationListener> configListeners; 

    private Thread thread;
    private boolean isRunning = false;
    private boolean isRunningTest = false;
    private int autostart = -1;

    // assume a general 45 fps over 10 seconds.
    private FrameRateRingBuffer fpsBuffer = new FrameRateRingBuffer(45);

    // Unit test.  Does sweep continuously.
    public static void main(String args[])
    {
        try {

            ELUManager elu = new ELUManager();
            boolean isOn = true;
            String lastFile = "lights.properties";

            Map<String,Integer> commands = new HashMap<String,Integer>();
            commands.put("start", 0);
            commands.put("stop", 1);
            commands.put("fps", 2);
            commands.put("allon", 3);
            commands.put("alloff", 4);
            commands.put("list", 5);
            commands.put("ls", 5);
            commands.put("load", 6);
            commands.put("l", 6);
            commands.put("test", 7);
            commands.put("quit", 8);
            commands.put("q", 8);
            commands.put("on", 9);
            commands.put("off", 10);
            commands.put("set", 11);

            while(isOn)
            {
                try{
                    System.out.print(">");

                    java.io.BufferedReader stdin = 
                            new java.io.BufferedReader(
                                    new java.io.InputStreamReader(System.in));

                    String input[] = stdin.readLine().split(" ");
                    Integer i = commands.get(input[0].toLowerCase());

                    if (i == null || input[0] == "?"){
                        System.out.println("unknown command " + input[0]);
                        System.out.println("--");
                        System.out.println("The following commands are valid:");
                        System.out.println("\tload [light properties file name]");
                        System.out.println("\tlist");
                        System.out.println("\tstart");
                        System.out.println("\tstop");
                        System.out.println("\tfps");
                        System.out.println("\tfps [desired fps]");
                        System.out.println("\tallon");
                        System.out.println("\talloff");
                        System.out.println("\ton [tag]");
                        System.out.println("\toff [tag]");
                        System.out.println("\tset [tag] [value 0-255]");
                        System.out.println("\ttest [testSuite]");
                        System.out.println("\tquit");
                    }else{
                        switch(i.intValue()){
                        case(0):
                            elu.start();
                            break;
                        case(1):
                            elu.stop();
                        break;
                        case(2):
                            if (input.length == 1)
                                System.out.println("Current measured fps = " + elu.getMeasuredFPS());
                            else{
                                try{
                                    int fps = Integer.parseInt(input[1]);
                                    if (fps > 0)
                                        elu.fps = fps;
                                    else
                                        System.out.println("Illegal fps: " + input[1]);
                                }catch(NumberFormatException e)
                                {
                                    System.out.println("Illegal fps: " + input[1]);
                                }
                            }
                            break;
                        case(3):
                            elu.allOn();
                            break;
                        case(4):
                            elu.allOff();
                            break;
                        case(5):
                            elu.debug();
                            break;
                        case(6):
                            if (input.length == 1)
                                try{
                                    elu.load(lastFile);
                                }catch(OptionException e){
                                    e.printStackTrace();
                                }
                            else
                                try{
                                    lastFile = input[1];
                                    elu.load(input[1]);
                                }catch(OptionException e){
                                    e.printStackTrace();
                                }
                            break;
                        case(7):
                                if (input.length == 2)
                                    elu.runTest(input[1]);
                                else
                                    System.out.println("usage: run [testSuite]");
                            break;
                        case(8):
                            elu.stop();
                            isOn = false;
                            break;
                        case(9):
                            elu.on(input[1]);
                            break;
                        case(10):
                            elu.off(input[1]);
                            break;
                        case(11):
                            if (input.length == 3)
                            {
                                try{
                                    int val = Integer.parseInt(input[2]);
                                    if (val > -1 && val < 256)
                                        elu.set(input[1],(byte)val);
                                    else
                                        System.out.println("usage: set [tag] [value(0-255)]");
                                }catch(NumberFormatException e)
                                {
                                    System.out.println("usage: set [tag] [value(0-255)]");
                                }
                            }else
                            {
                                System.out.println("usage: set [tag] [value(0-255)]");
                            }
                            break;
                        }
                    }
                }catch (java.io.IOException e){
                    logger.error(e);
                }
            }
        } catch (OptionException e) {
            logger.error(e);
        }
    }

    public final void run()
    {
        long targetDelay = (int)(1000.0 / fps);

        while (isRunning)
        {
            // record start time
            long start = System.currentTimeMillis();

            // sync all canvases to recipients
            this.syncAllLights();

            // how long did it take to execute?
            long duration = System.currentTimeMillis() - start;
            
            long delay = duration > targetDelay ? 0 : targetDelay - duration;

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
        thread = null;
    }

    /** 
     * Start autosyncing.  Will sync the latest array sent to each canvas with
     * the real world lights.
     */
    public final void start()
    {
        if (!isRunning && !isRunningTest){
            isRunning = true;
            if (thread == null){
                thread = new Thread(this);
                thread.start();
            }
        }
    }

    /**
     * stop autosync
     */
    public final void stop()
    {
        isRunning = false;
    }

    /**
     * Forces a synchronization of all canvases with all recipients.  This is
     * what run() calls- but you can call it on your own if you don't want
     * ELU to be it's own thread.
     */
    public void syncAllLights()
    {
        for (Recipient r : recipients.values())
        {
            r.sync();
        }
        fpsBuffer.markFrame();
    }

    /**
     * get the FPS that was requested by either lights.properties or overridden
     * using setTargetFPS.  This is the frame rate the system will do it's best
     * to achieve.
     * 
     * @return
     */
    public int getTargetFPS()
    {
        return fps;
    }

    /** 
     * Set the FPS that the system should ATTEMPT to achieve. FPS is translated
     * to a millisecond delay that is the expected duration of a frame.  During
     * each frame execution, the time do execute the frame is measured.  If the
     * time is shorter than the expected duration, the ELU thread will sleep
     * for the difference.  If the time is longer than the expected duration,
     * a warning will go out that frame rate has dropped, and immediate 
     * execution of the next frame will begin.
     * @param fps
     */
    public void setTargetFPS(int fps)
    {
        this.fps = fps;
    }

    /**
     * Return the empirically measured frame rate.
     * @return
     */ 
    public double getMeasuredFPS()
    {
        return this.fpsBuffer.getFPS();
    }
    
    /**
     * Turn all channels on all fixtures on.
     */
    public void allOn()
    {
        for (Recipient r : recipients.values())
        {
            r.allOn();
        }
    }

    /**
     * Turn all channels on all fixtures off.
     */
    public void allOff()
    {
        for (Recipient r : recipients.values())
        {
            r.allOff();
        }
    }

    public void on(String tag)
    {
        set(tag, (byte)255);
    }
    public void off(String tag)
    {
        set(tag, (byte)0);
    }

    public void set(String tag, byte value)
    {
        for (Recipient r : recipients.values())
        {
            for (CanvasDetector cd : r.getChannels())
            {
                if (cd != null && cd.tags.contains(tag))
                {
                    cd.setValue(value);
                }
            }
        }
        this.syncAllLights();
    }

    protected void setTestVals(String tag, byte value)
    {
        for (Recipient r : recipients.values())
        {
            for (CanvasDetector cd : r.getChannels())
            {
                if (cd != null)
                {
                    if(cd.tags.contains(tag))
                    {
                        cd.setValue(value);
                    }else{
                        cd.setValue((byte)0);
                    }
                }
            }
        }
        this.syncAllLights();
    }

    @Override
    public void testingComplete() {
        System.out.println("testSuite complete.");
        this.isRunningTest = false;
    }

    public void runTest(String tiName)
    {
        if (!isRunning && !isRunningTest)
        {
            TestSuite ti = suites.get(tiName);
            if (ti == null)
            {
                logger.error("No such testSuite '" + tiName + "'");
            }else{
                isRunningTest = true;
                ti.start();
            }
        }
    }

    public Object[] getTestSuiteNames(){
        return suites.keySet().toArray();
    }
    public Collection<TestSuite> getTestSuites(){
        return suites.values();
    }

    public void addConfigListener(ConfigurationListener cl) {
        if (configListeners == null){
            configListeners = new Vector<ConfigurationListener>();
        }
        configListeners.add(cl);
    }

    public void removeConfigListner(ConfigurationListener cl) {
        if (configListeners != null){
            configListeners.remove(cl);
        }
    }

    public ELUManager load() throws IOException, OptionException
    {
        return load("lights.properties");
    }

    public void notifyConfigListeners(){

        if (configListeners != null){
            for (ConfigurationListener cl : configListeners){
                cl.configurationLoaded();
            }
        }
    }
    
    public ELUManager load(String  propFileName) throws IOException, OptionException
    {
        ElectrolandProperties ep = new ElectrolandProperties(propFileName);
        return load(ep);
    }
    
    /** Configure the system using "lights.properties"
     * 
     * Currently a gross way of doing this.
     * 
     */
    public ELUManager load(ElectrolandProperties ep) throws IOException, OptionException
    {
        logger.info("ELUManager loading...");
        if (isRunning || isRunningTest)
            throw new OptionException("Cannot load while threads are running.");

        recipients = new HashMap<String, Recipient>();
        canvases = new HashMap<String, ELUCanvas>();
        tests = new HashMap<String, Test>();
        suites = new HashMap<String, TestSuite>();
        fixtures = new HashMap<String, Fixture>();

        HashMap<String, FixtureType>types = new HashMap<String, FixtureType>();

        // get fps
        logger.info("\tgetting settings.global." + FPS);
        fps = ep.getRequiredInt("settings","global",FPS);

        // parse recipients
        logger.info("\tgetting " + RECIPIENT + "s...");
        for (String name : ep.getObjectNames(RECIPIENT))
        {            
            logger.info("\tgetting " + RECIPIENT + "." + name);
            try{
                Recipient r = (Recipient)ep.getRequiredClass(RECIPIENT, name, "class");
                // name, configure, store
                r.setName(name);
                r.configure(ep.getParams(RECIPIENT, name));
                recipients.put(name, r);

            }catch(ClassCastException e)
            {
                throw new OptionException(name + "' is not of class Recipient.");
            }
        }

        // parse fixtureTypes
        logger.info("\tgetting " + FIXTURE_TYPE + "s...");
        for (String name : ep.getObjectNames(FIXTURE_TYPE))
        {
            logger.info("\tgetting " + FIXTURE_TYPE + "." + name);
            FixtureType type = new FixtureType(name, ep.getRequiredInt(FIXTURE_TYPE, name, "channels"));
            Double w = ep.getOptionalDouble(FIXTURE_TYPE, name, "realWidth");
            Double h = ep.getOptionalDouble(FIXTURE_TYPE, name, "realHeight");
            String u = ep.getOptional(FIXTURE_TYPE, name, "realUnits");
            if (w != null && h != null){
                type.setSize(new ReferenceDimension(w,h,u));
            }
            types.put(name, type);
        }

        // patch channels into each fixtureType (e.g., detectors)
        logger.info("\tgetting " + DETECTOR + "s...");
        for (String name : ep.getObjectNames(DETECTOR))
        {
            logger.info("\tgetting " + DETECTOR + "." + name);
            int x = ep.getRequiredInt(DETECTOR, name, "x");
            int y = ep.getRequiredInt(DETECTOR, name, "y");
            int width = ep.getRequiredInt(DETECTOR, name, "w");
            int height = ep.getRequiredInt(DETECTOR, name, "h");
            List<String> tags = ep.getOptionalList(DETECTOR, name, "tags");

            try{
                DetectionModel dm = (DetectionModel)ep.getRequiredClass(DETECTOR, name, "model");

                // patch information
                String ftname = ep.getRequired(DETECTOR, name, FIXTURE_TYPE);
                int index = ep.getRequiredInt(DETECTOR, name, "index");

                FixtureType ft = (FixtureType)types.get(ftname);
                if (ft== null){
                    throw new OptionException("fixtureType '" + ftname + "' cannot be found for " + DETECTOR + "'" + name + "'.");
                }
                Detector existing = ft.detectors.get(index);
                if (existing != null)
                {
                    throw new OptionException("channel at index " + index + " is already patched.");
                }
                ft.detectors.set(index, new Detector(x,y,width,height,dm, tags));

            }catch(ClassCastException e)
            {
                throw new OptionException(name + " is not of class DetectionModel.");
            }

        }

        // parse canvases
        logger.info("\tgetting " + CANVAS + "s...");
        for (String name : ep.getObjectNames(CANVAS))
        {
            logger.info("\tgetting " + CANVAS + "." + name);
            ELUCanvas ec = (ELUCanvas)ep.getRequiredClass(CANVAS, name, "class");
            ec.configure(ep.getParams(CANVAS, name));
            ec.setName(name);
            
            // optional dimensions
            Double w = ep.getOptionalDouble(CANVAS, name, "realWidth");
            Double h = ep.getOptionalDouble(CANVAS, name, "realHeight");
            String u = ep.getOptional(CANVAS, name, "realUnits");
            if (w != null && h != null){
                ec.setRealSize(new ReferenceDimension(w,h,u));
            }
                
            
            canvases.put(name, ec);
        }

        // parse fixtures
        logger.info("\tgetting " + FIXTURE + "s...");
        for (String name : ep.getObjectNames(FIXTURE))
        {
            logger.info("\tgetting " + FIXTURE + "." + name);
            String typeStr = ep.getRequired(FIXTURE, name, FIXTURE_TYPE);
            FixtureType type = types.get(typeStr);
            if (type == null){
                throw new OptionException("fixtureType '" + typeStr + "' for object '" + name + "' of type 'fixture' could not be found.");
            }
            int startAddress = ep.getRequiredInt(FIXTURE, name, "startAddress");
            String recipStr = ep.getRequired(FIXTURE, name, RECIPIENT);
            Recipient recipient = recipients.get(recipStr);
            if (recipient == null){
                throw new OptionException("recipient '" + recipStr + "' for object '" + name + "' of type 'fixture' could not be found.");                
            }
            List<String> tags = ep.getOptionalList(FIXTURE, name, "tags");

            Fixture fixture = new Fixture(name, type, startAddress, recipient, tags);

            Double x = ep.getOptionalDouble(FIXTURE, name, "x");
            Double y = ep.getOptionalDouble(FIXTURE, name, "y");
            Double z = ep.getOptionalDouble(FIXTURE, name, "z");
            if (x != null && y != null && z != null){
                Point3d location = new Point3d(x,y,z);
                fixture.setLocation(location);
            }

            fixtures.put(name, fixture);
        }

        // parse fixture to canvas mappings (this is the meat of everything)
        logger.info("\tgetting " + CANVAS_FIXTURE + "s...");
        for (String name : ep.getObjectNames(CANVAS_FIXTURE))
        {
            logger.info("\tgetting " + CANVAS_FIXTURE + "." + name);
            //     * find the fixture
            String fixtureName = ep.getRequired(CANVAS_FIXTURE, name, FIXTURE);
            Fixture fixture = fixtures.get(fixtureName);
            if (fixture == null){                
                throw new OptionException("fixture '" + fixtureName + "' for object '" + name + "' of type 'canvasFixture' could not be found.");
            }

            //       * find the canvas
            String cnvsName = ep.getRequired(CANVAS_FIXTURE, name, CANVAS);
            ELUCanvas canvas = canvases.get(cnvsName);
            if (canvas == null){
                throw new OptionException("canvas '" + cnvsName + "' for object '" + name + "' of type 'canvasFixture' could not be found.");
            }

            // counter for recipient mappings
            int channel = fixture.startAddress;

            //       * find the recipient
            Recipient recipient = fixture.recipient;

            
            //     for each prototype detector in the fixture (get from FixtureType)
            for (Detector dtr : fixture.type.detectors)
            {
                //      * create a CanvasDetector
                CanvasDetector cd = new CanvasDetector();
                cd.tags = new Vector<String>();
                cd.tags.addAll(fixture.tags);
                if (dtr.tags != null){
                    cd.tags.addAll(dtr.tags);
                }

                //      * calculate x,y based on the offset store it in the CanvasDetector
                double offsetX = ep.getRequiredDouble(CANVAS_FIXTURE, name, "x");
                double offsetY = ep.getRequiredDouble(CANVAS_FIXTURE, name, "y");

                double scaleX = ep.getRequiredDouble(CANVAS_FIXTURE, name, "xScale");
                double scaleY = ep.getRequiredDouble(CANVAS_FIXTURE, name, "yScale");

                Rectangle boundary = new Rectangle((int)((scaleX * (dtr.x + offsetX))),
                                                    (int)((scaleY * (dtr.y + offsetY))),
                                                    (int)(dtr.width * scaleX),
                                                    (int)(dtr.height * scaleY));
                cd.boundary = boundary;
                //      * store the detector model
                cd.model = dtr.model;

                // map the CanvasDetectors to pixel locations in the pixelgrab
                canvas.addDetector(cd);
                
                // map the CanvasDetector to a channel in the recipient.
                recipient.map(channel++, cd);
            }
        }

        // parse Tests
        logger.info("\tgetting " + TEST + "s...");
        for (String name : ep.getObjectNames(TEST))
        {
            logger.info("\tgetting " + TEST + "." + name);
            Test test = new Test(name, ep.getRequiredList("test", name, "tags"));
            tests.put(name, test);
        }

        // parse TestSuites
        logger.info("\tgetting " + TEST_SUITE + "s...");
        for (String name : ep.getObjectNames(TEST_SUITE))
        {
            logger.info("\tgetting " + TEST_SUITE + "." + name);
            int fps = ep.getRequiredInt(TEST_SUITE, name, "fps");
            List<String> testStrs = ep.getRequiredList(TEST_SUITE, name, "tests");
            ArrayList<Test> itests = new ArrayList<Test>(testStrs.size());

            for (String s : testStrs)
            {
                Test test = tests.get(s);
                if (test == null){
                    throw new OptionException("cannot find test '" + s + "' in " + TEST_SUITE + " '" + name + "'");
                }else{
                    itests.add(test);
                }
            }

            int loops = ep.getRequiredInt(TEST_SUITE, name, "loops");
            byte color = ep.getRequiredInt(TEST_SUITE, name, "color").byteValue();

            TestSuite it = new TestSuite(name, this, fps, itests, loops, color);
            it.addCompletionListener(this);

            suites.put(name, it);
        }

        Runtime.getRuntime().addShutdownHook(new BlackOutThread(this));
        notifyConfigListeners();

        autostart = ep.getDefaultInt("settings", "global", "autostart", -1);
        if (autostart >=0 ){
            new AutostartThread(this, autostart).start();
        }

        return this;
    }

    public Collection<Fixture> getFixtures()
    {
        return fixtures.values();
    }

    public boolean isAutostarted(){
        return autostart >= 0;
    }

    /**
     * return the canvas referenced by name in lights.properties.
     * @param name
     * @return
     */
    public ELUCanvas getCanvas(String name)
    {
        return canvases.get(name);
    }

    /**
     * return all canvases (mapped to names)
     * @return
     */
    public Map<String, ELUCanvas> getCanvases()
    {
        return canvases;
    }

    public void debug()
    {
        System.out.println("FPS set to " + fps);

        for (ELUCanvas c : canvases.values()){
            c.debug();
        }

        for (Fixture f : fixtures.values()){
            f.debug();
        }

        for (Recipient r : recipients.values()){
            r.debug();
        }

        for (TestSuite t : suites.values())
        {
            t.debug();
        }
    }
}

class BlackOutThread extends Thread{
    private ELUManager elu;
    public BlackOutThread(ELUManager elu)
    {
        this.elu = elu;
    }
    public void run()
    {
        elu.stop();
        elu.allOff();
    }
}

class AutostartThread extends Thread {

    private ELUManager elu;
    private int delay;

    public AutostartThread(ELUManager elu, int delay)
    {
        this.elu   = elu;
        this.delay = delay;
    }
    public void run()
    {
        try {
            Thread.sleep(1000 * delay);
            elu.start();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}