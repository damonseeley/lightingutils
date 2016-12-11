package com.damonseeley.utils.lighting;

import java.util.List;
import java.util.Vector;

import javax.vecmath.Point3d;

import net.electroland.utils.ReferenceDimension;

public class Fixture
{
    protected String name;
    protected FixtureType type;
    protected int startAddress;
    protected Vector<String> tags = new Vector<String>();
    protected Recipient recipient;
    protected Point3d location;

    public Fixture(String name, FixtureType type, int startAddress, Recipient recipient, List<String> tags){
        this.name = name;
        this.type = type;
        this.startAddress = startAddress;
        this.tags.addAll(tags);
        this.recipient = recipient;
    }

    public String getName(){
        return name;
    }

    public String getTypeName(){
        return type.name;
    }

    public Point3d getLocation() {
        return location;
    }

    protected void setLocation(Point3d location) {
        this.location = location;
    }

    public ReferenceDimension getRealDimensions(){
        return type.getRealDimensions();
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Fixture").append(name).append("[");
        sb.append("type=").append(type);
        sb.append(", startAddress=").append(startAddress);
        sb.append(", recipient=").append(recipient);
        sb.append(", tags=").append(tags);
        sb.append("location=").append(location);
        sb.append(", realDimensions=").append(type.getRealDimensions());
        sb.append("]");
        return sb.toString();
    }

    public void debug(){
        System.out.println(this);
    }
}