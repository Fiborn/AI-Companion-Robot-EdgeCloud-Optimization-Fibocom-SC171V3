package com.ruoyi.netty.tcp.action;

import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryFrameDecoder;
import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Active 包发送 / Active 包返回
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.ACTIVE })
public class ActiveAction implements TcpPacketAction
{
    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        ByteBuf resp = ctx.alloc().buffer(TcpBinaryFrameDecoder.HEADER_LENGTH);
        resp.writeShort(TcpBinaryFrameDecoder.HEADER_LENGTH);
        resp.writeShort(NettyPacketType.ACTIVE_RESP);
        ctx.writeAndFlush(resp);
    }
}
