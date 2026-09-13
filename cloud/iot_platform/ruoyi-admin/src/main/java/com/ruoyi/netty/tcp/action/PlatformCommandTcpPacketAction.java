package com.ruoyi.netty.tcp.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 平台下行指令（发送与设备侧返回）
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.PLATFORM_COMMAND })
public class PlatformCommandTcpPacketAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(PlatformCommandTcpPacketAction.class);

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        ByteBuf body = packet.getBody();
        if (packet.getPacketType() == NettyPacketType.PLATFORM_COMMAND)
        {
            log.debug("平台指令(发送) bodyLen={}, remote={}", body.readableBytes(), ctx.channel().remoteAddress());
        }
        else
        {
            log.debug("平台指令(返回) bodyLen={}, remote={}", body.readableBytes(), ctx.channel().remoteAddress());
        }
    }
}
