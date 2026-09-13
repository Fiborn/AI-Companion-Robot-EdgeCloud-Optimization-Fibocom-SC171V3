package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.Device;

/**
 * 设备信息 服务层
 *
 * @author ruoyi
 */
public interface IDeviceService
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
     * 按设备标识查询（s_device.did）
     *
     * @param did 设备标识
     * @return 设备或 null
     */
    Device selectDeviceByDid(String did);

    /**
     * 按 did 更新在线状态与连接地址
     *
     * @param did 设备标识
     * @param online 在线状态（0离线 1在线）
     * @param ip 连接IP
     * @param port 连接端口
     * @return 结果
     */
    int updateOnlineInfoByDid(String did, Integer online, String ip, Integer port);

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
     * 批量删除设备信息
     *
     * @param ids 主键数组
     * @return 结果
     */
    int deleteDeviceByIds(Long[] ids);

    /**
     * 删除设备信息
     *
     * @param id 主键
     * @return 结果
     */
    int deleteDeviceById(Long id);
}
