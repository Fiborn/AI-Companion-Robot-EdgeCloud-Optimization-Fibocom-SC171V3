package com.ruoyi.netty.tcp;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 二进制分包解码器。<br>
 * 包头 4 字节：前 2 字节为<strong>整帧长度</strong>（含本 4 字节头与后续包体，大端无符号），后 2 字节为数据包类型；随后为包体。
 */
public class TcpBinaryFrameDecoder extends ByteToMessageDecoder
{
    /** 包头固定长度 */
    public static final int HEADER_LENGTH = 4;

    /** 单帧在链路上的最大总长度（2 字节无符号上限） */
    public static final int MAX_TOTAL_FRAME_LENGTH = 65535;

    /** 包体最大长度（总长度减去固定头） */
    public static final int MAX_BODY_LENGTH = MAX_TOTAL_FRAME_LENGTH - HEADER_LENGTH;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
    {
        if (in.readableBytes() < HEADER_LENGTH)
        {
            return;
        }
        
        in.markReaderIndex();

        int totalLength = in.readUnsignedShort();
        int packetType = in.readUnsignedShort();

        if (totalLength < HEADER_LENGTH || totalLength > MAX_TOTAL_FRAME_LENGTH)
        {
            in.resetReaderIndex();
            ctx.close();
            return;
        }

        int bodyLength = totalLength - HEADER_LENGTH;
        if (in.readableBytes() < bodyLength)
        {
            in.resetReaderIndex();
            return;
        }

        ByteBuf body = in.readRetainedSlice(bodyLength);
        out.add(new TcpBinaryPacket(packetType, body));
    }
}
