package com.ruoyi.netty.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.ruoyi.web.service.IDeviceService;

/**
 * 应用启动后将 {@code s_device} 中全部记录的在线状态与 TCP 连接信息恢复为离线/未连接，避免上次进程异常退出后库中仍显示在线。
 */
@Component
public class DeviceOnlineStateStartupRunner implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(DeviceOnlineStateStartupRunner.class);

    private final IDeviceService deviceService;

    public DeviceOnlineStateStartupRunner(IDeviceService deviceService)
    {
        this.deviceService = deviceService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        int rows = deviceService.resetAllDevicesOnlineLoginInfo();
        log.info("应用启动：已重置全部设备的在线与连接信息（online=0, ip/port 清空），更新行数={}", rows);
    }
}
