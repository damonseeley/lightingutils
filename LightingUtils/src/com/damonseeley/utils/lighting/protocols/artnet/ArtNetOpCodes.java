package com.damonseeley.utils.lighting.protocols.artnet;

/**
 * These are the Art-Net v.1.4 Opcodes as defined in the art net spec:
 * 
 * http://www.artisticlicence.com/user-guides/art-net.pdf
 * 
 * @author geilfuss
 *
 */

public class ArtNetOpCodes {

	//	This is an ArtPoll packet, no other data is contained in this UDP
	//	packet.
	final static short OpPoll = (short)0x2000;

	//	This is an ArtPollReply Packet. It contains device status
	//	information.
	final static short OpPollReply = (short)0x2100;

	//	This is an ArtDmx data packet. It contains DMX512 information
	//	for a single Universe.
	final static short OpOutput = (short)0x5000;

	//	This is an ArtAddress packet. It contains remote programming
	//	information for a Node.
	final static short OpAddress = (short)0x6000;

	//	This is an ArtInput packet. It contains enable � disable data for
	//	DMX inputs.
	final static short OpInput = (short)0x7000;

	//	This is an ArtTodRequest packet. It is used to request a Table
	//	of Devices (ToD) for RDM discovery.
	final static short OpTodRequest = (short)0x8000;

	//	This is an ArtTodData packet. It is used to send a Table of
	//	Devices (ToD) for RDM discovery.
	final static short OpTodData = (short)0x8100;

	//	This is an ArtTodControl packet. It is used to send RDM
	//	discovery control messages.
	final static short OpTodControl = (short)0x8200;

	//	This is an ArtRdm packet. It is used to send all non discovery
	//	RDM messages.
	final static short OpRdm = (short)0x8300;

	//	This is an ArtRdmSub packet. It is used to send compressed,
	//	RDM Sub-Device data. DMX inputs.
	final static short OpRdmSub = (short)0x8400;

	//	This is an ArtVideoSetup packet. It contains video screen setup
	//	information for nodes that implement the extended video
	//	features.
	final static short OpVideoSetup = (short)0xa010;

	//	This is an ArtVideoPalette packet. It contains colour palette
	//	setup information for nodes that implement the extended video
	//	features.
	final static short OpVideoPalette = (short)0xa020;

	//	This is an ArtVideoData packet. It contains display data for
	//	nodes that implement the extended video features.
	final static short OpVideoData = (short)0xa040;

	//	This is an ArtMacMaster packet. It is used to program the
	//	Node�s MAC address, Oem device type and ESTA manufacturer
	//	code. This is for factory initialisation of a Node. It is not to be
	//	used by applications.
	final static short OpMacMaster = (short)0xf000;

	//	This is an ArtMacSlave packet. It is returned by the node to
	//	acknowledge receipt of an ArtMacMaster packet.
	final static short OpMacSlave = (short)0xf100;

	//	This is an ArtFirmwareMaster packet. It is used to upload new
	//	firmware or firmware extensions to the Node.
	final static short OpFirmwareMaster = (short)0xf200;

	//	This is an ArtFirmwareReply packet. It is returned by the node
	//	to acknowledge receipt of an ArtFirmwareMaster packet.
	final static short OpFirmwareReply = (short)0xf300;

	//	This is an ArtIpProg packet. It is used to reprogramme the IP,
	//	Mask and Port address of the Node.
	final static short OpIpProg = (short)0xf800;

	//	This is an ArtIpProgReply packet. It is returned by the node to
	//	acknowledge receipt of an ArtIpProg packet.
	final static short OpIpProgReply = (short)0xf900;

	//	This is an ArtMedia packet. It is Unicast by a Media Server and
	//	acted upon by a Server.
	final static short OpMedia = (short)0x9000;

	//	This is an ArtMediaPatch packet. It is Unicast by a Server and
	//	acted upon by a Media Server.
	final static short OpMediaPatch = (short)0x9100;

	//	This is an ArtMediaControl packet. It is Unicast by a Server and
	//	acted upon by a Media Server.
	final static short OpMediaControl = (short)0x9200;

	//	This is an ArtMediaControlReply packet. It is Unicast by a Media
	//	Server and acted upon by a Server
	final static short OpMediaContrlReply = (short)0x9300;
}