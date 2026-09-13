package com.ruoyi.web.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.web.domain.DeviceType;

/**
 * 设备类型 数据层
 *
 * @author ruoyi
 */
public interface DeviceTypeMapper
{
    DeviceType selectDeviceTypeById(Long id);

    List<DeviceType> selectDeviceTypeList(DeviceType deviceType);

    /**
     * 按类型名称精确查询（唯一性校验）
     *
     * @param name 类型名称
     * @return 记录或 null
     */
    DeviceType selectDeviceTypeByName(@Param("name") String name);

    int insertDeviceType(DeviceType deviceType);

    int updateDeviceType(DeviceType deviceType);

    int deleteDeviceTypeById(Long id);

    int deleteDeviceTypeByIds(Long[] ids);
}
