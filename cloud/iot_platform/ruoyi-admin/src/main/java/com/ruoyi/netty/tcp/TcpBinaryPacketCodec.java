package com.ruoyi.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * 与 {@link TcpBinaryFrameDecoder} 对称：写入 2 字节<strong>整帧长度</strong>（含 4 字节头）+ 2 字节类型 + 包体。
 */
public final class TcpBinaryPacketCodec
{
    private TcpBinaryPacketCodec()
    {
    }

    /**
     * 编码一帧（包体可为 null 表示长度为 0）
     */
    public static ByteBuf encode(ByteBufAllocator alloc, int packetType, byte[] body)
    {
        int n = body == null ? 0 : body.length;
        if (n > TcpBinaryFrameDecoder.MAX_BODY_LENGTH)
        {
            throw new IllegalArgumentException("包体长度超过上限: " + n);
        }

        int totalLength = TcpBinaryFrameDecoder.HEADER_LENGTH + n;

        ByteBuf frame = alloc.buffer(totalLength);
        
        frame.writeShort(totalLength);
        frame.writeShort(packetType);

        if (n > 0)
        {
            frame.writeBytes(body);
        }

        return frame;
    }

    /**
     * 使用非池化缓冲编码（适用于短包、工具调用）
     */
    public static ByteBuf encodeUnpooled(int packetType, byte[] body)
    {
        return encode(UnpooledByteBufAllocator.DEFAULT, packetType, body);
    }
}
