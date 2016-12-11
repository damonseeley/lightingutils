package com.damonseeley.utils.lighting.protocols.artnet;

import java.nio.ByteBuffer;

public class ArtNetData {	
	//	Array of 8 characters, the final character is a null 
	//	termination. 
	//	Value = �A� �r� �t� �-� �N� �e� �t� 0x00 
	byte[] ID =  {'A', 'r', 't', '-', 'N', 'e', 't', 0};


	public ByteBuffer getBytes(){
		// - what are the thread conditions for this?
		// - allocate or allocateDirect?
		// - what is the capacity as defined by the spec?
		return ByteBuffer.allocate(5000).put(ID);
	}
}