package com.ruoyi.netty.tcp.action;

import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.DeviceTcpSessionRegistry;
import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.netty.tcp.TcpBinaryPacket;
import com.ruoyi.netty.tcp.TcpBinaryPacketCodec;
import com.ruoyi.netty.tcp.NettyPacketStatusCode;
import com.ruoyi.web.domain.Device;
import com.ruoyi.web.service.IDeviceService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * 设备登录（发送与返回）<br>
 * 设备侧 {@link NettyPacketType#DEVICE_LOGIN} 包体：<strong>大端 2 字节无符号长度</strong> + 该长度字节的 did（UTF-8）；需在 {@code s_device} 中存在方可绑定。<br>
 * 服务端 {@link NettyPacketType#DEVICE_LOGIN_RESP} 包体：1 字节状态码，取值见 {@link NettyPacketStatusCode}。非 {@link NettyPacketStatusCode#SUCCESS} 时在发出应答后主动断开连接。
 */
@Component
@HandlesNettyPacketType({ NettyPacketType.DEVICE_LOGIN })
public class DeviceLoginAction implements TcpPacketAction
{
    private static final Logger log = LoggerFactory.getLogger(DeviceLoginAction.class);

    private final IDeviceService deviceService;

    private final DeviceTcpSessionRegistry sessionRegistry;

    public DeviceLoginAction(IDeviceService deviceService, DeviceTcpSessionRegistry sessionRegistry)
    {
        this.deviceService = deviceService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        byte status = NettyPacketStatusCode.ERROR;

        ByteBuf body = packet.getBody();
        String did = readDidFromBody(body);
        if (did.isEmpty())
        {
            log.warn("设备登录包体不完整或 did 无效 remote={}", ctx.channel().remoteAddress());
            status = NettyPacketStatusCode.ERROR;
        }
        else
        {
            Device device = deviceService.selectDeviceByDid(did);
            if (device == null)
            {
                log.warn("设备登录失败：did 不存在于系统中 did={}, remote={}", did, ctx.channel().remoteAddress());
                status = NettyPacketStatusCode.ERROR_VERIFY;
            }
            else
            {
                sessionRegistry.bindDid(ctx.channel(), did);
                SocketAddress address = ctx.channel().remoteAddress();
                InetSocketAddress remote = address instanceof InetSocketAddress ? (InetSocketAddress) address : null;
                String ip = remote == null || remote.getAddress() == null ? null : remote.getAddress().getHostAddress();
                Integer port = remote == null ? null : remote.getPort();
                deviceService.updateOnlineInfoByDid(did, 1, ip, port);
                log.info("设备登录成功 sessionId={}, did={}, name={}, remote={}", sessionRegistry.getSessionId(ctx.channel()), did, device.getName(), ctx.channel().remoteAddress());
                status = NettyPacketStatusCode.SUCCESS;
            }
        }

        ByteBuf resp = TcpBinaryPacketCodec.encode(ctx.alloc(), NettyPacketType.DEVICE_LOGIN_RESP, new byte[] { status });
        ChannelFuture flushed = ctx.writeAndFlush(resp);
        if (status != NettyPacketStatusCode.SUCCESS)
        {
            flushed.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String readDidFromBody(ByteBuf body)
    {
        if (body.readableBytes() < 2)
        {
            return "";
        }

        int len = body.getUnsignedShort(body.readerIndex());

        if (len <= 0 || body.readableBytes() < 2 + len)
        {
            return "";
        }
        body.skipBytes(2);
        byte[] raw = new byte[len];
        body.readBytes(raw, 0, len);
        return new String(raw, StandardCharsets.UTF_8).trim();
    }
}
