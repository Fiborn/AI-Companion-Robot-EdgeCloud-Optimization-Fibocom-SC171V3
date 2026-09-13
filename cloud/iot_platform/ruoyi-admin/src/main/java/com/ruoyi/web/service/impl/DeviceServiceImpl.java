package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.Device;
import com.ruoyi.web.mapper.DeviceMapper;
import com.ruoyi.web.service.IDeviceService;

/**
 * 设备信息 服务层实现
 *
 * @author ruoyi
 */
@Service
public class DeviceServiceImpl implements IDeviceService
{
    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public Device selectDeviceById(Long id)
    {
        return deviceMapper.selectDeviceById(id);
    }

    @Override
    public List<Device> selectDeviceList(Device device)
    {
        return deviceMapper.selectDeviceList(device);
    }

    @Override
    public Device selectDeviceByDid(String did)
    {
        return deviceMapper.selectDeviceByDid(did);
    }

    @Override
    public int updateOnlineInfoByDid(String did, Integer online, String ip, Integer port)
    {
        return deviceMapper.updateOnlineInfoByDid(did, online, ip, port);
    }

    @Override
    public int resetAllDevicesOnlineLoginInfo()
    {
        return deviceMapper.resetAllDevicesOnlineLoginInfo();
    }

    @Override
    public int insertDevice(Device device)
    {
        checkDeviceDidAndNameUnique(device);
        return deviceMapper.insertDevice(device);
    }

    @Override
    public int updateDevice(Device device)
    {
        checkDeviceDidAndNameUnique(device);
        return deviceMapper.updateDevice(device);
    }

    /**
     * 校验设备标识、名称全局唯一（新增或修改时排除自身）
     */
    private void checkDeviceDidAndNameUnique(Device device)
    {
        Device byDid = deviceMapper.selectDeviceByDid(device.getDid());
        if (byDid != null && (device.getId() == null || !byDid.getId().equals(device.getId())))
        {
            throw new ServiceException("设备标识已存在，请更换");
        }
        Device byName = deviceMapper.selectDeviceByName(device.getName());
        if (byName != null && (device.getId() == null || !byName.getId().equals(device.getId())))
        {
            throw new ServiceException("设备名称已存在，请更换");
        }
    }

    @Override
    public int deleteDeviceByIds(Long[] ids)
    {
        return deviceMapper.deleteDeviceByIds(ids);
    }

    @Override
    public int deleteDeviceById(Long id)
    {
        return deviceMapper.deleteDeviceById(id);
    }
}
