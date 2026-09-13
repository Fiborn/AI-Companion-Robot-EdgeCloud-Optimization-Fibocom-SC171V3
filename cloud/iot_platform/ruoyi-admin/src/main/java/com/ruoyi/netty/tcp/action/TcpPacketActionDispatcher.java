package com.ruoyi.netty.tcp.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.ruoyi.netty.tcp.TcpBinaryPacket;

import io.netty.channel.ChannelHandlerContext;

/**
 * 将数据包按类型码分发给 {@link HandlesNettyPacketType} 声明的处理器。<br>
 * 类型码应在各实现之间互不重叠；若多个 Bean 声明同一类型码，则先遍历到的生效（依赖 Spring 注入 {@link List} 顺序）。
 */
@Component
public class TcpPacketActionDispatcher
{
    private static final Logger log = LoggerFactory.getLogger(TcpPacketActionDispatcher.class);

    private final Map<Integer, TcpPacketAction> typeToAction;

    public TcpPacketActionDispatcher(List<TcpPacketAction> actions)
    {
        Map<Integer, TcpPacketAction> map = new HashMap<>();
        for (TcpPacketAction action : actions)
        {
            Class<?> userClass = ClassUtils.getUserClass(action);
            HandlesNettyPacketType ann = AnnotationUtils.findAnnotation(userClass, HandlesNettyPacketType.class);
            if (ann == null || ann.value().length == 0)
            {
                throw new IllegalStateException(
                    "TcpPacketAction 必须在类上标注 @HandlesNettyPacketType 且至少包含一个类型码: " + userClass.getName());
            }
            for (int typeCode : ann.value())
            {
                map.putIfAbsent(typeCode, action);
            }
        }
        this.typeToAction = Collections.unmodifiableMap(map);
    }

    public void dispatch(ChannelHandlerContext ctx, TcpBinaryPacket packet)
    {
        int type = packet.getPacketType();
        TcpPacketAction action = typeToAction.get(type);
        if (action != null)
        {
            action.handle(ctx, packet);
            return;
        }
        log.warn("未注册 TCP 包类型处理器: type=0x{}, remote={}",
            Integer.toHexString(type), ctx.channel().remoteAddress());
    }
}
