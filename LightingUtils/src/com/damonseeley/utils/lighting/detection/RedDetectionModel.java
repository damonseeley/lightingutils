package com.damonseeley.utils.lighting.detection;

import java.awt.Color;

import com.damonseeley.utils.lighting.DetectionModel;

/**
 * This class determines the average red intensity of an array of pixels, and
 * returns that value as a byte from 0-255.  It returns the same value for
 * getColor as getBrightness.
 * 
 * @author geilfuss
 *
 */
public class RedDetectionModel implements DetectionModel {

    @Override
    public byte getValue(int[] pixels) {
        float r = 0;
        for (int i = 0; i < pixels.length; i++){
            r += (pixels[i] >> 16) & 0xFF;
        }
        return (byte)(r / pixels.length);
    }

    @Override
    public Color getColor(byte b) {
        return new Color(b & 0xff, 0, 0);
    }

    public String toString(){
        return "Red";
    }
}