package com.ruoyi.netty.tcp.action;

import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.channel.ChannelHandlerContext;

/**
 * TCP 二进制包业务处理：在实现类上通过 {@link HandlesNettyPacketType} 声明类型码，在 {@link #handle} 中实现逻辑。<br>
 * <strong>约定：</strong>不得释放 {@link TcpBinaryPacket#getBody()}，由 {@link com.ruoyi.netty.tcp.TcpBinaryInboundHandler} 统一释放。
 */
public interface TcpPacketAction
{
    /**
     * 处理数据包
     */
    void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet);
}
