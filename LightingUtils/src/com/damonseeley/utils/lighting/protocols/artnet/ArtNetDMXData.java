package com.damonseeley.utils.lighting.protocols.artnet;

import java.nio.ByteBuffer;

import net.electroland.utils.Util;

public class ArtNetDMXData extends ArtNetData {

	//	OpOutput. Transmitted low byte first. 
	short OpCode = ArtNetOpCodes.OpOutput;

	//	High byte of the Art-Net protocol revision number.
	byte ProtVerH = 0;

	//	Low byte of the Art-Net protocol revision number. 
	//	Current value 14 
	byte ProtVer = 14;

	//	The sequence number is used to ensure that ArtDmx 
	//	packets are used in the correct order. When Art-Net 
	//	is carried over a medium such as the Internet, it is 
	//	possible that ArtDmx packets will reach the receiver 
	//	out of order. 
	//	This field is incremented in the range 0x01 to 0xff to 
	//	allow the receiving node to resequence packets. 
	//	The Sequence field is set to 0x00 to disable this 
	//	feature. 
	// 0x00 IS THE DEFAULT.  IF WE SUPPORT INCREMENTING, IT WILL BE IN 
	// GETSEQUENCE().
	public byte Sequence = 0; 
	
	//	The physical input port from which DMX512 data was 
	//	input. This field is for information only. Use Universe 
	//	for data routing. 	
	byte Physical = 1; // is this channel?

	//	The high byte is currently set to zero. The low byte is 
	//	the address of this Universe of data. In DMX-Hub, the 
	//	high nibble is the Sub-net switch and the low Nibble 
	//	is the Universe address switch.  
	//	Transmitted low byte first. 	
	byte Universe = 0;

	//	The length of the DMX512 data array. This value 
	//	should be an even number in the range 2 � 512.  
	//	It represents the number of DMX512 channels 
	//	received. 
	//	High Byte. 
	//byte LengthHi; // LengthHi and Length will be set by getLength()

	//	The length of the DMX512 data array. This value 
	//	should be an even number in the range 2 � 512.  
	//	It represents the number of DMX512 channels 
	//	received. 
	//	Low Byte. 
	//byte Length;

	//	An array of DMX512 lighting data.	Will be length Length.
	byte[] Data = new byte[0];

	public ByteBuffer getBytes(){
		return super.getBytes().
						put(Util.getLoByte(OpCode)).
						put(Util.getHiByte(OpCode)).
						put(ProtVer).put(ProtVerH). // this is backwards of the spec, but same as the 3rd party test code.
//						put(ProtVerH).put(ProtVer). // this is as of the spec.
						put(getSequence()).			// also- ProtVer, ProtVerH should probably be in ArtNetData.
						put(Physical).
						put(Universe).
						put((byte)0). // hi byte defaults to '0'
						put(getLengthHi()).
						put(getLength()).
						put(Data);
	}
	public byte getLengthHi() {
		return Util.getHiByte((short)Data.length);
	}
	public byte getLength() {
		return Util.getLoByte((short)Data.length);
	}
	// to be implemented (should autogenerate)
	public byte getSequence() {
		return Sequence;
	}	

	// setters
	public void setData(byte[] Data){
		this.Data = Data;
	}
	public void setPhysical(byte Physical){
		this.Physical = Physical;
	}
	public void setUniverse(byte Universe) {
		this.Universe = Universe;
	}
}