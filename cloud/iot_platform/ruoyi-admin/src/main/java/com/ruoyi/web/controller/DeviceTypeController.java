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
import com.ruoyi.web.domain.DeviceType;
import com.ruoyi.web.service.IDeviceTypeService;

/**
 * 设备类型
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/device/type")
public class DeviceTypeController extends BaseController
{
    @Autowired
    private IDeviceTypeService deviceTypeService;

    @PreAuthorize("@ss.hasPermi('device:type:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceType deviceType)
    {
        startPage();
        List<DeviceType> list = deviceTypeService.selectDeviceTypeList(deviceType);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('device:type:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(deviceTypeService.selectDeviceTypeById(id));
    }

    @PreAuthorize("@ss.hasPermi('device:type:add')")
    @Log(title = "设备类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DeviceType deviceType)
    {
        deviceType.setCreateBy(getUsername());
        return toAjax(deviceTypeService.insertDeviceType(deviceType));
    }

    @PreAuthorize("@ss.hasPermi('device:type:edit')")
    @Log(title = "设备类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DeviceType deviceType)
    {
        deviceType.setUpdateBy(getUsername());
        return toAjax(deviceTypeService.updateDeviceType(deviceType));
    }

    @PreAuthorize("@ss.hasPermi('device:type:remove')")
    @Log(title = "设备类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(deviceTypeService.deleteDeviceTypeByIds(ids));
    }
}
