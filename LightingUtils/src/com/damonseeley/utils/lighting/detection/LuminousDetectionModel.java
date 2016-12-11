package com.damonseeley.utils.lighting.detection;

import java.awt.Color;

import com.damonseeley.utils.lighting.DetectionModel;

public class LuminousDetectionModel implements DetectionModel {

    @Override
    public byte getValue(int[] pixels) {
        float total = 0;
        for (int i = 0; i < pixels.length; i++){

            // separate rgb vals
            int a = (pixels[i] >> 24 ) & 0xFF;
            int r = (pixels[i] >> 16 ) & 0xFF;
            int g = (pixels[i] >>  8 ) & 0xFF;
            int b = (pixels[i]       ) & 0xFF;

            if (a == 0){
                // add zero. eg., do nothing.
            }else{
                double da = (a / 255.0);
                int gy = (int)(((r + g + b)/3.0) * da);

                total += gy;
            }
        }
        return (byte)(total / pixels.length);
    }

    @Override
    public Color getColor(byte b) {
        int i = b & 0xff;
        return new Color(i,i,i);
    }
}