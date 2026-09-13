package com.ruoyi.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.netty.tcp.DeviceTcpSendService;
import com.ruoyi.netty.tcp.NettyPacketType;
import com.ruoyi.web.domain.Device;
import com.ruoyi.web.domain.DevicePwmSetRequest;
import com.ruoyi.web.service.IDeviceService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 设备信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/device/info")
public class DeviceController extends BaseController
{
    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DeviceTcpSendService deviceTcpSendService;

    /**
     * 查询设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('device:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(Device device)
    {
        startPage();
        List<Device> list = deviceService.selectDeviceList(device);
        return getDataTable(list);
    }

    /**
     * 获取设备信息详情
     */
    @PreAuthorize("@ss.hasPermi('device:info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(deviceService.selectDeviceById(id));
    }

    /**
     * 新增设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:add')")
    @Log(title = "设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Device device)
    {
        device.setCreateBy(getUsername());
        return toAjax(deviceService.insertDevice(device));
    }

    /**
     * 修改设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:edit')")
    @Log(title = "设备信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Device device)
    {
        device.setUpdateBy(getUsername());
        return toAjax(deviceService.updateDevice(device));
    }

    /**
     * 删除设备信息
     */
    @PreAuthorize("@ss.hasPermi('device:info:remove')")
    @Log(title = "设备信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceService.deleteDeviceByIds(ids));
    }

    /**
     * 向在线设备下发 RGB PWM（{@link NettyPacketType#PWM_SET}），执行结果由下位机 {@link NettyPacketType#PWM_SET_RESP} 上报。
     */
    @PreAuthorize("@ss.hasPermi('device:info:pwm')")
    @Log(title = "设备PWM设置", businessType = BusinessType.UPDATE)
    @PostMapping("/pwm")
    public AjaxResult setPwm(@Validated @RequestBody DevicePwmSetRequest req)
    {
        Device device = deviceService.selectDeviceByDid(req.getDid());

        if (device == null)
        {
            return error("设备不存在");
        }

        if (device.getOnline() == null || device.getOnline() != 1)
        {
            return error("设备离线，无法下发");
        }

        ByteBuf pwmBody = Unpooled.buffer(3);
        byte[] body;
        try
        {
            pwmBody.writeByte(req.getPwmLamp1());
            pwmBody.writeByte(req.getPwmLamp2());
            pwmBody.writeByte(req.getPwmLamp3());
            body = new byte[pwmBody.readableBytes()];
            pwmBody.readBytes(body);
        }
        finally
        {
            pwmBody.release();
        }

        boolean sent = deviceTcpSendService.sendByDid(req.getDid(), NettyPacketType.PWM_SET, body);
        if (!sent)
        {
            return error("TCP 下发失败，请确认设备已建立连接");
        }

        return success("PWM 指令已下发，执行结果由下位机回报");
    }
}
