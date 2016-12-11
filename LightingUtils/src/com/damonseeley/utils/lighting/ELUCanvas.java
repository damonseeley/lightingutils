package com.damonseeley.utils.lighting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;
import net.electroland.utils.ReferenceDimension;


abstract public class ELUCanvas {

    private String name;
    private ReferenceDimension realSize;

    protected List<CanvasDetector>detectors = Collections.synchronizedList(new ArrayList<CanvasDetector>());

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }    

    /**
     * Send an array of pixels to be synced with all fixtures/detectors that
     * are attached to this canvas.  The return will be a clone of all the 
     * * detectors in that is in a 0,0 (left,top) coordinate space, with the 
     * evaluated byte values in each detector.
     * 
     * @param a
     * @return
     * @throws BadArrayException - will be thrown if the array does not conform
     * to any constraints defined by the surface that the concrete instance of
     * this class exposes.  For example, ELUCanvas2D would require that the
     * length of the array be exactly 
     * 
     *   ELUCanvas2D.getWidth() * ELUCanvas2D.getHeight()
     *   
     * Alternately, the array may throw an exception if things like bitdepth
     * are violated within data in the array.
     * 
     */
    abstract public CanvasDetector[] sync(int[] pixels);

    abstract public void configure(ParameterMap params) throws OptionException;

    abstract public void map(CanvasDetector d) throws OptionException;

    /**
     * print debugging info.
     */
    abstract public void debug();
    
    /**
     * Get all detectors attached to this detector
     * @return
     */
    public CanvasDetector[] getDetectors()
    {
        return detectors.toArray(new CanvasDetector[detectors.size()]);
    }

    public void addDetector(CanvasDetector cd) throws OptionException
    {
        detectors.add(cd);
        map(cd);
    }
    public ReferenceDimension getRealDimensions() {
        return realSize;
    }
    public void setRealSize(ReferenceDimension realSize) {
        this.realSize = realSize;
    }
}