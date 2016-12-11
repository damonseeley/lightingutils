package com.damonseeley.lightingutilsdemo.core;

public class FpsAverage {

	private int     data[];
    private int     index = 0;
    private float  total = 0;
    private long lastTouch = 0;

	
    public FpsAverage(int samples){
        this.data = new int[samples];
    }

    public int getAverage(){
        return (int)((1000.0f) / (total / data.length));
    }

    // ring buffered sum
    public void touch(){

        if (lastTouch == 0){
            lastTouch = System.currentTimeMillis();

        }else{

            long current      = System.currentTimeMillis();
            int duration      = (int)(current - lastTouch);
            lastTouch       = current;

            total -= data[index];
            total += duration;
            data[index++] = duration;

            if (index == data.length){
                index = 0;
            }
        }
    }
}