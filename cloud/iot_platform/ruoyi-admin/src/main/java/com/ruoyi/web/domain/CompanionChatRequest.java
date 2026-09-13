package com.ruoyi.web.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanionChatRequest
{
    private String did;

    private String message;

    public String getDid()
    {
        return did;
    }

    public void setDid(String did)
    {
        this.did = did;
    }

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容长度不能超过2000个字符")
    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
