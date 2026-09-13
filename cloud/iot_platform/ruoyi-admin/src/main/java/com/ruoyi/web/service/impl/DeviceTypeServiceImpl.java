package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.DeviceType;
import com.ruoyi.web.mapper.DeviceTypeMapper;
import com.ruoyi.web.service.IDeviceTypeService;

/**
 * 设备类型 服务层实现
 *
 * @author ruoyi
 */
@Service
public class DeviceTypeServiceImpl implements IDeviceTypeService
{
    @Autowired
    private DeviceTypeMapper deviceTypeMapper;

    @Override
    public DeviceType selectDeviceTypeById(Long id)
    {
        return deviceTypeMapper.selectDeviceTypeById(id);
    }

    @Override
    public List<DeviceType> selectDeviceTypeList(DeviceType deviceType)
    {
        return deviceTypeMapper.selectDeviceTypeList(deviceType);
    }

    @Override
    public int insertDeviceType(DeviceType deviceType)
    {
        checkDeviceTypeNameUnique(deviceType);
        return deviceTypeMapper.insertDeviceType(deviceType);
    }

    @Override
    public int updateDeviceType(DeviceType deviceType)
    {
        checkDeviceTypeNameUnique(deviceType);
        return deviceTypeMapper.updateDeviceType(deviceType);
    }

    private void checkDeviceTypeNameUnique(DeviceType deviceType)
    {
        DeviceType exist = deviceTypeMapper.selectDeviceTypeByName(deviceType.getName());
        if (exist != null && (deviceType.getId() == null || !exist.getId().equals(deviceType.getId())))
        {
            throw new ServiceException("类型名称已存在，请更换");
        }
    }

    @Override
    public int deleteDeviceTypeByIds(Long[] ids)
    {
        return deviceTypeMapper.deleteDeviceTypeByIds(ids);
    }

    @Override
    public int deleteDeviceTypeById(Long id)
    {
        return deviceTypeMapper.deleteDeviceTypeById(id);
    }
}
