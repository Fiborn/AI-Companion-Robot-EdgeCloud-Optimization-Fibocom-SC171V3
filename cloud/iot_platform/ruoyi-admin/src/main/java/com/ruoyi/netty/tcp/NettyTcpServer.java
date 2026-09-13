package com.ruoyi.netty.tcp;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty TCP 服务端：应用就绪后启动，随 Spring 容器销毁而关闭。
 */
@Component
@ConditionalOnProperty(prefix = "ruoyi.netty.tcp", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(NettyTcpProperties.class)
public class NettyTcpServer implements ApplicationListener<ApplicationReadyEvent>, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(NettyTcpServer.class);

    private final NettyTcpProperties properties;

    private final DeviceTcpSessionRegistry deviceTcpSessionRegistry;

    private final TcpBinaryInboundHandler tcpBinaryInboundHandler;

    private volatile EventLoopGroup bossGroup;
    private volatile EventLoopGroup workerGroup;
    private volatile Channel serverChannel;
    private volatile Thread serverThread;

    public NettyTcpServer(NettyTcpProperties properties, DeviceTcpSessionRegistry deviceTcpSessionRegistry,
        TcpBinaryInboundHandler tcpBinaryInboundHandler)
    {
        this.properties = properties;
        this.deviceTcpSessionRegistry = deviceTcpSessionRegistry;
        this.tcpBinaryInboundHandler = tcpBinaryInboundHandler;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        serverThread = new Thread(this::runServer, "netty-tcp-server");
        serverThread.setDaemon(false);
        serverThread.start();
    }

    private void runServer()
    {
        TcpChannelInitializer childHandler = new TcpChannelInitializer(deviceTcpSessionRegistry, tcpBinaryInboundHandler);

        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(childHandler)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture bindFuture = b.bind(properties.getPort()).sync();
            serverChannel = bindFuture.channel();
            log.info("Netty TCP 服务已启动，监听端口 {}", properties.getPort());

            serverChannel.closeFuture().sync();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            log.warn("Netty TCP 服务线程被中断");
        }
        catch (Exception e)
        {
            log.error("Netty TCP 服务启动或运行失败", e);
        }
        finally
        {
            shutdownEventLoopGroups();
        }
    }

    private void shutdownEventLoopGroups()
    {
        if (bossGroup != null)
        {
            bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
        if (workerGroup != null)
        {
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    @Override
    public void destroy()
    {
        if (serverChannel != null && serverChannel.isOpen())
        {
            serverChannel.close().syncUninterruptibly();
        }
        if (serverThread != null)
        {
            try
            {
                serverThread.join(TimeUnit.SECONDS.toMillis(15));
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        shutdownEventLoopGroups();
    }
}
