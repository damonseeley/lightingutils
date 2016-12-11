package com.damonseeley.lightingutilsdemo.core.viz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.damonseeley.utils.lighting.CanvasDetector;
import com.damonseeley.utils.lighting.ELUManager;
import com.damonseeley.utils.lighting.Fixture;
import com.damonseeley.utils.lighting.detection.BlueDetectionModel;
import com.damonseeley.utils.lighting.detection.GreenDetectionModel;
import com.damonseeley.utils.lighting.detection.RedDetectionModel;

import net.electroland.utils.Util;

public class Raster2dViz extends JPanel {

    private static final long serialVersionUID = -2470541288243028641L;
    private BufferedImage frame;
    private CanvasDetector[] detectors;
    private ELUManager elu;
    private int textLeftOffset = 10;
    private int textRightOffset = 10;
  
    @Override
    public void paint(Graphics g) {
    	
        super.paint(g);

        Graphics2D g2d = (Graphics2D)g;

        if (frame != null){

            synchronized(frame){

                // render animation latest frame
                Dimension d = this.getPreferredSize();
                g2d.drawImage(frame, 0, 0, (int)d.getWidth(), (int)d.getHeight(), null);

                // render detectors
                for (CanvasDetector cd : detectors){

                    Rectangle r = (Rectangle)(cd.getBoundary());
                    int i = Util.unsignedByteToInt(cd.getLatestState());
                    Color c = null;

                    if (i != 0){
                        if (cd.getDetectorModel() instanceof RedDetectionModel){
                            c = new Color(i,0,0);
                        } else if (cd.getDetectorModel() instanceof GreenDetectionModel) {
                            c = new Color(0,i,0);
                        } else if (cd.getDetectorModel() instanceof BlueDetectionModel) {
                            c = new Color(0,0,1);
                        }
                    }

                    if (c != null){
                        g2d.setColor(c);
                        g2d.fillRect(r.x, r.y, r.width, r.height);
                    }

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(r.x, r.y, r.width, r.height);

                }

                if (elu != null){
                    for (Fixture f : elu.getFixtures()) {
                        Font font = new Font("Arial", Font.PLAIN, 9);
                        g2d.setFont(font);

                        // value
                        g2d.setColor(new Color(60,60,180));
                        g2d.drawString(f.getName(), (int)f.getLocation().x + textRightOffset, (int)f.getLocation().y + textLeftOffset);
                    }
                }
            }
        }
    }

    public void update(BufferedImage frame, CanvasDetector[] detectors, ELUManager elu){
        if (frame == null){
            this.frame = frame;
            this.detectors = detectors;
            this.elu = elu;
        }else{
            synchronized(frame){
                this.frame = frame;
                this.detectors = detectors;
                this.elu = elu;
            }
        }
    }
}