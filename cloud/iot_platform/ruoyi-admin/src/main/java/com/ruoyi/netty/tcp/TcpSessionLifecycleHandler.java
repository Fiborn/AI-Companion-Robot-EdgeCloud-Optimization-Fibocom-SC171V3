package com.ruoyi.netty.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 在首包到达前完成会话登记；连接关闭时解绑。每个子通道一个实例（非 Sharable）。
 */
public class TcpSessionLifecycleHandler extends ChannelInboundHandlerAdapter
{
    private final DeviceTcpSessionRegistry sessionRegistry;

    public TcpSessionLifecycleHandler(DeviceTcpSessionRegistry sessionRegistry)
    {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        sessionRegistry.registerChannel(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
        sessionRegistry.unregisterChannel(ctx.channel());
        ctx.fireChannelInactive();
    }
}
