package com.damonseeley.utils.lighting.protocols.kinet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.electroland.utils.Util;

public class KiNetData {

    private int     MAGIC_NUMBER = 0x4ADC0104;
    private short   VERSION = 0x0001;
    private short   PORT_OUT_TYPE = 0x0108;
    private short   SEQ_NUM = 0x00000000;
    private short   PORT_OUT_FLAGS = 0x0000;
    private byte    PADDING = 0x00;
    private int     universe;
    private byte    port;
    private short   start_code; // for chromasic

    public static void main(String args[])
    {
        KiNetData d = new KiNetData(-1, 1, false);
        ByteBuffer packet = d.createPORTOUT(new byte[3]);
        System.out.println(Util.bytesToHex(packet.array(), packet.position()));
    }

    public KiNetData(int universe, int port, boolean isChromasic)
    {
        setUniverse(universe);
        setPort(port);
        setIsChromasic(isChromasic);
    }

    /**
     * take an array of color data, and wrap it into a KiNet PORT_OUT packet,
     * ready for UDP sending.
     * 
     * @param data
     * @return
     */
    public ByteBuffer createPORTOUT(byte[] data)
    {
        if (data.length > 512)
        {
            throw new RuntimeException("KiNet data length cannot exceed 512 bytes.");
        }
        // create packet (should calculate that 24...)
        ByteBuffer  bb = ByteBuffer.allocate(24 + data.length); 
        bb.order(ByteOrder.LITTLE_ENDIAN); 

        bb.putInt(MAGIC_NUMBER);        // 4 bytes magic number (0x4ADC0104)
        bb.putShort(VERSION);           // 2 bytes version (1)
        bb.putShort(PORT_OUT_TYPE);     // 2 bytes packet type (0x0108 == PORTOUT)
        bb.putInt(SEQ_NUM);             // 4 bytes sequence (not-supported)
        bb.putInt(universe);            // 4 bytes universe
        bb.put(port);                   // 1 byte port
        bb.put(PADDING);                // 1 byte padding for port
        bb.putShort(PORT_OUT_FLAGS);    // 2 bytes flags (0x00)
        bb.putShort((short)data.length);// 2 bytes payload length
        bb.putShort(start_code);        // 2 bytes start code (chromasic = 0x0fff.  non-chromasic = 0x0000)
        bb.put(data);                   // 0-512 bytes of data. RGB triples.

        return bb;
    }

    public void setUniverse(int universe)
    {
        this.universe = universe;
    }

    public void setPort(int port)
    {
        // set port (check to see if it is legal)
        if (port < 1 || port > 16){
            throw new RuntimeException("Illegal KiNet Port " + port + ". Port must be 1-16");
        }else{
            this.port = (byte)port;
        }
    }

    public void setIsChromasic(boolean isChromasic)
    {
        if (isChromasic){
            start_code = 0x0fff;
        }else{
            start_code = 0x0000;
        }
    }
}