package com.ruoyi.netty.tcp.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 设备上报数据（发送与返回/ACK）
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.DEVICE_DATA_REPORT })
public class DeviceDataReportTcpPacketAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(DeviceDataReportTcpPacketAction.class);

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        ByteBuf body = packet.getBody();
        if (packet.getPacketType() == NettyPacketType.DEVICE_DATA_REPORT)
        {
            log.debug("设备数据上报(发送) bodyLen={}, remote={}", body.readableBytes(), ctx.channel().remoteAddress());
        }
        else
        {
            log.debug("设备数据上报(返回) bodyLen={}, remote={}", body.readableBytes(), ctx.channel().remoteAddress());
        }
    }
}
