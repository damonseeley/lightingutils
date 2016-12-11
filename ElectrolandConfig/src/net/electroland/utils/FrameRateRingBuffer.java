package net.electroland.utils;

/**
 * Use this to measure FPS.
 * 
 * Pick a capacity. Then call markFrame() everytime you render a frame. Call
 * getFPS() to access the average framerate at any time.
 * 
 * @author bradley
 */
// TODO: What about make this a subclass of Thread, that takes care of all
//       sleep calculations. Then markFrame() will be superfluous.
public class FrameRateRingBuffer {

    int[]   samples;
    int     sampleIndex;
    long    lastSampleTime;
    int     total;
    boolean firstPass = true;

    //  test
    public static void main(String args[])
    {
        // will take 3.3 seconds
        FrameRateRingBuffer r = new FrameRateRingBuffer(10);
        for (int i = 0; i < 100; i++){
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            r.markFrame();
        }
        System.out.println(r);
    }

    public FrameRateRingBuffer(int expectedFrameRate, int seconds)
    {
        samples = new int[expectedFrameRate * seconds];
        lastSampleTime = System.currentTimeMillis();
    }

    public FrameRateRingBuffer(int capacity){
        if (capacity < 1){
            throw new RuntimeException("capacity must be greater than 0.");
        }
        samples = new int[capacity];
        lastSampleTime = System.currentTimeMillis();
    }

    public void markFrame()
    {
        synchronized(samples){
            long curr = System.currentTimeMillis();
            total -= samples[sampleIndex];
            samples[sampleIndex] = (int)(curr - lastSampleTime);
            total += samples[sampleIndex++];
            lastSampleTime = curr;
            if (sampleIndex == samples.length){
                sampleIndex = 0;
                firstPass = false;
            }
        }
    }

    public int getFPS()
    {
        int denominator = firstPass ? sampleIndex : samples.length;
        if (denominator == 0){
            return -1;
        }
        return 1000 / (total / denominator);
    }

    public String toString(){
        StringBuffer sb = new StringBuffer("StringBuffer[fps=");
        sb.append(this.getFPS()).append(", times=[");
        for (long t : samples){
            sb.append(t).append(',');
        }
        sb.append("]]");
        return sb.toString();
    }
}