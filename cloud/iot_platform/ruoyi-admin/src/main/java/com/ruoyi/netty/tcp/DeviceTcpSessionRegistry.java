package com.ruoyi.netty.tcp;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.web.service.IDeviceService;

import io.netty.channel.Channel;

/**
 * 在线 TCP 连接索引：会话 ID、设备 did 与 {@link Channel} 互查；连接关闭时自动解绑。<br>
 * 写路径（注册 / 绑定 did / 注销）使用写锁互斥，读路径使用读锁，避免多线程下映射与 Channel 状态不一致。
 */
@Component
public class DeviceTcpSessionRegistry
{
    private static final Logger log = LoggerFactory.getLogger(DeviceTcpSessionRegistry.class);

    private final IDeviceService deviceService;

    private final ReentrantReadWriteLock registryLock = new ReentrantReadWriteLock();

    private final Map<String, Channel> sessionIdToChannel = new ConcurrentHashMap<>();

    private final Map<String, Channel> didToChannel = new ConcurrentHashMap<>();

    public DeviceTcpSessionRegistry(IDeviceService deviceService)
    {
        this.deviceService = deviceService;
    }

    /**
     * 新连接：分配会话 ID 并登记
     *
     * @return 会话编号（32 位十六进制无连字符 UUID）
     */
    public String registerChannel(Channel channel)
    {
        registryLock.writeLock().lock();
        try
        {
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            channel.attr(TcpChannelAttributes.SESSION_ID).set(sessionId);
            sessionIdToChannel.put(sessionId, channel);
            log.debug("TCP 会话建立 sessionId={}, remote={}", sessionId, channel.remoteAddress());
            return sessionId;
        }
        finally
        {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * 将已存在于 {@code s_device.did} 的设备与当前连接绑定；同一 did 仅保留最新连接，旧连接会被关闭。
     */
    public void bindDid(Channel channel, String did)
    {
        if (did == null || did.isEmpty())
        {
            return;
        }
        Channel previous = null;
        registryLock.writeLock().lock();
        try
        {
            channel.attr(TcpChannelAttributes.DEVICE_DID).set(did);
            previous = didToChannel.put(did, channel);
            log.debug("TCP 绑定 did sessionId={}, did={}, remote={}",
                channel.attr(TcpChannelAttributes.SESSION_ID).get(), did, channel.remoteAddress());
        }
        finally
        {
            registryLock.writeLock().unlock();
        }
        // 在锁外关闭旧连接，避免旧连接 channelInactive→unregister 与本线程锁重入/死锁
        if (previous != null && previous != channel && previous.isActive())
        {
            log.info("设备 did={} 重复上线，关闭旧连接 {}", did, previous.remoteAddress());
            previous.close();
        }
    }

    /**
     * 连接关闭：移除会话与 did 映射
     */
    public void unregisterChannel(Channel channel)
    {
        String did;
        registryLock.writeLock().lock();
        try
        {
            String sessionId = channel.attr(TcpChannelAttributes.SESSION_ID).get();
            did = channel.attr(TcpChannelAttributes.DEVICE_DID).get();
            if (sessionId != null)
            {
                sessionIdToChannel.remove(sessionId, channel);
            }
            if (did != null)
            {
                didToChannel.remove(did, channel);
            }
            log.debug("TCP 会话注销 sessionId={}, did={}, remote={}", sessionId, did, channel.remoteAddress());
        }
        finally
        {
            registryLock.writeLock().unlock();
        }
        // 连接关闭后回写设备离线状态，并清空 IP/端口
        if (did != null && !did.isEmpty())
        {
            try
            {
                deviceService.updateOnlineInfoByDid(did, 0, null, null);
            }
            catch (Exception e)
            {
                log.warn("设备离线状态回写失败 did={}", did, e);
            }
        }
    }

    public Optional<Channel> findChannelBySessionId(String sessionId)
    {
        if (sessionId == null || sessionId.isEmpty())
        {
            return Optional.empty();
        }
        registryLock.readLock().lock();
        try
        {
            return Optional.ofNullable(sessionIdToChannel.get(sessionId)).filter(Channel::isActive);
        }
        finally
        {
            registryLock.readLock().unlock();
        }
    }

    public Optional<Channel> findChannelByDid(String did)
    {
        if (did == null || did.isEmpty())
        {
            return Optional.empty();
        }
        registryLock.readLock().lock();
        try
        {
            return Optional.ofNullable(didToChannel.get(did)).filter(Channel::isActive);
        }
        finally
        {
            registryLock.readLock().unlock();
        }
    }

    public int getOnlineSessionCount()
    {
        registryLock.readLock().lock();
        try
        {
            return sessionIdToChannel.size();
        }
        finally
        {
            registryLock.readLock().unlock();
        }
    }

    /** 当前连接上的会话编号（未建立 TCP 时为 null） */
    public String getSessionId(Channel channel)
    {
        return channel == null ? null : channel.attr(TcpChannelAttributes.SESSION_ID).get();
    }

    /** 已注册绑定的 did */
    public String getBoundDid(Channel channel)
    {
        return channel == null ? null : channel.attr(TcpChannelAttributes.DEVICE_DID).get();
    }
}
