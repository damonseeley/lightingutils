package net.electroland.utils;

import java.awt.geom.Dimension2D;

public class ReferenceDimension extends Dimension2D {

    private String units;
    private double width, height;

    public ReferenceDimension(double width, double height, String units){
        setSize(width, height, units);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    public String getUnits()
    {
        return units;
    }

    public void setSize(double width, double height, String units) {
        setSize(width,height);
        setUnits(units);
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("w=").append(width).append(", ");
        sb.append("h=").append(height).append(", ");
        sb.append("units=").append(units);
        return sb.toString();
    }
}