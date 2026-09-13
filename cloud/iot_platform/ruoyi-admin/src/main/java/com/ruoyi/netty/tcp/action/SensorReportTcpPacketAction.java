package com.ruoyi.netty.tcp.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.CompanionSensorSnapshotService;
import com.ruoyi.netty.tcp.NettyPacketStatusCode;
import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;
import com.ruoyi.netty.tcp.TcpBinaryPacketCodec;
import com.ruoyi.netty.tcp.TcpChannelAttributes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Handles board-side SENSOR_REPORT packets.
 *
 * Body format from lower_python:
 * count:u8 + repeated(count) { sensor_type:u8, value:double, timestamp:u32 }.
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.SENSOR_REPORT })
public class SensorReportTcpPacketAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(SensorReportTcpPacketAction.class);

    private static final int SENSOR_VALUE_BYTES = 13;

    private final CompanionSensorSnapshotService sensorSnapshotService;

    public SensorReportTcpPacketAction(CompanionSensorSnapshotService sensorSnapshotService)
    {
        this.sensorSnapshotService = sensorSnapshotService;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        ByteBuf body = packet.getBody();
        byte status = NettyPacketStatusCode.SUCCESS;

        try
        {
            if (body.readableBytes() < 1)
            {
                status = NettyPacketStatusCode.ERROR_Tag;
                return;
            }

            int count = body.readUnsignedByte();
            if (body.readableBytes() < count * SENSOR_VALUE_BYTES)
            {
                status = NettyPacketStatusCode.ERROR_Tag;
                return;
            }

            List<CompanionSensorSnapshotService.SensorValue> values = new ArrayList<>();
            for (int i = 0; i < count; i++)
            {
                int sensorType = body.readUnsignedByte();
                double value = body.readDouble();
                long timestamp = body.readUnsignedInt();
                values.add(new CompanionSensorSnapshotService.SensorValue(sensorType, value, timestamp));
            }

            String did = ctx.channel().attr(TcpChannelAttributes.DEVICE_DID).get();
            if (did == null || did.trim().isEmpty())
            {
                status = NettyPacketStatusCode.ERROR_LOGIN;
                log.warn("传感器上报被拒绝：设备未登录 remote={}", ctx.channel().remoteAddress());
                return;
            }

            sensorSnapshotService.update(did, values);
            log.debug("传感器上报已接收 did={}, count={}", did, values.size());
        }
        finally
        {
            ByteBuf resp = TcpBinaryPacketCodec.encode(ctx.alloc(), NettyPacketType.SENSOR_REPORT_RESP, new byte[] { status });
            ctx.writeAndFlush(resp);
        }
    }
}
