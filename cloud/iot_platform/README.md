# 云端平台后端 · IoT Platform

「AI 陪伴桌面机器人」的**自研物联网平台后端**，负责设备接入、物模型管理、传感器数据汇聚、AI 模型配置与视觉看护。

## 技术栈

| 项 | 版本 / 说明 |
| --- | --- |
| 框架 | RuoYi-Vue **3.9.2**（Spring Boot 3.x + MyBatis + MySQL + Redis） |
| JDK | 17+ |
| 构建 | Maven（含 `mvnw` 包装器，无需预装 Maven） |
| 设备接入 | Netty TCP Server，**端口 8081**，自研二进制协议 |

> 本目录基于开源项目 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)（MIT 许可）二次开发，原始说明见 [`README_RuoYi-upstream.md`](README_RuoYi-upstream.md)，许可证见 [`LICENSE`](LICENSE)。

## 模块结构

| 模块 | 说明 |
| --- | --- |
| `ruoyi-admin` | 启动入口 + **全部自研业务代码**（见下） |
| `ruoyi-common` | 通用工具、注解、常量 |
| `ruoyi-framework` | 框架核心：安全、拦截器、数据源 |
| `ruoyi-system` | 系统管理（用户 / 角色 / 菜单 / 字典） |
| `ruoyi-quartz` | 定时任务 |
| `ruoyi-generator` | 代码生成器 |

## 自研业务代码

全部位于 `ruoyi-admin/src/main/java/com/ruoyi/` 下，是区别于上游 RuoYi 框架的部分。

### `netty.tcp` —— 自研二进制设备协议（26 个类）

| 分组 | 类 |
| --- | --- |
| 服务与配置 | `NettyTcpServer`、`NettyTcpProperties`、`TcpChannelInitializer` |
| 编解码 | `TcpBinaryFrameDecoder`、`TcpBinaryPacketCodec`、`TcpBinaryPacket` |
| 会话管理 | `DeviceTcpSessionRegistry`、`TcpSessionLifecycleHandler`、`TcpChannelAttributes`、`DeviceOnlineStateStartupRunner` |
| 状态常量 | `NettyPacketType`、`NettyPacketStatusCode` |
| 下发与快照 | `DeviceTcpSendService`、`CompanionSensorSnapshotService`、`CompanionCameraSnapshotService` |
| `action/` 分发 | `TcpPacketAction`、`TcpPacketActionDispatcher`、`HandlesNettyPacketType`、`DeviceLoginAction`、`ActiveAction`、`DeviceDataReportTcpPacketAction`、`SensorReportTcpPacketAction`、`CameraReportTcpPacketAction`、`PwmSetTcpPacketAction`、`PlatformCommandTcpPacketAction` |

### `web` —— 平台业务（44 个类）

| 层 | 自研类 |
| --- | --- |
| Controller | `DeviceController`、`DeviceTypeController`、`AiModelConfigController`、`CompanionController` |
| Domain | `Device`、`DeviceType`、`AiModelConfig`、`DevicePwmSetRequest`、`CompanionChatRequest` |
| Service | `IDeviceService`、`IDeviceTypeService`、`IAiModelConfigService`、`CompanionAiChatService`、`VoiceServerConfigService`（+ 各实现类） |
| Mapper | `DeviceMapper`、`DeviceTypeMapper`、`AiModelConfigMapper`（+ `resources/mapper/` 下同名 XML） |

> `web/controller/{system,monitor,tool,common}` 与 `web/core` 为 RuoYi 框架自带，非自研。

## 设备二进制协议

帧格式（**大端**）：

```
+-----------------+-----------------+------------------+
| 2B  总长度       | 2B  包类型       |  body            |
+-----------------+-----------------+------------------+
```

- 单帧上限 65535 字节（由 2 字节长度域决定）
- 包类型定义：`com.ruoyi.netty.tcp.NettyPacketType`
- 状态码定义：`com.ruoyi.netty.tcp.NettyPacketStatusCode`
- 分发机制：`@HandlesNettyPacketType` 注解 + `TcpPacketActionDispatcher` 启动时自动建立「包类型 → 处理 Action」映射，新增报文只需加一个带注解的 Action

## 数据库

库名 `iot_platform`（MySQL 8）。**导入顺序不可颠倒**：

```bash
mysql -uroot -p -e "CREATE DATABASE iot_platform DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"

mysql -uroot -p iot_platform < sql/iot_platform.sql               # 1. 基础库（33 张表）
mysql -uroot -p iot_platform < sql/ai_model_config.sql            # 2. AI 模型配置表 + 菜单
mysql -uroot -p iot_platform < sql/ai_menu_move_out_of_tool.sql   # 3. 把「AI 设置」菜单移出「系统工具」目录
```

业务表仅 3 张：`s_device`、`s_device_type`、`s_ai_model_config`；其余为 RuoYi 框架表（`sys_*` / `gen_*` / `qrtz_*`）。

> **关于 SQL 编码**：原始工程中另有一份 `iot_platform.sql` 存在编码损坏（165 处字符串字面量未闭合，无法导入），已弃用。本目录内使用的是修复后的干净版本，可直接导入。

## 配置

配置文件位于 `ruoyi-admin/src/main/resources/`：`application.yml`、`application-druid.yml`。
所有敏感项均支持**环境变量覆盖**：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/iot_platform?...` | 数据库地址 |
| `MYSQL_USERNAME` | `root` | 数据库用户 |
| `MYSQL_PASSWORD` | `123456` | 数据库密码 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `TOKEN_SECRET` | 框架默认值 | JWT 签发密钥 |
| `RUOYI_PROFILE` | `./ruoyi-ui/public/img/upload` | 文件上传目录 |
| `AI2_ENV_PATH` | `../AI2/.env` | AI2 语音服务配置文件路径 |

> 仓库中保留的默认值均为**框架出厂值**，不包含任何真实密钥。生产部署请务必通过环境变量覆盖。

## 启动

```bash
# 前置：MySQL 与 Redis 已运行，且已按上面的顺序导入 SQL
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

服务端口：HTTP **8080**、设备 TCP **8081**。默认账号：`admin / admin123`

## 关联服务

| 服务 | 地址 |
| --- | --- |
| 前端管理界面 | http://127.0.0.1:8088/index |
| 后端接口 | http://127.0.0.1:8080 |
| 设备 TCP 接入 | 127.0.0.1:8081 |
| 小智 MCP 客户端 | http://localhost:9999 |
| 小智智控台 | http://localhost:8002 |
