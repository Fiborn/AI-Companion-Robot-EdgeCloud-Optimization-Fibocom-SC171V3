package com.ruoyi.netty.tcp;

import io.netty.util.AttributeKey;

/**
 * Netty Channel 上绑定的会话属性
 */
public final class TcpChannelAttributes
{
    private TcpChannelAttributes()
    {
    }

    /** 连接建立时分配的全局唯一会话编号（与 did 无关） */
    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("tcpSessionId");

    /** 设备登录成功后与 {@link com.ruoyi.web.domain.Device#getDid()} 一致 */
    public static final AttributeKey<String> DEVICE_DID = AttributeKey.valueOf("deviceDid");
}
