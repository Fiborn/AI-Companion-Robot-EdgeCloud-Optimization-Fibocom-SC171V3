package com.ruoyi.netty.tcp;

import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 向在线设备 TCP 连接下发二进制帧（全局可注入本服务调用发送接口）。
 */
@Service
public class DeviceTcpSendService
{
    private final DeviceTcpSessionRegistry sessionRegistry;

    public DeviceTcpSendService(DeviceTcpSessionRegistry sessionRegistry)
    {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * 按会话 ID 发送（连接建立时分配，与 did 无关）
     *
     * @return 是否已写入 pipeline（连接不存在或未激活返回 false）
     */
    public boolean sendBySessionId(String sessionId, int packetType, byte[] body)
    {
        return sessionRegistry.findChannelBySessionId(sessionId)
            .map(ch -> writeAndFlush(ch, packetType, body))
            .orElse(false);
    }

    /**
     * 按设备标识 did（与 s_device.did 一致，且已注册绑定）发送
     */
    public boolean sendByDid(String did, int packetType, byte[] body)
    {
        return sessionRegistry.findChannelByDid(did)
            .map(ch -> writeAndFlush(ch, packetType, body))
            .orElse(false);
    }

    private boolean writeAndFlush(Channel channel, int packetType, byte[] body)
    {
        if (!channel.isActive())
        {
            return false;
        }
        ByteBuf frame = TcpBinaryPacketCodec.encode(channel.alloc(), packetType, body);
        channel.writeAndFlush(frame);
        return true;
    }
}
