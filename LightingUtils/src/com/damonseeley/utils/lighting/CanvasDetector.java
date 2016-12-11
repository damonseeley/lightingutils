package com.damonseeley.utils.lighting;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class CanvasDetector {

    protected Shape boundary;
    protected DetectionModel model;
    protected byte latestState;
    protected List<String> tags;
    protected ArrayList<Integer>pixelIndices = new ArrayList<Integer>();

    public final Shape getBoundary() {
        return boundary;
    }
    public final DetectionModel getDetectorModel() {
        return model;
    }
    public byte getLatestState() {
        return latestState;
    }
    public void setValue(byte b){
        latestState = b;
    }
    public final List<String> getTags() {
        return tags;
    }
    public final List<Integer> getPixelIndices() {
        return pixelIndices;
    }

    public final synchronized void eval(int pixels[])
    {

        // copy all the pixels that are in my range
        int[] mypixels = new int[pixelIndices.size()];
        int ptr = 0;
        for (Integer i : pixelIndices)
        {
            mypixels[ptr++] = pixels[i];
        }

        // evaluate
        latestState = model.getValue(mypixels);
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("CanvasDetector[");
        sb.append(boundary).append(',');
        sb.append("tags").append(tags).append(",latestEval=");
        sb.append(latestState).append(',').append(model).append(']');
        return sb.toString();
    }
}