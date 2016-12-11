package com.damonseeley.utils.lighting;

import java.util.Vector;

import net.electroland.utils.ReferenceDimension;

public class FixtureType
{
	protected String name;
	protected Vector<Detector> detectors;
	protected ReferenceDimension realSize;

	public FixtureType(String name, int channels)
	{
		this.name = name;
		detectors = new Vector<Detector>();
		detectors.setSize(channels);
	}

    public ReferenceDimension getRealDimensions() {
        return realSize;
    }

    public void setSize(ReferenceDimension realSize) {
        this.realSize = realSize;
    }

}