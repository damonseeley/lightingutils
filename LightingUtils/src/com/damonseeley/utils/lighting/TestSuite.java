package com.damonseeley.utils.lighting;

import java.util.List;
import java.util.Vector;

import net.electroland.utils.Util;

import org.apache.log4j.Logger;

public class TestSuite implements Runnable {

    private static Logger logger = Logger.getLogger(TestSuite.class);

    private int loops;
    private int fps;
    private Test[] tests;
    private ELUManager elu;
    private byte color;
    private Thread thread;
    private String name;
    private List<TestSuiteCompletionListener> completionListeners;
    
    public TestSuite(String name, ELUManager elu, int fps, List<Test>tests, int loops, byte color)
    {
        this.name = name;
        this.elu = elu;
        this.fps = fps;
        this.tests = new Test[tests.size()];
        tests.toArray(this.tests);
        this.loops = loops;
        this.color = color;
        this.completionListeners = new Vector<TestSuiteCompletionListener>();
    }

    public void addCompletionListener(TestSuiteCompletionListener listener){
        this.completionListeners.add(listener);
    }
    public void removeCompletionListener(TestSuiteCompletionListener listener){
        this.completionListeners.remove(listener);
    }

    @Override
    public void run() {
        int loopsLeft = loops;
        while (loopsLeft-- > 0)
        {
            for (Test t : tests)
            {
                for (String tag : t.tags)
                {
                    elu.setTestVals(tag, color);
                    
                    try {
                        Thread.sleep((long)(1000.0/fps));
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }
        }

        for (TestSuiteCompletionListener l : completionListeners){
            l.testingComplete();
        }

        thread = null;
    }
    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    public void debug()
    {
        StringBuffer sb = new StringBuffer("TestSuite '");
        sb.append(name).append("'[fps=").append(fps).append(",loops=");
        sb.append(loops).append(",color=").append(Util.bytesToHex(color));
        sb.append(",tests[");
        for (Test t : tests)
        {
            sb.append(t.name);
            sb.append(',');
        }
        sb.append("]]");
        System.out.println(sb.toString());
    }
}