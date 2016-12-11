package com.damonseeley.utils.lighting.detection;

import java.awt.Color;

import com.damonseeley.utils.lighting.DetectionModel;

public class BrightRedDetectionModel implements DetectionModel {

    @Override
    public byte getValue(int[] pixels) {
        // we'll ignore up to half of the pixels if they are black.
        int ignore = pixels.length / 2;
        int points = 0;
        float r = 0;
        for (int i = 0; i < pixels.length; i++){
            int intensity = (pixels[i] >> 16) & 0xFF;
            // if we surpassed the max number of ignored pixels or this isn't
            // a black pixel, average it in.
            if (ignore == 0 || intensity > 0)
            {
                r += intensity;
                points++;
            }else{
                // ignore this pixel.
                ignore--;
            }
        }
        return points == 0 ? (byte)0 : (byte)(r / points);
    }

    @Override
    public Color getColor(byte b) {
        return new Color(b & 0xff, 0, 0);
    }
}