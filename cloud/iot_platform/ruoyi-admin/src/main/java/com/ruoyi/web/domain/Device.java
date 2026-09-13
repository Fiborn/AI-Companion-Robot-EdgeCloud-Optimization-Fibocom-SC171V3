package com.ruoyi.web.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设备信息表 s_device
 *
 * @author ruoyi
 */
public class Device extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 设备标识 */
    private String did;

    /** 设备名称 */
    private String name;

    /** 设备类型ID（关联 s_device_type.id，列 device_type_id） */
    private Long deviceTypeId;

    /** 设备类型名称（关联查询，非表字段） */
    private String typeName;

    /** 是否在线（0离线 1在线） */
    private Integer online;

    /** 设备连接IP */
    private String ip;

    /** 设备连接端口 */
    private Integer port;

    /** 扩展内容 */
    private String content;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotBlank(message = "设备标识不能为空")
    @Size(max = 64, message = "设备标识长度不能超过64个字符")
    public String getDid()
    {
        return did;
    }

    public void setDid(String did)
    {
        this.did = did;
    }

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 128, message = "设备名称长度不能超过128个字符")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @NotNull(message = "设备类型不能为空")
    public Long getDeviceTypeId()
    {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId)
    {
        this.deviceTypeId = deviceTypeId;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public Integer getOnline()
    {
        return online;
    }

    public void setOnline(Integer online)
    {
        this.online = online;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("did", getDid())
            .append("name", getName())
            .append("deviceTypeId", getDeviceTypeId())
            .append("typeName", getTypeName())
            .append("online", getOnline())
            .append("ip", getIp())
            .append("port", getPort())
            .append("content", getContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
