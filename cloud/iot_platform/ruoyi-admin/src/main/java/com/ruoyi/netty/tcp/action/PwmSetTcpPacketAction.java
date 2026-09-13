package com.ruoyi.netty.tcp.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理下位机 {@link NettyPacketType#PWM_SET_RESP}（应答服务器下发的 {@link NettyPacketType#PWM_SET} RGB PWM）。
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.PWM_SET_RESP })
public class PwmSetTcpPacketAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(PwmSetTcpPacketAction.class);

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        ByteBuf body = packet.getBody();
        int bodyLen = body.readableBytes();
        if (bodyLen < 1)
        {
            log.warn("PWM_SET_RESP 包体缺少状态码 bodyLen={}, remote={}", bodyLen, ctx.channel().remoteAddress());
            return;
        }

        byte status = body.readByte();
        log.info("PWM_SET_RESP status=0x{} extraLen={}, remote={}",
            String.format("%02x", status & 0xff),
            body.readableBytes(),
            ctx.channel().remoteAddress());
    }
}
