# 辅助工具与调试脚本

来源：`iot-platform\start.py`、`start-all.ps1`、`stop-all.ps1`、`start-platform.bat`、`tools\ai2\`。

## 启动脚本一览

| 脚本 | 作用 | 依赖 |
| --- | --- | --- |
| `start-platform.bat` | 一键启动全链路：AI2 语音服务 + 小智 MCP + 云端平台 | 下面两个脚本 |
| `ai2/start-iot-with-ai2.ps1` | AI2 语音(:8095) → 小智 MCP(:9999, Docker) → `start.py` | Docker Desktop |
| `start-all.ps1` | 只启 Docker 侧：xiaozhi-client + nuanxin-esp32-server | Docker Desktop |
| `stop-all.ps1` | 停止上述 Docker 容器 | Docker Desktop |

云端平台自身的启停由 `cloud/iot_platform/start.py` 负责：

```bash
python start.py                    # 端口复用，快速返回，服务后台常驻
python start.py --wait-full        # 等到完全就绪
python start.py --backend-only     # 只起后端
python start.py --frontend-only    # 只起前端
python start.py --keep-alive       # 常驻监控，Ctrl+C 停止
```

## 端口分配

| 端口 | 服务 |
| --- | --- |
| 8080 | 平台后端（HTTP） |
| 8081 | 设备接入（自研二进制 TCP） |
| 8088 | 前端开发服务器 |
| 8095 | AI2 语音服务 |
| 9999 | 小智客户端（MCP / Web UI） |
| 8000 / 8002 | 小智服务端 WebSocket / 智控台 |
| 8765 | 板端 HTTP API（供 MCP 桥接调用） |
| 3306 / 6379 | MySQL / Redis |

## 迁移注意

四个 PowerShell / bat 脚本中目前**硬编码了 `E:\ai\iot-platform\...` 绝对路径**，迁入本仓库后必须改为 `$PSScriptRoot` 相对路径，否则 Docker 启停与小智链路会指向旧目录。详见 [`../docs/目录映射与迁移方案.md`](../docs/目录映射与迁移方案.md) 第三节。

## 调试工具

| 文件 | 用途 |
| --- | --- |
| `ImportSql.java` / `classes/` | 独立的 SQL 导入小工具 |
| `sc171_bridge.py` | 板端 HTTP API（:8765）→ MCP stdio 桥接，可单独运行调试 |
| `camera-captures/` | 抓拍输出目录 |
