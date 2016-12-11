package com.damonseeley.lightingutilsdemo.core;

import net.electroland.eio.InputChannel;

public interface ChannelDriven {
    public void fire(EventMetaData meta, ClipPlayer cp, InputChannel channel);
}