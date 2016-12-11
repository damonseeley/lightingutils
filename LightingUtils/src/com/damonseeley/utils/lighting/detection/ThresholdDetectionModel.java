package com.damonseeley.utils.lighting.detection;

import java.awt.Color;

import com.damonseeley.utils.lighting.DetectionModel;

public class ThresholdDetectionModel implements DetectionModel {

    private float threshold = .5f; // default is a 50% threshold
    
    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold){
        if (threshold < 0 || threshold > 1.0){
            throw new RuntimeException("Threshold must be between 0 and 1.0");
        }else{
            this.threshold = threshold;
        }
    }

    @Override
    public byte getValue(int[] pixels) {
        float total = 0;
        for (int i = 0; i < pixels.length; i++){

            // separate rgb vals
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8 & 0xFF);
            int b = (pixels[i] & 0xFF);

            // http://en.wikipedia.org/wiki/Grayscale
            int gy = (int)((.3 * r) + (.59 * g) + (.11 * b));

            total += gy;
        }
        return (total / pixels.length) >= (threshold * 255) ? (byte)255 : (byte)0;
    }

    @Override
    public Color getColor(byte b) {
        return b == (byte)255 ? Color.white : Color.black;
    }
}