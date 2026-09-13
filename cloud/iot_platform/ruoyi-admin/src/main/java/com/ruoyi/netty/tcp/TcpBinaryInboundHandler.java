package com.ruoyi.netty.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.netty.tcp.action.TcpPacketActionDispatcher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 入站最后一环：按包类型交给 {@link TcpPacketActionDispatcher}，再路由到各 {@link com.ruoyi.netty.tcp.action.TcpPacketAction}。
 */
@Component
@ChannelHandler.Sharable
public class TcpBinaryInboundHandler extends SimpleChannelInboundHandler<TcpBinaryPacket>
{
    private static final Logger log = LoggerFactory.getLogger(TcpBinaryInboundHandler.class);

    private final TcpPacketActionDispatcher packetActionDispatcher;

    public TcpBinaryInboundHandler(TcpPacketActionDispatcher packetActionDispatcher)
    {
        this.packetActionDispatcher = packetActionDispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpBinaryPacket msg)
    {
        ByteBuf body = msg.getBody();
        try
        {
            packetActionDispatcher.dispatch(ctx, msg);
        }
        catch (Exception e)
        {
            log.error("TCP 包业务处理异常 type=0x{}, remote={}",
                Integer.toHexString(msg.getPacketType()), ctx.channel().remoteAddress(), e);
        }
        finally
        {
            body.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        log.warn("TCP 连接异常 remote={}: {}", ctx.channel().remoteAddress(), cause.toString());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
    {
        log.debug("TCP 连接关闭 remote={}", ctx.channel().remoteAddress());
    }
}
