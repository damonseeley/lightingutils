package com.damonseeley.utils.lighting.detection;

import java.awt.Color;

import com.damonseeley.utils.lighting.DetectionModel;

/**
 * This class determines the average green intensity of an array of pixels, and
 * returns that value as a byte from 0-255.  It returns the same value for
 * getColor as getBrightness.
 * 
 * @author geilfuss
 *
 */
public class GreenDetectionModel implements DetectionModel {

    @Override
    public byte getValue(int[] pixels) {
        float g = 0;
        for (int i = 0; i < pixels.length; i++){
            g += (pixels[i] >> 8 & 0xFF);
        }
        return (byte)(g / pixels.length);
    }

    @Override
    public Color getColor(byte b) {
        return new Color(0, b & 0xff, 0);
    }

    public String toString(){
        return "Green";
    }

}