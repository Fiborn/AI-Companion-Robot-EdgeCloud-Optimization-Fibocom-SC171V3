package com.ruoyi.netty.tcp.action;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ruoyi.netty.tcp.NettyPacketType;

/**
 * 声明该 {@link TcpPacketAction} 处理的 TCP 包类型码，取值使用 {@link NettyPacketType} 中的常量。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HandlesNettyPacketType
{
    /**
     * 包类型码（如 {@link NettyPacketType#ACTIVE}、{@link NettyPacketType#ACTIVE_RESP}）
     */
    int[] value();
}
