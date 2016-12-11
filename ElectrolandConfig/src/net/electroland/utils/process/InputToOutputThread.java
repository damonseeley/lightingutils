package net.electroland.utils.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Reads from an InputStream and pipes to either an output stream or a Log4j
 * channel.
 * 
 * @author bradley
 *
 */
public class InputToOutputThread implements Runnable {

    private BufferedReader in;
    private PrintWriter pw;
    private Logger logger;
    private Level level;
    private boolean isRunning = false;

    public InputToOutputThread(InputStream is, OutputStream os){
        this.in = new BufferedReader(new InputStreamReader(is));
        this.pw = new PrintWriter(new OutputStreamWriter(os));
    }

    public InputToOutputThread(InputStream is, Logger logger, Level level){
        this.in       = new BufferedReader(new InputStreamReader(is));
        this.logger   = logger;
        this.level    = level;
    }

    public InputToOutputThread(InputStream is, OutputStream os, Logger logger, Level level){
        this.in       = new BufferedReader(new InputStreamReader(is));
        this.pw       = new PrintWriter(new OutputStreamWriter(os));
        this.logger   = logger;
        this.level    = level;
    }

    @Override
    public void run() {

        isRunning = true;

        while(isRunning){
            try {
                if (in.ready()){
                    println(in.readLine());
                }
                Thread.sleep(2);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPiping() {
        new Thread(this).start();
    }

    public void stopPiping() {
        isRunning = false;
    }

    private void println(String line){
        if (pw != null){
            pw.println(line);
        }
        if (logger != null){
            logger.log(level, line);
        }
    }
}