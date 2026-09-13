package com.ruoyi.netty.tcp;

import io.netty.buffer.ByteBuf;

/**
 * 一帧完整二进制包（解码后）：链路上为 2 字节整帧长度 + 2 字节类型 + 包体；此处仅保留类型与包体 {@link ByteBuf}。
 */
public final class TcpBinaryPacket
{
    private final int packetType;
    private final ByteBuf body;

    public TcpBinaryPacket(int packetType, ByteBuf body)
    {
        this.packetType = packetType;
        this.body = body;
    }

    public int getPacketType()
    {
        return packetType;
    }

    public ByteBuf getBody()
    {
        return body;
    }
}
