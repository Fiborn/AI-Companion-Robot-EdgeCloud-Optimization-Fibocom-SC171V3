package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.DeviceType;

/**
 * 设备类型 服务层
 *
 * @author ruoyi
 */
public interface IDeviceTypeService
{
    DeviceType selectDeviceTypeById(Long id);

    List<DeviceType> selectDeviceTypeList(DeviceType deviceType);

    int insertDeviceType(DeviceType deviceType);

    int updateDeviceType(DeviceType deviceType);

    int deleteDeviceTypeByIds(Long[] ids);

    int deleteDeviceTypeById(Long id);
}
