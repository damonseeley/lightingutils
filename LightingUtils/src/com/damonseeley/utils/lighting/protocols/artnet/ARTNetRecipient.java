package com.damonseeley.utils.lighting.protocols.artnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import net.electroland.utils.OptionException;
import net.electroland.utils.ParameterMap;
import net.electroland.utils.Util;

import org.apache.log4j.Logger;

import com.damonseeley.utils.lighting.CanvasDetector;
import com.damonseeley.utils.lighting.Recipient;


public class ARTNetRecipient extends Recipient {

    public static int ART_NET_PORT = 6454; // port should be fixed for art net.
    public static byte OFF = (byte)0;
    public static byte ON =(byte)255;
    private int totalChannels, channelBits = 8, universe, port = ART_NET_PORT;
    private InetAddress ip;
    private String ipStr;
    private static Logger logger = Logger.getLogger(ARTNetRecipient.class);
    private static DatagramSocket socket;
    
    @Override
    public void configure(ParameterMap properties)
            throws OptionException {

        // Typical: -channels 512 channelBits 16 -address 127.0.0.1 -universe 1
        // get total channels (must be 1-512)
        totalChannels = properties.getRequiredInt("channels");
        if (totalChannels < 1 || totalChannels > 512)
        {
            throw new OptionException("recipient.channel must be between 1 and 512.");
        }
        // allocate channels
        setChannels(new CanvasDetector[totalChannels]);

        // optional port param (for UDP port)
        Integer port = properties.getOptionalInt("port");
        this.port = (port == null) ? ART_NET_PORT : port;
        if (this.port < 1 || this.port > 65535)
        {
            throw new OptionException("recipient.port must be between 1 and 65535.");
        }

        // get channelBits - (optional) Valid values are either 8 or 16.
        Integer channelBits = properties.getOptionalInt("channelBits");
        if (channelBits != null)
        {
            if (channelBits != 8 && channelBits != 16)
            {
                throw new OptionException("recipient.channelBits must be either 8 or 16.");
            }else{
                this.channelBits = channelBits;
            }
        }

        // get IP address (not validated here)
        ipStr = properties.getRequired("address");
        try {
            this.ip = InetAddress.getByName(ipStr);
        } catch (UnknownHostException e) {
            throw new OptionException("recipient.address failed: " + e.getMessage());
        }

        // get universe (must be 0-255)
        universe = properties.getRequiredInt("universe");
        if (universe < 0 || universe > 255)
        {
            throw new OptionException("recipient.universe must be between 0 and 255.");
        }
    }

    @Override
    public void map(int channel, CanvasDetector cd) throws OptionException {
        if (channel >= 0 && channel < getChannels().length)
        {
            getChannels()[channel] = cd;
        }else{
            throw new OptionException("Attempt to map to channel " + channel + 
                            " in " + this.getName() + " is out of bounds.");
        }
    }

    @Override
    public void allOn() {
        setAll(ON);
        byte[] bytes = new byte[this.totalChannels];
        for (int i=0; i < bytes.length; i++){
            bytes[i] = ON;
        }
        send(bytes);
    }

    @Override
    public void allOff() {
        setAll(OFF);
        byte[] bytes = new byte[this.totalChannels];
        for (int i=0; i < bytes.length; i++){
            bytes[i] = OFF;
        }
        send(bytes);
    }

    @Override
    public void send(Byte[] data)
    {
        // check for nulls in the data packet and replace with (byte)0
        for (int i = 0; i < data.length; i++)
        {
            if (data[i] == null){
                data[i] = (byte)0;
            }
        }
        // copy to byte[]
        byte[] bdata = new byte[data.length];
        
        for (int i = 0; i < bdata.length; i++)
        {
            bdata[i] = data[i];
        }
        send(bdata);
    }

    public void send(byte[] data){
        try {

            ArtNetDMXData dmx = new ArtNetDMXData(); // could cache this.

            dmx.setUniverse((byte)universe);
            dmx.setPhysical((byte)1);
            dmx.Sequence = (byte)0;    
            dmx.setData(data);

            ByteBuffer b = dmx.getBytes();

            logger.trace(this.getName() + ", universe " + universe + " at IP " + 
                            this.ipStr + " on port " + port + ": "  + 
                            Util.bytesToHex(b.array(), b.position()));

            synchronized (this){
                if (socket == null || socket.isClosed()){
                    socket = new DatagramSocket(port);
                }
            }

            DatagramPacket packet = new DatagramPacket(b.array(), b.position(), ip, port);
            socket.send(packet);

        } catch (SocketException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
    }
    @Override
    public void debug(){
        System.out.println("ARTNetRecipient '" + this.getName() + "'");
        System.out.println("\thas " + totalChannels + " channels");
        System.out.println("\tat " + channelBits + " channelBits");
        System.out.println("\tcommunicated to universe " + universe);
        System.out.println("\tat address " + ipStr);

        for (int i = 0; i < getChannels().length; i++)
        {
            if (getChannels()[i] != null){
                System.out.println("ARTNetRecipient '" + this.getName() + 
                        "' channel[" + i + "] contains " + getChannels()[i]);
            }
        }
    }
}