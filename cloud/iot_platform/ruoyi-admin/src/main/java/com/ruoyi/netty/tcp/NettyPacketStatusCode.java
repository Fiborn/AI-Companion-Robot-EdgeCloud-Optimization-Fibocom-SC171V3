package com.ruoyi.netty.tcp;

/**
 * TCP 二进制包 1 字节状态码，与设备端 {@code enum class code : std::uint8_t} 取值对齐。
 */
public final class NettyPacketStatusCode
{
    public static final byte SUCCESS = 0x00;               // 成功
    public static final byte ERROR = 0x01;                 // 失败，原因不详
    public static final byte ERROR_LOGIN = 0x02;           // 失败，尚未登录
    public static final byte ERROR_VERIFY = 0x03;          // 失败，登录校验错误（用户名、密码等）
    public static final byte ERROR_TYPE = 0x04;     // 失败，客户端类型或通道不匹配
    public static final byte ERROR_RIGHT = 0x05;           // 失败，权限不足
    public static final byte ERROR_SQL = 0x06;             // 失败，服务端 SQL 错误（严重）
    public static final byte ERROR_MEMORY = 0x07;          // 失败，内存不足或分配失败
    public static final byte ERROR_Tag = 0x08;             // 失败，数据包参数 / 标记非法
    public static final byte ERROR_ADDR = 0x09;            // 失败，IP 被封禁或不在白名单
    public static final byte ERROR_LOGINED = 0x0a;         // 失败，重复登录（会话已存在）
    public static final byte ERROR_LOCK = 0x0b;            // 失败，资源已锁定
    public static final byte ERROR_FORBID = 0x0c;          // 失败，账号或功能被禁用
    public static final byte ERROR_TIME_OUT = 0x0d;        // 失败，操作超时
    public static final byte ERROR_COMPRESS = (byte) 0xff;       //扩展：载荷压缩标记（非错误码；语义由具体协议约定）
}
