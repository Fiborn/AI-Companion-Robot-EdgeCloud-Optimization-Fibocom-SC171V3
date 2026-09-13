package com.ruoyi.netty.tcp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty TCP 服务配置（application.yml: ruoyi.netty.tcp）
 */
@ConfigurationProperties(prefix = "ruoyi.netty.tcp")
public class NettyTcpProperties
{
    /** 是否启用 */
    private boolean enabled = true;

    /** 监听端口 */
    private int port = 8081;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
