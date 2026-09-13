package com.ruoyi.web.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.web.domain.Device;

/**
 * 设备信息 数据层
 *
 * @author ruoyi
 */
public interface DeviceMapper
{
    /**
     * 查询设备信息
     *
     * @param id 主键
     * @return 设备信息
     */
    Device selectDeviceById(Long id);

    /**
     * 查询设备信息列表
     *
     * @param device 查询条件
     * @return 集合
     */
    List<Device> selectDeviceList(Device device);

    /**
     * 按设备标识查询（唯一性校验）
     *
     * @param did 设备标识
     * @return 设备或 null
     */
    Device selectDeviceByDid(@Param("did") String did);

    /**
     * 按设备名称查询（唯一性校验）
     *
     * @param name 设备名称
     * @return 设备或 null
     */
    Device selectDeviceByName(@Param("name") String name);

    /**
     * 按 did 更新在线状态与连接地址
     *
     * @param did 设备标识
     * @param online 在线状态（0离线 1在线）
     * @param ip 连接IP
     * @param port 连接端口
     * @return 结果
     */
    int updateOnlineInfoByDid(@Param("did") String did, @Param("online") Integer online,
        @Param("ip") String ip, @Param("port") Integer port);

    /**
     * 将全部设备置为离线并清空连接 IP、端口（应用启动时清除陈旧 TCP 登录态）
     *
     * @return 更新行数
     */
    int resetAllDevicesOnlineLoginInfo();

    /**
     * 新增设备信息
     *
     * @param device 设备信息
     * @return 结果
     */
    int insertDevice(Device device);

    /**
     * 修改设备信息
     *
     * @param device 设备信息
     * @return 结果
     */
    int updateDevice(Device device);

    /**
     * 删除设备信息
     *
     * @param id 主键
     * @return 结果
     */
    int deleteDeviceById(Long id);

    /**
     * 批量删除设备信息
     *
     * @param ids 主键数组
     * @return 结果
     */
    int deleteDeviceByIds(Long[] ids);
}
