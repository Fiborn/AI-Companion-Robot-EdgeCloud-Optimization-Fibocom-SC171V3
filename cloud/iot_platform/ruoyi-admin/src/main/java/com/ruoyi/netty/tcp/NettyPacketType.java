package com.ruoyi.netty.tcp;

/**
 * TCP 二进制数据包类型（2 字节无符号值，大端序与包头一致）。<br>
 * <strong>指令分「发送/请求」与「返回」：</strong>发送类常量表示主动下发或请求；对应返回类常量名称以 {@code _RESP} 结尾。<br>
 * 所有类型在此文件集中维护，便于与设备端协议对齐。
 */
public final class NettyPacketType
{
    // ---------- 发送 / 请求 ----------

    /** Active（发送） */
    public static final short ACTIVE = 0x0001;
    public static final short ACTIVE_RESP = 0x0002;
    /** 设备登录（发送） */
    public static final short DEVICE_LOGIN = 0x0003;
    public static final short DEVICE_LOGIN_RESP = 0x0004;
    /** 设备上报数据（发送） */
    public static final short DEVICE_DATA_REPORT = 0x0010;
    public static final short DEVICE_DATA_REPORT_RESP = 0x0011;

    public static final short PWM_SET = 0x0012;
    public static final short PWM_SET_RESP = 0x0013;

    /** 下位机主动上报传感器数据 */
    public static final short SENSOR_REPORT = 0x0020;
    public static final short SENSOR_REPORT_RESP = 0x0021;
    /** 平台查询传感器数据 */
    public static final short SENSOR_QUERY = 0x0022;
    public static final short SENSOR_QUERY_RESP = 0x0023;

    public static final short CAMERA_REPORT = 0x0030;
    public static final short CAMERA_REPORT_RESP = 0x0031;
    public static final short CAMERA_QUERY = 0x0032;
    public static final short CAMERA_PHOTO = 0x0033;
    public static final short CAMERA_PHOTO_RESP = 0x0034;
    public static final short CAMERA_FRAME = 0x0035;
    public static final short CAMERA_FRAME_RESP = 0x0036;
    public static final short CAMERA_CONFIG = 0x0037;
    public static final short CAMERA_CONFIG_RESP = 0x0038;

    /** 平台下行指令（发送） */
    public static final short PLATFORM_COMMAND = 0x0100;
    public static final short PLATFORM_COMMAND_RESP = 0x0101;

    /**
     * 是否为已登记类型（未知类型仍可按通用逻辑处理，此处用于日志分类）
     */
    public static boolean isKnown(short typeCode)
    {
        return typeCode == ACTIVE
            || typeCode == ACTIVE_RESP
            || typeCode == DEVICE_LOGIN
            || typeCode == DEVICE_LOGIN_RESP
            || typeCode == DEVICE_DATA_REPORT
            || typeCode == DEVICE_DATA_REPORT_RESP
            || typeCode == PLATFORM_COMMAND
            || typeCode == PLATFORM_COMMAND_RESP
            || typeCode == PWM_SET
            || typeCode == PWM_SET_RESP
            || typeCode == SENSOR_REPORT
            || typeCode == SENSOR_REPORT_RESP
            || typeCode == SENSOR_QUERY
            || typeCode == SENSOR_QUERY_RESP
            || typeCode == CAMERA_REPORT
            || typeCode == CAMERA_REPORT_RESP
            || typeCode == CAMERA_QUERY
            || typeCode == CAMERA_PHOTO
            || typeCode == CAMERA_PHOTO_RESP
            || typeCode == CAMERA_FRAME
            || typeCode == CAMERA_FRAME_RESP
            || typeCode == CAMERA_CONFIG
            || typeCode == CAMERA_CONFIG_RESP;
    }
}
