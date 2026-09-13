# 云端平台前端 · Web Frontend

「AI 陪伴桌面机器人」的**可视化管理前端**，提供设备状态、传感器数据、视觉看护画面与 AI 对话的统一操作界面。

## 技术栈

| 项 | 说明 |
| --- | --- |
| 框架 | Vue **2.x** + Element UI + Vuex + Vue Router |
| 构建 | Vue CLI（webpack），配置见 `vue.config.js` |
| 运行时 | Node.js 16+ |
| 开发端口 | **8088** |
| 后端代理 | `http://localhost:8080`（可用 `VUE_APP_BACKEND_URL` 覆盖） |

> 基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)（MIT）前端二次开发，原始说明见 [`README_RuoYiUI-upstream.md`](README_RuoYiUI-upstream.md)。

## 自研业务页面

| 页面 | 文件 | 说明 |
| --- | --- | --- |
| 首页 · 暖芯陪伴机器人 | `src/views/index.vue` | 视觉看护 MJPEG 实时画面 + AI 对话主界面 |
| 设备管理 | `src/views/device/index.vue` | 设备列表、在线状态、远程 PWM 灯控下发 |
| 设备类型 | `src/views/device/type/index.vue` | 物模型 / 设备分类维护 |
| 传感器数据 | `src/views/device/sensor/index.vue` | 温湿度 / 光照 / 空气质量等上报数据查询 |
| AI 设置 | `src/views/ai/model/index.vue` | 大模型供应商、接口地址、API Key、系统提示词配置 |

对应的接口封装：

```text
src/api/companion.js          # 视觉看护 / AI 对话
src/api/device/device.js      # 设备与 PWM 下发
src/api/device/deviceType.js  # 设备类型
src/api/ai/model.js           # AI 模型配置
```

> `src/views/{system,monitor,tool}`、`src/views/dashboard`、`src/views/error` 等为 RuoYi 框架自带页面，非自研。
>
> `src/views/index_v1.vue` 为首页旧版本残留文件，未被路由引用，可清理。

## 环境变量

| 文件 | 用途 | 关键项 |
| --- | --- | --- |
| `.env.development` | 开发 | `VUE_APP_BASE_API` 代理前缀 |
| `.env.production` | 生产 | `VUE_APP_BASE_API = '/prod-api'` |
| `.env.staging` | 预发布 | 同上 |

## 构建与运行

```bash
npm install          # 安装依赖

npm run dev          # 开发模式，http://localhost:8088
npm run build:prod   # 生产构建，产出 dist/
npm run build:stage  # 预发布构建
```

开发模式下前端通过 `vue.config.js` 的 devServer proxy 把 `VUE_APP_BASE_API` 转发到后端 `http://localhost:8080`，因此**必须先启动后端**。

## 部署

生产环境把 `dist/` 交给 Nginx 托管，并将 `/prod-api` 反向代理到后端：

```nginx
location /prod-api/ {
    proxy_set_header Host $http_host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_pass http://127.0.0.1:8080/;
}
```

## 默认账号

`admin / admin123`
