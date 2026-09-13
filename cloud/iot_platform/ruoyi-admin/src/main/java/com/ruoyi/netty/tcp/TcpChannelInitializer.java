package com.ruoyi.netty.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * TCP 子通道 pipeline
 */
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel>
{
    private final DeviceTcpSessionRegistry sessionRegistry;

    private final TcpBinaryInboundHandler inboundHandler;

    public TcpChannelInitializer(DeviceTcpSessionRegistry sessionRegistry, TcpBinaryInboundHandler inboundHandler)
    {
        this.sessionRegistry = sessionRegistry;
        this.inboundHandler = inboundHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch)
    {
        ch.pipeline().addLast(new TcpBinaryFrameDecoder());
        ch.pipeline().addLast(new TcpSessionLifecycleHandler(sessionRegistry));
        ch.pipeline().addLast(inboundHandler);
    }
}
