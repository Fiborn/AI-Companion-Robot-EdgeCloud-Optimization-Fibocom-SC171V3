package com.ruoyi.web.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 设备 RGB PWM 设置（HTTP 请求体；pwmLamp1/2/3 依次为红、绿、蓝）
 */
public class DevicePwmSetRequest
{
    /** 设备标识 did */
    @NotBlank(message = "设备标识不能为空")
    @Size(max = 64, message = "设备标识长度不能超过64个字符")
    private String did;

    /** 红色 PWM 占空比 0～255（与 JSON 字段 pwmLamp1 对应） */
    @Min(value = 0, message = "红色占空比不能小于0")
    @Max(value = 255, message = "红色占空比不能大于255")
    private int pwmLamp1;

    /** 绿色 PWM 占空比 0～255（与 JSON 字段 pwmLamp2 对应） */
    @Min(value = 0, message = "绿色占空比不能小于0")
    @Max(value = 255, message = "绿色占空比不能大于255")
    private int pwmLamp2;

    /** 蓝色 PWM 占空比 0～255（与 JSON 字段 pwmLamp3 对应） */
    @Min(value = 0, message = "蓝色占空比不能小于0")
    @Max(value = 255, message = "蓝色占空比不能大于255")
    private int pwmLamp3;

    public String getDid()
    {
        return did;
    }

    public void setDid(String did)
    {
        this.did = did;
    }

    public int getPwmLamp1()
    {
        return pwmLamp1;
    }

    public void setPwmLamp1(int pwmLamp1)
    {
        this.pwmLamp1 = pwmLamp1;
    }

    public int getPwmLamp2()
    {
        return pwmLamp2;
    }

    public void setPwmLamp2(int pwmLamp2)
    {
        this.pwmLamp2 = pwmLamp2;
    }

    public int getPwmLamp3()
    {
        return pwmLamp3;
    }

    public void setPwmLamp3(int pwmLamp3)
    {
        this.pwmLamp3 = pwmLamp3;
    }
}
