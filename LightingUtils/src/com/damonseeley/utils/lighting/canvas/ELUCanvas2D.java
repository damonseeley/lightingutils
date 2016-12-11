package com.damonseeley.utils.lighting.canvas;

import java.awt.Dimension;
import java.awt.Rectangle;

import com.damonseeley.utils.lighting.CanvasDetector;
import com.damonseeley.utils.lighting.ELUCanvas;

import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;

public class ELUCanvas2D extends ELUCanvas {

    protected Dimension d;
    int[] lastpixels;

    @Override
    public void configure(ParameterMap p)
            throws OptionException {

        try{
            // get dimensions from config file
            int width = Integer.parseInt(p.get("width"));
            int height = Integer.parseInt(p.get("height"));

            // set dimensions
            d = new Dimension(width,height);

        }catch(NumberFormatException e){
            throw new OptionException("cannot parse canvas dimensions " + e);
        }
    }

    @Override
    public void map(CanvasDetector d) throws OptionException {

        // for each index, get the array indices contained within the boundary
        // and store them in the CanvasDetector.

        Rectangle boundary = (Rectangle)d.getBoundary();

        int x1 = boundary.x;
        int y1 = boundary.y;
        int x2 = x1 + boundary.width - 1;
        int y2 = y1 + boundary.height - 1;
        int pixels = this.d.width * this.d.height;

        for (int y = y1; y <= y2; y++)
        {
            for (int x = x1; x <= x2; x++){
                int current = (y * this.d.width) + x;
                 // don't include offscreen pixels
                if (current > -1 && current < pixels){
                    d.getPixelIndices().add(current);
                }
            }
        }
    }

    @Override
    public CanvasDetector[] sync(int[] pixels) {

        synchronized(detectors){
            for (CanvasDetector d : detectors)
            {
                d.eval(pixels);
            }
        }

        return super.getDetectors();
    }

    public Dimension getDimensions() {
        return d;
    }

    public void debug()
    {
        System.out.println("ELUCanvas2D '" + this.getName() + "' is " + d.width + " by " + d.height + " pixels.");
        if (this.getRealDimensions() != null){
            System.out.println("ELUCanvas2D '" + this.getName() + "' is " + this.getRealDimensions().getWidth() + " by " + this.getRealDimensions().getWidth() + " " + this.getRealDimensions().getUnits() + ".");
        }else{
            System.out.println("\tNo real size has been set for this canvas.");
        }
        for (CanvasDetector cd : detectors)
        {
            System.out.println("ELUCanvas2D '" + this.getName() + "' contains " + cd);
            System.out.println("\tis mapped to pixels " + cd.getPixelIndices());
        }
    }
}