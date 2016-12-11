package com.damonseeley.utils.lighting.canvas;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;

public class ProcessingCanvas extends ELUCanvas2D {

    private ELUPApplet applet;

    @Override
    public void configure(ParameterMap props) throws OptionException {

        super.configure(props);

        // get the custom config variables
        int fps = props.getRequiredInt("fps");
        Object appletObj = props.getRequiredClass("applet");

        // load the applet
        if (appletObj instanceof ELUPApplet){

            // make sure setSyncArea(...)  precedes init(). otherwise there is a race
            // condition against setup().
            applet = (ELUPApplet)appletObj;
            applet.properties = props;
            applet.setSyncArea(this.getDimensions());
            applet.setSyncCanvas(this);
            applet.init();
            applet.frameRate(fps);

            // open it in its own window
            JFrame f = new JFrame();
            f.setTitle(props.get("applet"));
            f.setLayout(new BorderLayout());
            f.setSize(this.getDimensions().width + 80, this.getDimensions().height + 40);
            f.add(applet, BorderLayout.CENTER);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        }
    }

    public ELUPApplet getApplet(){
        return applet;
    }
}