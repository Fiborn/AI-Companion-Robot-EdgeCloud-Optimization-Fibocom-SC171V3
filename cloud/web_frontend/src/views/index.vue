<template>
  <div class="app-container companion-home">
    <section class="hero">
      <div>
        <h1>{{ greeting }}，欢迎使用暖芯陪伴机器人后台</h1>
        <p>统一查看小暖的陪伴状态、语音会话、视觉看护、传感上下文和模型运行情况，快速处理机器人交互与安全事件。</p>
      </div>
      <div class="ai-state">
        <div class="ai-state-top">
          <div class="companion-face">
            <i class="el-icon-cpu" />
          </div>
          <div>
            <b>{{ deviceOnline ? '小暖在线' : '小暖离线' }}</b>
            <span>{{ deviceOnline ? '陪伴在线 · 语音与视觉感知中' : '机器人离线 · 等待连接' }}</span>
          </div>
        </div>
        <div class="voice-bar" aria-label="语音输入波形">
          <i v-for="bar in 6" :key="bar" />
        </div>
      </div>
    </section>

    <section class="metrics" aria-label="状态概览">
      <article class="metric-card">
        <div class="metric-icon blue"><i class="el-icon-odometer" /></div>
        <div><small>感知模组在线</small><strong>{{ onlineSensorCount }} / {{ sensors.length }}</strong><em>{{ sensorHealthText }}</em></div>
      </article>
      <article class="metric-card">
        <div class="metric-icon green"><i class="el-icon-data-analysis" /></div>
        <div><small>AI 推理负载</small><strong>{{ edgeUsage }}%</strong><em>{{ edgeStateText }}</em></div>
      </article>
      <article class="metric-card">
        <div class="metric-icon orange"><i class="el-icon-warning-outline" /></div>
        <div><small>陪伴安全事件</small><strong>{{ pendingAlerts }}</strong><em class="warn-text">{{ pendingAlerts ? '需查看' : '已清空' }}</em></div>
      </article>
      <article class="metric-card">
        <div class="metric-icon purple"><i class="el-icon-video-camera" /></div>
        <div><small>视觉看护</small><strong>{{ cameraConnected ? '在线' : '离线' }}</strong><em>{{ cameraPaused ? '已暂停' : '实时预览中' }}</em></div>
      </article>
    </section>

    <section class="workspace">
      <div class="left-stack">
        <div ref="cameraPanel" class="panel camera-panel">
          <div>
            <div class="panel-head">
              <h2 class="panel-title"><i class="el-icon-video-camera" />视觉看护预览</h2>
              <div class="panel-tools">
                <span class="chip">{{ cameraDevice || 'Camera / MIPI-CSI' }}</span>
                <span class="chip">{{ cameraConnected ? '机器人已上报' : '等待机器人上报' }}</span>
              </div>
            </div>
            <div ref="cameraStage" class="camera-stage" :class="{ paused: cameraPaused, offline: !cameraConnected }">
              <template v-if="cameraConnected">
                <img class="camera-stream" :src="activeCameraImageUrl" alt="视觉看护实时画面" @load="onCameraStreamLoad" @error="onCameraStreamError">
                <div class="camera-live-badge">
                  <i class="el-icon-video-camera-solid" />
                  <span>{{ cameraDevice || 'camera' }} · {{ cameraConfig.resolution }} · 实测 {{ cameraConfig.fps }}fps</span>
                </div>
              </template>
              <div v-if="!cameraConnected" class="camera-empty">
                <i class="el-icon-video-camera" />
                <strong>未检测到视觉看护视频源</strong>
                <span>请连接机器人摄像头模组，或在配置中填写视频流地址。</span>
                <el-button type="primary" size="small" @click="connectCamera">接入视觉模组</el-button>
              </div>
              <div v-if="false" class="camera-overlay">
                <div class="detect-box"><span>Person 96%</span></div>
                <i class="pose-line l1" />
                <i class="pose-line l2" />
                <i class="pose-line l3" />
                <i class="pose-line l4" />
              </div>
              <div class="camera-hud">
                <span><i class="rec-dot" :class="{ muted: !cameraConnected }" />{{ cameraConnected ? (cameraPaused ? 'PAUSED' : 'LIVE') : 'OFFLINE' }}</span>
                <span>{{ clock }}</span>
              </div>
              <div class="camera-bottom">
                <span>{{ cameraConnected ? `${cameraConfig.resolution} · 实测 ${cameraConfig.fps}fps` : '无视频输入' }}</span>
                <span>{{ cameraConnected ? (cameraLastSync || '机器人视觉在线') : (cameraMessage || '等待接入') }}</span>
              </div>
            </div>
          </div>
          <div class="camera-side">
            <div class="camera-stat">
              <small>画面状态</small>
              <strong>{{ cameraState }}</strong>
            </div>
            <div class="camera-stat">
              <small>看护状态</small>
              <strong>{{ cameraConnected ? '视觉在线' : '未检测' }}</strong>
            </div>
            <div class="camera-stat">
              <small>延迟</small>
              <strong>{{ cameraConnected ? `${cameraConfig.latency}ms` : '-' }}</strong>
            </div>
            <div class="camera-actions">
              <el-button type="primary" size="small" @click="toggleCamera">{{ cameraConnected ? (cameraPaused ? '继续预览' : '暂停预览') : '查询' }}</el-button>
              <el-button size="small" @click="snapshotCamera">抓拍</el-button>
              <el-button size="small" @click="fullscreenCamera">全屏</el-button>
              <el-button size="small" @click="cameraDialogVisible = true">配置</el-button>
            </div>
          </div>
        </div>

        <div ref="sensorPanel" class="panel">
          <div class="panel-head">
            <h2 class="panel-title"><i class="el-icon-monitor" />陪伴感知模组</h2>
            <div class="panel-tools">
              <span class="select-like">小暖机器人主机 A01</span>
              <span class="chip">{{ deviceOnline && boardOnline ? '机器人在线' : '等待机器人上报' }}</span>
              <span class="chip">{{ lastSensorSync || '5秒刷新' }}</span>
              <el-button type="primary" size="small" @click="openSensorDialog">添加感知模组</el-button>
            </div>
          </div>
          <div class="sensor-grid">
            <article v-for="sensor in sensors" :key="sensor.id" class="sensor-card" @click="goDeviceType('sensor')">
              <div class="sensor-top">
                <span class="sensor-name"><span class="sensor-symbol">{{ sensor.symbol }}</span>{{ sensor.name }}</span>
                <div class="sensor-actions">
                  <button v-if="!sensor.connected" type="button" @click.stop="connectSensor(sensor)">接入</button>
                  <button class="sensor-delete" type="button" title="删除感知模组" @click.stop="deleteSensor(sensor.id)">×</button>
                </div>
              </div>
              <div class="sensor-value"><strong>{{ sensor.connected ? sensor.value : '--' }}</strong><span v-if="sensor.connected">{{ sensor.unit }}</span></div>
              <div class="sensor-time">
                <span class="status" :class="sensorStatusClass(sensor)">{{ statusText(sensor) }}</span>
                {{ sensor.connected ? '更新于 ' + sensor.updatedAt : '等待接入' }}
              </div>
              <div class="sensor-meta">
                <span>{{ sensor.metric }}</span>
                <span>{{ sensor.port }}</span>
                <span>{{ sensor.scene }}</span>
              </div>
            </article>
          </div>
        </div>

        <div class="panel chart-panel">
          <div>
            <div class="panel-head">
              <h2 class="panel-title"><i class="el-icon-data-line" />陪伴上下文趋势</h2>
              <div class="panel-tools"><span class="chip">最近交互周期</span><span class="chip">{{ trendMetricLabel }}</span></div>
            </div>
            <div class="chart" :class="{ empty: !hasTrendData }" aria-label="陪伴上下文趋势图">
              <svg v-if="hasTrendData" viewBox="0 0 720 246" preserveAspectRatio="none">
                <polyline :points="envTrendPoints" stroke="#4080ff" stroke-width="4" fill="none" stroke-linecap="round" stroke-linejoin="round" />
                <polygon :points="envTrendFillPoints" fill="rgba(64,128,255,.08)" />
              </svg>
              <div v-else class="chart-empty">暂无机器人感知上报数据</div>
              <div v-if="hasTrendData" class="chart-labels">
                <span v-for="label in envTrendLabels" :key="label">{{ label }}</span>
              </div>
            </div>
          </div>
          <div>
            <div class="panel-head">
              <h2 class="panel-title"><i class="el-icon-bell" />陪伴安全提醒</h2>
            </div>
            <div class="alert-list">
              <div v-if="!alerts.length" class="alert-empty">暂无陪伴安全提醒</div>
              <div
                v-for="alert in alerts"
                :key="alert.id"
                class="alert-item"
                :class="{ danger: alert.level === 'danger', resolved: alert.resolved }"
              >
                <strong>{{ alert.title }}</strong>
                <span>{{ alert.detail }}</span>
                <el-button
                  v-if="!alert.resolved"
                  type="text"
                  size="mini"
                  @click="resolveAlert(alert)"
                >处理</el-button>
                <em v-else>已处理</em>
              </div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <h2 class="panel-title"><i class="el-icon-document" />最近事件</h2>
            <div class="panel-tools"><span class="chip">全部机器人模块</span></div>
          </div>
          <table class="event-table">
            <thead><tr><th>时间</th><th>事件</th><th>来源</th><th>处理状态</th></tr></thead>
            <tbody>
              <tr v-for="event in events" :key="event.time + event.title">
                <td>{{ event.time }}</td>
                <td>{{ event.title }}</td>
                <td>{{ event.source }}</td>
                <td>
                  <span class="status" :class="event.statusClass">{{ event.status }}</span>
                  <el-button
                    v-if="event.statusClass === 'warn' || event.statusClass === 'danger'"
                    type="text"
                    size="mini"
                    @click="resolveEvent(event)"
                  >处理</el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </section>

    <footer class="footer">当前版本：v{{ version }}</footer>

    <el-dialog title="添加机器人感知模组" :visible.sync="sensorDialogVisible" width="520px" append-to-body>
      <el-form label-width="96px" size="small">
        <el-form-item label="模组类型">
          <el-select v-model="sensorForm.type" style="width: 100%" @change="applyPreset">
            <el-option v-for="item in sensorTypes" :key="item.type" :label="item.name" :value="item.type" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示名称"><el-input v-model="sensorForm.name" /></el-form-item>
        <el-form-item label="采集指标"><el-input v-model="sensorForm.metric" /></el-form-item>
        <el-form-item label="当前数值"><el-input v-model="sensorForm.value" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="sensorForm.unit" /></el-form-item>
        <el-form-item label="接入接口"><el-input v-model="sensorForm.port" /></el-form-item>
        <el-form-item label="AI联动能力"><el-input v-model="sensorForm.scene" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="sensorForm.status" style="width: 100%">
            <el-option label="正常" value="normal" />
            <el-option label="需要关注" value="warn" />
            <el-option label="异常" value="danger" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="sensorDialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="saveSensor">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="视觉看护配置" :visible.sync="cameraDialogVisible" width="420px" append-to-body>
      <el-form label-width="90px" size="small">
        <el-form-item label="分辨率">
          <el-select v-model="cameraConfig.resolution" style="width: 100%">
            <el-option label="480×360" value="480×360" />
            <el-option label="640×480" value="640×480" />
            <el-option label="1280×720" value="1280×720" />
            <el-option label="1920×1080" value="1920×1080" />
          </el-select>
        </el-form-item>
        <el-form-item label="帧率">
          <el-input-number v-model="cameraConfig.fps" :min="15" :max="60" />
        </el-form-item>
        <el-form-item label="JPEG质量">
          <el-input-number v-model="cameraConfig.quality" :min="20" :max="95" />
        </el-form-item>
        <el-form-item label="实时流">
          <el-switch v-model="cameraConfig.streamEnabled" />
        </el-form-item>
        <el-form-item label="推理延迟">
          <el-input-number v-model="cameraConfig.latency" :min="20" :max="300" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="cameraDialogVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="sendCameraConfig">保存</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import defaultSettings from '@/settings'
import { chatWithCompanion, configureCompanionCamera, getCompanionCamera, getCompanionSensors, takeCompanionCameraPhoto } from '@/api/companion'
import { listDevice } from '@/api/device/device'

const presets = {
  camera: { type: 'camera', symbol: '📷', name: '高清摄像头', metric: '姿态检测/视觉识别', unit: 'fps', value: '30', port: 'Camera / MIPI-CSI', scene: '姿态检测、桌面状态识别、远程看护' },
  mic: { type: 'mic', symbol: '🎙', name: '麦克风阵列', metric: '语音唤醒/声源定位', unit: 'dB', value: '42', port: 'Audio / USB', scene: '语音交互、噪声分析、主动提醒' },
  temperature: { type: 'temperature', symbol: '🌡', name: '温湿度传感器', metric: '温度/湿度', unit: '°C / %RH', value: '26.4 / 58', port: 'Sensors / I2C', scene: '环境监测、舒适度分析' },
  light: { type: 'light', symbol: '☀', name: '光照传感器', metric: '照度', unit: 'lux', value: '430', port: 'Sensors J1201', scene: '办公照明建议、专注模式联动' },
  lightAdc: { type: 'lightAdc', symbol: '☀', name: '光敏电阻传感器', metric: 'AC电压', unit: 'V', value: '1.5', port: 'LIGHT / ADC1', scene: '亮暗检测、环境光联动' },
  air: { type: 'air', symbol: '🍃', name: '空气质量传感器', metric: 'AQI / TVOC / CO2', unit: 'AQI', value: '46', port: 'Sensors / I2C', scene: '室内环境质量分析、通风提醒' },
  infrared: { type: 'infrared', symbol: '⌁', name: '红外接近传感器', metric: '接近距离', unit: 'cm', value: '35', port: 'GPIO / ADC', scene: '靠近唤醒、桌面交互感知' },
  radar: { type: 'radar', symbol: '◎', name: '毫米波雷达', metric: '人体存在/姿态', unit: 'state', value: '有人', port: 'UART5 / Radar', scene: '存在检测、姿态检测、睡眠/久坐提醒' }
}

const boardSensorMap = {
  1: { preset: 'temperature', field: 'temperature' },
  2: { preset: 'temperature', field: 'humidity' },
  3: { preset: 'light', field: 'light' },
  4: { preset: 'air', field: 'tvoc' },
  5: { preset: 'air', field: 'co2' },
  6: { preset: 'air', field: 'pm25' },
  7: { preset: 'infrared', field: 'proximity' },
  8: { preset: 'radar', field: 'presence' },
  9: { preset: 'radar', field: 'distance' },
  10: { preset: 'radar', field: 'breath' },
  11: { preset: 'radar', field: 'heartbeat' },
  12: { preset: 'lightAdc', field: 'lightAdc' }
}

const chatStorageKey = 'ai-companion-chat-sessions-v1'
const initialAiMessageText = '你好，我是小暖。你可以问我当前陪伴状态、视觉与语音感知、机器人连接情况，或者让我生成一份简短的陪伴简报。'

export default {
  name: 'Home',
  data() {
    return {
      version: defaultSettings.version || '3.9.2',
      greeting: '下午好',
      clock: '',
      timer: null,
      metricsTimer: null,
      boardTimer: null,
      cameraTimer: null,
      boardDid: '000001',
      deviceOnline: false,
      boardOnline: false,
      lastSensorSync: '',
      cameraConnected: false,
      cameraPaused: false,
      cameraState: '未连接',
      cameraDevice: '',
      cameraMessage: '',
      cameraLastSync: '',
      cameraStreamKey: Date.now(),
      cameraPausedFrameKey: Date.now(),
      cameraReconnectTimer: null,
      cameraDialogVisible: false,
      cameraConfig: {
        resolution: '1920×1080',
        fps: 30,
        latency: 82,
        quality: 45,
        streamEnabled: true
      },
      edgeUsage: 72,
      chatCount: 0,
      initialChatCount: 0,
      sensorDialogVisible: false,
      sensorTypes: Object.values(presets),
      sensorForm: {},
      sensors: [],
      environmentHistory: [],
      messages: [],
      currentSessionId: '',
      chatSessions: [],
      chatHistoryVisible: false,
      quickPrompts: ['分析陪伴异常', '生成陪伴简报', '打开安静陪伴', '检查机器人连接'],
      chatInput: '',
      chatLoading: false,
      alerts: [],
      events: [
        {
          time: '01:24:48',
          title: '视觉看护连接成功',
          source: 'Camera / MIPI-CSI',
          status: '已连接',
          statusClass: 'success'
        },
        {
          time: '01:23:15',
          title: '温湿度传感器数据上报',
          source: 'SHT3X / I2C',
          status: '正常',
          statusClass: ''
        },
        {
          time: '01:20:02',
          title: '系统启动完成',
          source: '小暖机器人主机',
          status: '就绪',
          statusClass: 'success'
        }
      ]
    }
  },
  computed: {
    cameraApiBase() {
      const apiBase = process.env.VUE_APP_BASE_API || ''
      return apiBase === '/dev-api'
        ? `${window.location.protocol}//${window.location.hostname}:8080`
        : apiBase
    },
    activeCameraImageUrl() {
      return this.cameraPaused ? this.cameraFrameUrl : this.cameraStreamUrl
    },
    cameraStreamUrl() {
      return `${this.cameraApiBase}/companion/camera/stream?did=${encodeURIComponent(this.boardDid)}&t=${this.cameraStreamKey}`
    },
    cameraFrameUrl() {
      return `${this.cameraApiBase}/companion/camera/frame?did=${encodeURIComponent(this.boardDid)}&t=${this.cameraPausedFrameKey}`
    },
    onlineSensorCount() {
      return this.sensors.filter(item => item.connected && item.status !== 'danger').length
    },
    pendingAlerts() {
      return this.alerts.filter(item => !item.resolved).length +
        this.events.filter(item => item.statusClass === 'warn' || item.statusClass === 'danger').length
    },
    sensorHealthText() {
      if (!this.onlineSensorCount) return '未接入'
      return this.onlineSensorCount === this.sensors.length ? '正常' : '部分接入'
    },
    edgeStateText() {
      if (this.edgeUsage >= 85) return '繁忙'
      if (this.edgeUsage <= 45) return '空闲'
      return '稳定'
    },
    uptimeText() {
      if (!this.deviceOnline) return '--'
      return '在线'
    },
    chatDeltaText() {
      const delta = this.chatCount - this.initialChatCount
      return delta > 0 ? `+${delta}` : '实时'
    },
    hasSensorData() {
      return this.environmentHistory.length > 0 || this.sensors.some(item => item.connected)
    },
    trendMetricLabel() {
      if (!this.hasTrendData) return '无数据'
      return this.environmentHistory[0].type || '真实数据'
    },
    hasTrendData() {
      return this.environmentHistory.length >= 2
    },
    envTrendPoints() {
      const values = this.environmentHistory.slice(0, 20).reverse().map(item => Number(item.value)).filter(item => !Number.isNaN(item))
      if (values.length < 2) return ''
      const min = Math.min(...values)
      const max = Math.max(...values)
      const range = max - min || 1
      return values.map((value, index) => {
        const x = (index / (values.length - 1)) * 700 + 10
        const y = 216 - ((value - min) / range) * 170
        return `${x},${y}`
      }).join(' ')
    },
    envTrendFillPoints() {
      return this.envTrendPoints ? `10,236 ${this.envTrendPoints} 710,236` : ''
    },
    envTrendLabels() {
      const values = this.environmentHistory.slice(0, 5).reverse()
      return values.map(item => item.time)
    }
  },
  created() {
    this.loadSensors()
    this.fetchBoardSensors()
    this.fetchCameraStatus()
    this.tickClock()
    this.timer = setInterval(this.tickClock, 1000)
    this.metricsTimer = setInterval(this.refreshMetrics, 3000)
    this.boardTimer = setInterval(this.fetchBoardSensors, 5000)
    this.cameraTimer = setInterval(this.fetchCameraStatus, 5000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
    clearInterval(this.metricsTimer)
    clearInterval(this.boardTimer)
    clearInterval(this.cameraTimer)
    clearTimeout(this.cameraReconnectTimer)
  },
  methods: {
    setGreeting() {
      const hour = new Date().getHours()
      if (hour < 5) this.greeting = '夜深了'
      else if (hour < 8) this.greeting = '早上好'
      else if (hour < 12) this.greeting = '上午好'
      else if (hour < 14) this.greeting = '中午好'
      else if (hour < 18) this.greeting = '下午好'
      else if (hour < 22) this.greeting = '晚上好'
      else this.greeting = '夜深了'
    },
    tickClock() {
      this.setGreeting()
      this.clock = new Date().toLocaleTimeString('zh-CN', { hour12: false })
    },
    refreshMetrics() {
      const base = this.cameraConnected ? 58 : 42
      const sensorLoad = Math.min(this.onlineSensorCount * 3, 24)
      const alertLoad = this.pendingAlerts * 4
      const wave = Math.round(Math.sin(Date.now() / 7000) * 8)
      this.edgeUsage = Math.max(18, Math.min(96, base + sensorLoad + alertLoad + wave))
    },
    loadSensors() {
      const stored = localStorage.getItem('ai-companion-vue-sensors')
      const savedSensors = stored ? JSON.parse(stored).map(item => ({
        ...item,
        connected: false
      })) : null
      this.sensors = savedSensors || Object.values(presets).map(item => ({
        ...item,
        id: item.type,
        connected: false,
        status: 'normal',
        updatedAt: this.clock || new Date().toLocaleTimeString('zh-CN', { hour12: false })
      }))
    },
    persistSensors() {
      localStorage.setItem('ai-companion-vue-sensors', JSON.stringify(this.sensors))
    },
    fetchDeviceStatus() {
      return listDevice({ did: this.boardDid, pageNum: 1, pageSize: 10 }).then(res => {
        const rows = res.rows || []
        const device = rows.find(item => item.did === this.boardDid) || rows[0]
        this.deviceOnline = !!(device && Number(device.online) === 1)
        if (!this.deviceOnline) {
          this.boardOnline = false
          this.cameraConnected = false
          this.cameraState = '未连接'
          this.cameraLastSync = ''
          this.markBoardSensorsOffline()
        }
        return this.deviceOnline
      }).catch(() => {
        this.deviceOnline = false
        this.boardOnline = false
        this.cameraConnected = false
        this.cameraState = '未连接'
        this.markBoardSensorsOffline()
        return false
      })
    },
    fetchBoardSensors() {
      this.fetchDeviceStatus().then(deviceOnline => {
        if (!deviceOnline) return
        return getCompanionSensors(this.boardDid)
      }).then(res => {
        if (!res) return
        const data = res.data || {}
        const values = data.sensors || []
        this.boardOnline = this.deviceOnline && data.online === true
        if (!this.boardOnline || !values.length) {
          this.markBoardSensorsOffline()
          return
        }
        this.appendEnvironmentHistory(values)
        this.applyBoardSensors(values)
        this.lastSensorSync = '同步于 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false })
        this.refreshMetrics()
      }).catch(() => {
        this.boardOnline = false
        this.markBoardSensorsOffline()
      })
    },
    markBoardSensorsOffline() {
      const boardPresetKeys = Array.from(new Set(Object.values(boardSensorMap).map(item => item.preset)))
      this.sensors.forEach(sensor => {
        if (boardPresetKeys.includes(sensor.type)) {
          sensor.connected = false
        }
      })
      this.persistSensors()
      this.refreshMetrics()
    },
    appendEnvironmentHistory(values) {
      const preferred = values.find(item => ['temperature', 'humidity', 'lightAdc', 'light', 'tvoc', 'co2', 'pm25'].includes(item.type) && item.value !== undefined && item.value !== null)
      if (!preferred) return
      const value = Number(preferred.value)
      if (Number.isNaN(value)) return
      const record = {
        type: this.sensorNameForType(preferred.type),
        value,
        unit: preferred.unit || '',
        time: new Date().toLocaleTimeString('zh-CN', { hour12: false })
      }
      const latest = this.environmentHistory[0]
      if (latest && latest.type === record.type && String(latest.value) === String(record.value)) return
      this.environmentHistory.unshift(record)
      this.environmentHistory = this.environmentHistory.slice(0, 50)
    },
    fetchCameraStatus() {
      if (!this.deviceOnline) {
        this.cameraConnected = false
        this.cameraState = '未连接'
        this.cameraLastSync = ''
        return
      }
      getCompanionCamera(this.boardDid).then(res => {
        const data = res.data || {}
        const connected = data.connected === true
        this.cameraConnected = connected
        this.cameraDevice = data.device || ''
        this.cameraMessage = data.message || ''
        if (connected) {
          this.cameraState = this.cameraPaused ? '画面已暂停' : '实时预览中'
          this.cameraConfig.resolution = data.width && data.height ? `${data.width}×${data.height}` : this.cameraConfig.resolution
          this.cameraConfig.fps = Math.max(1, Math.round(Number(data.fps || this.cameraConfig.fps)))
          this.cameraLastSync = '同步于 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false })
          if (!this.cameraStreamKey) this.cameraStreamKey = Date.now()
        } else {
          this.cameraState = '未连接'
          this.cameraLastSync = ''
        }
        this.refreshMetrics()
      }).catch(() => {
        this.cameraConnected = false
        this.cameraState = '未连接'
        this.cameraMessage = '后台摄像头接口未连接'
      })
    },
    applyBoardSensors(values) {
      const grouped = {}
      values.forEach(item => {
        const map = boardSensorMap[item.type]
        if (!map) return
        if (!grouped[map.preset]) grouped[map.preset] = {}
        grouped[map.preset][map.field] = item
      })

      Object.keys(grouped).forEach(presetKey => {
        const sensor = this.ensurePresetSensor(presetKey)
        const fields = grouped[presetKey]
        sensor.connected = true
        sensor.updatedAt = new Date().toLocaleTimeString('zh-CN', { hour12: false })
        sensor.value = this.formatBoardSensorValue(presetKey, fields)
        sensor.unit = this.formatBoardSensorUnit(presetKey)
        sensor.status = this.deriveBoardSensorStatus(presetKey, fields)
      })
      this.persistSensors()
    },
    ensurePresetSensor(presetKey) {
      let sensor = this.sensors.find(item => item.type === presetKey)
      if (!sensor) {
        sensor = {
          ...presets[presetKey],
          id: presetKey,
          connected: false,
          status: 'normal',
          updatedAt: ''
        }
        this.sensors.push(sensor)
      }
      return sensor
    },
    formatBoardSensorValue(presetKey, fields) {
      const fixed = value => Number(value || 0).toFixed(1).replace(/\.0$/, '')
      if (presetKey === 'temperature') {
        const temp = fields.temperature ? fixed(fields.temperature.value) : '--'
        const humidity = fields.humidity ? fixed(fields.humidity.value) : '--'
        return `${temp} / ${humidity}`
      }
      if (presetKey === 'air') {
        const parts = []
        if (fields.tvoc) parts.push(`TVOC ${fixed(fields.tvoc.value)}`)
        if (fields.co2) parts.push(`CO2 ${fixed(fields.co2.value)}`)
        if (fields.pm25) parts.push(`PM2.5 ${fixed(fields.pm25.value)}`)
        return parts.join(' / ') || '--'
      }
      if (presetKey === 'infrared') return fields.proximity && Number(fields.proximity.value) > 0 ? '靠近' : '未靠近'
      if (presetKey === 'radar') {
        const presence = fields.presence && Number(fields.presence.value) > 0 ? '有人' : '无人'
        const distance = fields.distance ? ` / ${fixed(fields.distance.value)}m` : ''
        return presence + distance
      }
      const first = Object.values(fields)[0]
      return first ? fixed(first.value) : '--'
    },
    formatBoardSensorUnit(presetKey) {
      if (presetKey === 'temperature') return '°C / %RH'
      if (presetKey === 'air') return ''
      if (presetKey === 'infrared' || presetKey === 'radar') return 'state'
      return presets[presetKey].unit
    },
    deriveBoardSensorStatus(presetKey, fields) {
      if (presetKey === 'temperature') {
        const temp = fields.temperature ? Number(fields.temperature.value) : null
        if (temp !== null && (temp < 10 || temp > 35)) return 'warn'
      }
      if (presetKey === 'air') {
        const co2 = fields.co2 ? Number(fields.co2.value) : 0
        const tvoc = fields.tvoc ? Number(fields.tvoc.value) : 0
        if (co2 > 1200 || tvoc > 500) return 'warn'
      }
      return 'normal'
    },
    statusText(sensor) {
      if (!sensor.connected) return '未接入'
      return sensor.status === 'danger' ? '异常' : sensor.status === 'warn' ? '关注' : '正常'
    },
    sensorStatusClass(sensor) {
      if (!sensor.connected) return 'offline'
      return sensor.status === 'normal' ? '' : sensor.status
    },
    sensorNameForType(type) {
      const map = {
        temperature: '温度',
        humidity: '湿度',
        light: '光照',
        lightAdc: '光敏电阻',
        tvoc: 'TVOC',
        co2: 'CO2',
        pm25: 'PM2.5',
        proximity: '红外接近',
        presence: '人体存在',
        distance: '目标距离'
      }
      return map[type] || type || '感知模组'
    },
    openSensorDialog() {
      this.sensorDialogVisible = true
      this.sensorForm = { ...presets.camera, status: 'normal' }
    },
    applyPreset(type) {
      this.sensorForm = { ...presets[type], status: this.sensorForm.status || 'normal' }
    },
    saveSensor() {
      if (!this.sensorForm.name || !this.sensorForm.value) {
        this.$message.warning('请填写感知模组名称和当前数值')
        return
      }
      this.sensors.push({
        ...this.sensorForm,
        id: `sensor-${Date.now()}`,
        connected: false,
        updatedAt: new Date().toLocaleTimeString('zh-CN', { hour12: false })
      })
      this.unshiftEvent('新增感知模组：' + this.sensorForm.name, '感知模组管理', '已记录', '')
      this.refreshMetrics()
      this.persistSensors()
      this.sensorDialogVisible = false
      this.$message.success('感知模组已添加')
    },
    connectSensor(sensor) {
      if (!this.deviceOnline || !this.boardOnline) {
        this.$message.warning('机器人离线，不能手动标记感知模组在线')
        return
      }
      sensor.connected = true
      sensor.updatedAt = new Date().toLocaleTimeString('zh-CN', { hour12: false })
      this.persistSensors()
      this.unshiftEvent('感知模组已接入：' + sensor.name, '感知模组管理', '已连接', '')
      this.refreshMetrics()
      this.$message.success(sensor.name + ' 已接入')
    },
    deleteSensor(id) {
      const target = this.sensors.find(item => item.id === id)
      this.sensors = this.sensors.filter(item => item.id !== id)
      if (target) this.unshiftEvent('删除感知模组：' + target.name, '感知模组管理', '已记录', '')
      this.refreshMetrics()
      this.persistSensors()
      this.$message.success('感知模组已删除')
    },
    toggleCamera() {
      if (!this.cameraConnected) {
        this.fetchCameraStatus()
        return
      }
      this.cameraPaused = !this.cameraPaused
      clearTimeout(this.cameraReconnectTimer)
      if (this.cameraPaused) {
        this.cameraPausedFrameKey = Date.now()
        this.sendCameraStreamSwitch(false)
      } else {
        this.cameraStreamKey = Date.now()
        this.sendCameraStreamSwitch(true)
      }
      this.cameraState = this.cameraPaused ? '画面已暂停' : '实时预览中'
    },
    connectCamera() {
      this.fetchCameraStatus()
    },
    goDeviceType(view) {
      if (view === 'sensor') {
        this.$router.push({ path: '/iot/sensor', query: { did: this.boardDid } })
        return
      }
      this.$router.push({ path: '/iot/deviceType', query: { view } })
    },
    onCameraStreamLoad() {
      clearTimeout(this.cameraReconnectTimer)
      this.cameraState = this.cameraPaused ? '画面已暂停' : '实时监控中'
    },
    onCameraStreamError() {
      if (this.cameraPaused) return
      clearTimeout(this.cameraReconnectTimer)
      this.cameraState = '视频流重连中'
      this.cameraReconnectTimer = setTimeout(() => {
        this.cameraStreamKey = Date.now()
      }, 1000)
    },
    sendCameraStreamSwitch(streamEnabled) {
      const [width, height] = this.parseCameraResolution()
      configureCompanionCamera(this.boardDid, {
        width,
        height,
        fps: Number(this.cameraConfig.fps) || 30,
        quality: Number(this.cameraConfig.quality) || 45,
        streamEnabled
      }).catch(() => {
        this.$message.warning(streamEnabled ? '视频流恢复失败' : '视频流暂停失败')
      })
    },
    parseCameraResolution() {
      const normalized = String(this.cameraConfig.resolution || '1920x1080')
        .replace(/×/g, 'x')
        .replace(/脳/g, 'x')
      const [width, height] = normalized.split('x').map(item => Number(item) || 0)
      return [width || 1920, height || 1080]
    },
    snapshotCamera() {
      if (!this.cameraConnected) {
        this.$message.warning('请先接入视觉看护模组')
        return
      }
      takeCompanionCameraPhoto(this.boardDid).then(res => {
        const photo = res.photo || (res.data && res.data.photo) || {}
        const path = photo.path || ''
        const ok = photo.success === true || !!path
        this.cameraState = ok ? '抓拍已保存' : '抓拍未保存'
        this.unshiftEvent(ok ? '视觉看护抓拍已保存到后台' : '视觉看护抓拍未保存', '视觉感知', path || '暂无实时帧', '')
        if (ok) {
          this.cameraLastSync = path
          this.$message.success(path ? `抓拍已保存：${path}` : '抓拍已保存')
        } else {
          this.$message.warning(res.msg || '暂无实时帧，抓拍未保存')
        }
        setTimeout(this.fetchCameraStatus, 1200)
      }).catch(() => {
        this.$message.error('抓拍失败，请检查后端和视觉看护状态')
      })
    },
    fullscreenCamera() {
      if (!this.cameraConnected) {
        this.$message.warning('请先接入视觉看护模组')
        return
      }
      const el = this.$refs.cameraStage
      const requestFullscreen = el && (el.requestFullscreen || el.webkitRequestFullscreen || el.mozRequestFullScreen || el.msRequestFullscreen)
      if (!requestFullscreen) {
        this.$message.info('当前浏览器不支持全屏接口')
        return
      }
      Promise.resolve(requestFullscreen.call(el)).catch(() => {
        this.$message.warning('全屏打开失败，请检查浏览器权限')
      })
    },
    sendCameraConfig() {
      const [width, height] = this.parseCameraResolution()
      configureCompanionCamera(this.boardDid, {
        width,
        height,
        fps: Number(this.cameraConfig.fps) || 30,
        quality: Number(this.cameraConfig.quality) || 45,
        streamEnabled: this.cameraConfig.streamEnabled !== false
      }).then(() => {
        this.cameraDialogVisible = false
        this.cameraStreamKey = Date.now()
        this.$message.success('配置已下发到机器人')
        setTimeout(this.fetchCameraStatus, 1200)
      })
    },
    saveCameraConfig() {
      this.cameraDialogVisible = false
      if (!this.cameraConnected) this.connectCamera()
      this.$message.success('视觉看护配置已保存')
    },
    scrollToPanel(refName) {
      const el = this.$refs[refName]
      if (el && el.scrollIntoView) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    generateReport() {
      this.chatInput = '请根据当前机器人感知模组、视觉看护、语音交互和安全提醒，生成一份简短的陪伴简报，并给出建议。'
      this.sendMessage()
    },
    resolveEvent(event) {
      event.status = '已处理'
      event.statusClass = ''
      this.unshiftEvent('告警已处理：' + event.title, '告警中心', '已记录', '')
      this.refreshMetrics()
      this.$message.success('告警已处理')
    },
    resolveAlert(alert) {
      alert.resolved = true
      this.unshiftEvent('告警已处理：' + alert.title, '告警中心', '已处理', '')
      this.refreshMetrics()
      this.$message.success('告警已处理')
    },
    unshiftEvent(title, source, status, statusClass) {
      this.events.unshift({
        time: this.clock || new Date().toLocaleTimeString('zh-CN', { hour12: false }),
        title,
        source,
        status,
        statusClass
      })
      this.events = this.events.slice(0, 8)
    },
    sendQuick(prompt) {
      this.chatInput = prompt
      this.sendMessage()
    },
    sendMessage() {
      const text = this.chatInput.trim()
      if (!text || this.chatLoading) return
      this.addMessage('user', text)
      this.chatInput = ''
      this.chatCount += 1
      this.chatLoading = true
      chatWithCompanion({
        did: this.boardDid,
        message: text
      }).then(res => {
        this.addMessage('ai', res.answer || res.msg || 'AI没有返回内容')
      }).catch(error => {
        const fallback = this.replyFor(text)
        const message = error && error.msg ? error.msg : 'AI云端暂时不可用'
        this.addMessage('ai', `${message}，先用本地规则回答：${fallback}`)
      }).finally(() => {
        this.chatLoading = false
      })
    },
    addMessage(role, text) {
      this.messages.push({
        id: Date.now() + Math.random(),
        role,
        text,
        time: new Date().toLocaleString('zh-CN', { hour12: false })
      })
      this.persistChatSessions()
      this.$nextTick(() => {
        const body = this.$refs.chatBody
        if (body) body.scrollTop = body.scrollHeight
      })
    },
    loadChatSessions() {
      let sessions = []
      try {
        sessions = JSON.parse(localStorage.getItem(chatStorageKey) || '[]')
      } catch (e) {
        sessions = []
      }
      this.chatSessions = Array.isArray(sessions) ? sessions : []
      if (this.chatSessions.length) {
        const latest = this.chatSessions[0]
        this.currentSessionId = latest.id
        this.messages = latest.messages || this.initialMessages()
      } else {
        this.createChatSession(false)
      }
      this.updateTodayChatCount()
    },
    initialMessages() {
      return [{
        id: Date.now(),
        role: 'ai',
        text: initialAiMessageText,
        time: new Date().toLocaleString('zh-CN', { hour12: false })
      }]
    },
    createChatSession(showMessage = true) {
      this.persistChatSessions()
      const now = new Date().toLocaleString('zh-CN', { hour12: false })
      const session = {
        id: 'chat-' + Date.now(),
        title: '新会话 ' + new Date().toLocaleTimeString('zh-CN', { hour12: false }),
        createdAt: now,
        updatedAt: now,
        messages: this.initialMessages()
      }
      this.chatSessions.unshift(session)
      this.chatSessions = this.chatSessions.slice(0, 50)
      this.currentSessionId = session.id
      this.messages = session.messages
      this.saveChatSessions()
      this.updateTodayChatCount()
      if (showMessage) this.$message.success('已新建会话')
    },
    openChatHistory() {
      this.persistChatSessions()
      this.chatHistoryVisible = true
    },
    switchChatSession(session) {
      this.persistChatSessions()
      this.currentSessionId = session.id
      this.messages = session.messages || this.initialMessages()
      this.chatHistoryVisible = false
      this.updateTodayChatCount()
      this.$nextTick(() => {
        const body = this.$refs.chatBody
        if (body) body.scrollTop = body.scrollHeight
      })
    },
    deleteChatSession(session) {
      this.chatSessions = this.chatSessions.filter(item => item.id !== session.id)
      if (session.id === this.currentSessionId) {
        if (this.chatSessions.length) {
          this.currentSessionId = this.chatSessions[0].id
          this.messages = this.chatSessions[0].messages || this.initialMessages()
        } else {
          this.currentSessionId = ''
          this.messages = []
          this.createChatSession(false)
        }
      }
      this.saveChatSessions()
      this.updateTodayChatCount()
    },
    persistChatSessions() {
      if (!this.currentSessionId) return
      const now = new Date().toLocaleString('zh-CN', { hour12: false })
      const session = this.chatSessions.find(item => item.id === this.currentSessionId)
      if (!session) return
      session.messages = this.messages
      session.updatedAt = now
      const firstUserMessage = this.messages.find(item => item.role === 'user')
      if (firstUserMessage && firstUserMessage.text) {
        session.title = firstUserMessage.text.length > 18 ? firstUserMessage.text.slice(0, 18) + '...' : firstUserMessage.text
      }
      this.chatSessions = [
        session,
        ...this.chatSessions.filter(item => item.id !== session.id)
      ].slice(0, 50)
      this.saveChatSessions()
    },
    saveChatSessions() {
      localStorage.setItem(chatStorageKey, JSON.stringify(this.chatSessions))
    },
    updateTodayChatCount() {
      const today = new Date().toLocaleDateString('zh-CN')
      this.chatCount = this.chatSessions.reduce((count, session) => {
        return count + (session.messages || []).filter(item => {
          return item.role === 'user' && item.time && item.time.startsWith(today)
        }).length
      }, 0)
      this.initialChatCount = 0
    },
    replyFor(text) {
      const summary = this.currentEnvironmentSummary()
      if (text.includes('连接')) {
        return `机器人连接检查：主控板${this.boardOnline ? '在线' : '离线'}，视觉看护${this.cameraConnected ? '在线' : '未连接'}，在线感知模组 ${this.onlineSensorCount}/${this.sensors.length}。`
      }
      if (!summary) {
        return '当前没有收到机器人感知模组的真实上报数据，我不能给出陪伴环境、视觉或语音交互结论。请先确认机器人采集程序已启动并上报。'
      }
      if (text.includes('异常')) return `基于当前真实上报：${summary}。暂未生成自动陪伴异常结论，如需阈值可以在感知模组管理里继续配置。`
      if (text.includes('简报') || text.includes('日报')) return `今日陪伴记录基于当前会话上报：${summary}。历史数据不足时，简报只展示已收到的真实数据。`
      if (text.includes('安静')) return '安静陪伴需要噪声或麦克风数据支撑；当前若没有语音交互阵列上报，我不会判断噪声偏高。'
      return `当前机器人感知数据：${summary}。`
    },
    currentEnvironmentSummary() {
      const connected = this.sensors.filter(item => item.connected && item.value !== '--')
      if (!connected.length) return ''
      return connected.map(item => `${item.name} ${item.value}${item.unit && item.unit !== 'state' ? item.unit : ''}`).join('，')
    }
  }
}
</script>

<style lang="scss" scoped>
.companion-home {
  min-height: calc(100vh - 136px);
  padding: 22px 24px 28px;
  background: linear-gradient(180deg, #eef4ff 0, #f6f8fc 260px, #f5f7fb 100%);
  color: #1f2d3d;
}

.hero {
  min-height: 112px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border-radius: 10px;
  color: #fff;
  overflow: hidden;
  position: relative;
  background:
    radial-gradient(circle at 86% 24%, rgba(53, 224, 190, .28), transparent 25%),
    radial-gradient(circle at 52% 0%, rgba(255, 255, 255, .24), transparent 28%),
    linear-gradient(135deg, #3677f5 0%, #5c97ff 54%, #18bfc0 100%);
  box-shadow: 0 18px 38px rgba(64, 128, 255, .22);
}

.hero::after {
  content: "";
  position: absolute;
  inset: auto -50px -90px auto;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  border: 48px solid rgba(255, 255, 255, .09);
}

.hero > * {
  position: relative;
  z-index: 1;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: 26px;
}

.hero p {
  margin: 0;
  color: rgba(255, 255, 255, .88);
}

.hero-actions {
  margin-top: 14px;
}

.hero-btn {
  color: #fff;
  background: rgba(255, 255, 255, .18);
  border-color: rgba(255, 255, 255, .3);
}

.ai-state {
  width: 280px;
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, .15);
  backdrop-filter: blur(6px);
}

.ai-state-top {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;
}

.companion-face {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: #fff;
  color: #4080ff;
  font-size: 22px;
}

.ai-state b {
  display: block;
  margin-bottom: 3px;
  font-size: 16px;
}

.ai-state span {
  color: rgba(255, 255, 255, .82);
  font-size: 12px;
}

.voice-bar {
  height: 34px;
  display: flex;
  align-items: flex-end;
  gap: 6px;
}

.voice-bar i {
  width: 7px;
  border-radius: 4px 4px 0 0;
  background: rgba(255, 255, 255, .88);
}

.voice-bar i:nth-child(1) { height: 13px; }
.voice-bar i:nth-child(2) { height: 22px; }
.voice-bar i:nth-child(3) { height: 17px; }
.voice-bar i:nth-child(4) { height: 28px; }
.voice-bar i:nth-child(5) { height: 15px; }
.voice-bar i:nth-child(6) { height: 24px; }

.metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.metric-card,
.panel {
  background: #fff;
  border: 1px solid rgba(226, 234, 246, .9);
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(38, 57, 77, .08);
}

.metric-card {
  min-height: 82px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 15px 16px;
  transition: transform .18s ease, box-shadow .18s ease;
}

.metric-card:hover,
.sensor-card:hover,
.camera-stat:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(38, 57, 77, .11);
}

.metric-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #fff;
  font-size: 22px;
}

.metric-icon.blue { background: linear-gradient(135deg, #4080ff, #6ba3ff); }
.metric-icon.green { background: linear-gradient(135deg, #2fb86f, #5fd695); }
.metric-icon.orange { background: linear-gradient(135deg, #f59e0b, #f7bf4f); }
.metric-icon.cyan { background: linear-gradient(135deg, #13b7bd, #20d2bd); }
.metric-icon.purple { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }

.metric-card small {
  display: block;
  margin-bottom: 5px;
  color: #7d8ba0;
}

.metric-card strong {
  font-size: 22px;
  line-height: 1;
}

.metric-card em {
  margin-left: 6px;
  color: #2fb86f;
  font-size: 12px;
  font-style: normal;
}

.warn-text {
  color: #f59e0b !important;
}

.workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 18px;
  margin-top: 18px;
  align-items: start;
}

.left-stack {
  display: grid;
  gap: 18px;
}

.panel {
  padding: 18px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 17px;
}

.panel-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7d8ba0;
  font-size: 13px;
}

.select-like,
.chip {
  height: 28px;
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  border: 1px solid #e7edf6;
  border-radius: 5px;
  background: #f6f8fc;
  color: #53677d;
}

.camera-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 230px;
  gap: 16px;
}

.camera-stage {
  min-height: 300px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, #101c2f 0%, #182a45 52%, #0e1a2c 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .06), 0 16px 28px rgba(20, 32, 51, .18);
}

.camera-stage.paused {
  filter: grayscale(.35);
}

.camera-stage.offline {
  background:
    linear-gradient(135deg, rgba(16, 28, 47, .96), rgba(20, 42, 68, .96)),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, .04) 0 1px, transparent 1px 90px),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, .04) 0 1px, transparent 1px 68px);
}

.camera-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  padding: 28px;
  color: rgba(255, 255, 255, .86);
  text-align: center;
}

.camera-empty i {
  width: 54px;
  height: 54px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #9fb7d8;
  background: rgba(255, 255, 255, .08);
  font-size: 28px;
}

.camera-empty strong {
  font-size: 17px;
}

.camera-empty span {
  max-width: 360px;
  color: rgba(255, 255, 255, .62);
  font-size: 13px;
}

.camera-feed {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 58% 35%, rgba(255, 255, 255, .2), transparent 8%),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, .04) 0 1px, transparent 1px 90px),
    repeating-linear-gradient(0deg, rgba(255, 255, 255, .04) 0 1px, transparent 1px 68px);
}

.camera-feed::before {
  content: "";
  position: absolute;
  left: 50%;
  top: 48%;
  width: 128px;
  height: 188px;
  transform: translate(-50%, -50%);
  border-radius: 60px 60px 34px 34px;
  background: linear-gradient(180deg, rgba(255, 255, 255, .26), rgba(255, 255, 255, .08));
  box-shadow: 0 0 42px rgba(64, 128, 255, .25);
}

.camera-feed::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 31%;
  width: 76px;
  height: 76px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: rgba(255, 255, 255, .28);
  border: 2px solid rgba(255, 255, 255, .4);
}

.camera-stream {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  background: #101d2e;
}

.camera-live-badge {
  position: absolute;
  left: 14px;
  top: 42px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  border-radius: 8px;
  color: #dce8f7;
  background: rgba(15, 28, 45, .74);
  font-size: 12px;
}

.camera-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.detect-box {
  position: absolute;
  left: 42%;
  top: 18%;
  width: 18%;
  height: 50%;
  border: 2px solid #35e0be;
  border-radius: 8px;
  box-shadow: 0 0 18px rgba(53, 224, 190, .24);
}

.detect-box span {
  position: absolute;
  left: -2px;
  top: -27px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 5px 5px 0 0;
  color: #082033;
  background: #35e0be;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.pose-line {
  position: absolute;
  height: 2px;
  background: #ffd166;
  transform-origin: left center;
  box-shadow: 0 0 10px rgba(255, 209, 102, .5);
}

.pose-line.l1 { left: 49%; top: 35%; width: 70px; transform: rotate(33deg); }
.pose-line.l2 { left: 49%; top: 35%; width: 66px; transform: rotate(147deg); }
.pose-line.l3 { left: 50%; top: 44%; width: 72px; transform: rotate(75deg); }
.pose-line.l4 { left: 50%; top: 44%; width: 72px; transform: rotate(105deg); }

.camera-hud,
.camera-bottom {
  position: absolute;
  left: 14px;
  right: 14px;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  color: rgba(255, 255, 255, .9);
  font-size: 12px;
}

.camera-hud {
  top: 14px;
}

.camera-bottom {
  bottom: 14px;
}

.rec-dot {
  width: 8px;
  height: 8px;
  display: inline-block;
  margin-right: 6px;
  border-radius: 50%;
  background: #f05b5b;
  box-shadow: 0 0 0 4px rgba(240, 91, 91, .2);
}

.rec-dot.muted {
  background: #9fb0c7;
  box-shadow: 0 0 0 4px rgba(159, 176, 199, .18);
}

.camera-side {
  display: grid;
  gap: 12px;
}

.camera-stat {
  padding: 12px;
  border: 1px solid #edf2fa;
  border-radius: 8px;
  background: #fbfcff;
  transition: transform .18s ease, box-shadow .18s ease;
}

.camera-stat small {
  display: block;
  margin-bottom: 6px;
  color: #7d8ba0;
}

.camera-stat strong {
  font-size: 18px;
}

.camera-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.sensor-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  max-height: 430px;
  overflow: auto;
  padding-right: 4px;
}

.sensor-grid::-webkit-scrollbar {
  width: 8px;
}

.sensor-grid::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background: #cbd7e8;
}

.sensor-card {
  min-height: 142px;
  padding: 14px;
  border: 1px solid #edf2fa;
  border-radius: 8px;
  background: linear-gradient(180deg, #fff, #fbfcff);
  transition: transform .18s ease, box-shadow .18s ease;
  cursor: pointer;
}

.sensor-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}

.sensor-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5f75;
  font-weight: 600;
}

.sensor-symbol {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  background: #edf4ff;
}

.sensor-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sensor-actions button {
  height: 24px;
  padding: 0 8px;
  border: 0;
  border-radius: 5px;
  color: #4080ff;
  background: #eef4ff;
  cursor: pointer;
  font-size: 12px;
}

.sensor-actions button:hover {
  background: #dfeaff;
}

.sensor-delete {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 5px;
  color: #98a6b8;
  background: transparent;
  cursor: pointer;
}

.sensor-delete:hover {
  color: #f05b5b;
  background: #fff0f0;
}

.sensor-value {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  margin-bottom: 10px;
}

.sensor-value strong {
  font-size: 26px;
  line-height: 1;
}

.sensor-value span,
.sensor-time {
  color: #7d8ba0;
  font-size: 12px;
}

.status {
  height: 22px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 11px;
  color: #2fb86f;
  background: #eaf8f1;
  font-size: 12px;
}

.status.warn {
  color: #f59e0b;
  background: #fff6e5;
}

.status.danger {
  color: #f05b5b;
  background: #fff0f0;
}

.status.offline {
  color: #7d8ba0;
  background: #eef2f7;
}

.sensor-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 52px;
  margin-top: 12px;
  overflow: hidden;
}

.sensor-meta span {
  height: 22px;
  display: inline-flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 11px;
  color: #53677d;
  background: #eef3fa;
  font-size: 12px;
}

.chart-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 240px;
  gap: 22px;
}

.chart {
  height: 218px;
  position: relative;
  border-radius: 8px;
  background:
    linear-gradient(#eef3fa 1px, transparent 1px) 0 0 / 100% 49px,
    linear-gradient(90deg, #eef3fa 1px, transparent 1px) 0 0 / 80px 100%,
    #fbfcff;
  overflow: hidden;
}

.chart svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.chart.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-empty {
  color: #8492a6;
  font-size: 14px;
}

.chart-labels {
  position: absolute;
  inset: auto 12px 10px 12px;
  display: flex;
  justify-content: space-between;
  color: #98a6b8;
  font-size: 12px;
}

.alert-list {
  display: grid;
  gap: 10px;
}

.alert-empty {
  min-height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dce5f1;
  border-radius: 8px;
  color: #8492a6;
  background: #fbfcff;
  font-size: 13px;
}

.alert-item {
  padding: 12px;
  border-left: 3px solid #f59e0b;
  border-radius: 8px;
  background: #fbfcff;
}

.alert-item.danger {
  border-left-color: #f05b5b;
}

.alert-item.resolved {
  border-left-color: #2ac77a;
  background: #f7fbf9;
}

.alert-item strong {
  display: block;
  margin-bottom: 5px;
  font-size: 13px;
}

.alert-item span {
  color: #7d8ba0;
  font-size: 12px;
}

.alert-item em {
  display: inline-block;
  margin-left: 8px;
  color: #2ac77a;
  font-size: 12px;
  font-style: normal;
}

.event-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.event-table th,
.event-table td {
  height: 38px;
  border-bottom: 1px dashed #e7edf6;
  color: #53677d;
  text-align: left;
}

.event-table th {
  color: #8492a6;
  font-weight: 500;
}

.chat-panel {
  padding: 0;
  overflow: hidden;
  position: sticky;
  top: 104px;
}

.chat-head {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  border-bottom: 1px solid #e7edf6;
  background: linear-gradient(180deg, #fff, #f7faff);
}

.chat-person {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-face {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #4080ff, #18bfc0);
  font-size: 22px;
}

.chat-person strong {
  display: block;
  margin-bottom: 4px;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.online {
  color: #2fb86f;
  font-size: 12px;
}

.chat-body {
  height: 396px;
  padding: 18px;
  overflow: auto;
  background: #fff;
}

.message {
  display: flex;
  margin-bottom: 16px;
}

.message.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 282px;
  padding: 11px 13px;
  border-radius: 8px;
  background: #f4f7fb;
  color: #42566c;
  line-height: 1.6;
  box-shadow: 0 8px 18px rgba(38, 57, 77, .07);
}

.message.user .bubble {
  color: #fff;
  background: linear-gradient(135deg, #4080ff, #5e9bff);
}

.chat-footer {
  padding: 12px 14px 14px;
  border-top: 1px solid #e7edf6;
  background: #fbfcff;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.prompt {
  height: 28px;
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  border: 1px solid #d8e7ff;
  border-radius: 14px;
  color: #4080ff;
  background: #edf4ff;
  font-size: 12px;
  cursor: pointer;
}

.prompt:hover {
  background: #e3efff;
}

.chat-input {
  display: flex;
  gap: 10px;
}

.history-list {
  display: grid;
  gap: 10px;
  max-height: 420px;
  overflow: auto;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e7edf6;
  border-radius: 8px;
  background: #fbfcff;
  cursor: pointer;
}

.history-item.active {
  border-color: #4080ff;
  background: #edf4ff;
}

.history-item strong {
  display: block;
  margin-bottom: 4px;
  color: #31445b;
}

.history-item span,
.history-empty {
  color: #8492a6;
  font-size: 12px;
}

.history-empty {
  padding: 32px 0;
  text-align: center;
}

.footer {
  padding: 22px 0 4px;
  color: #7d8ba0;
  font-size: 13px;
  text-align: center;
}

@media (max-width: 1320px) {
  .ai-state {
    display: none;
  }

  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workspace,
  .camera-panel,
  .chart-panel {
    grid-template-columns: 1fr;
  }

  .chat-panel {
    position: static;
  }

  .chat-body {
    height: 330px;
  }
}

@media (max-width: 768px) {
  .companion-home {
    padding: 16px;
  }

  .metrics {
    grid-template-columns: 1fr;
  }

  .panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .sensor-grid {
    grid-template-columns: 1fr;
  }
}

.companion-home {
  min-height: calc(100vh - 84px);
  padding: 20px 24px 28px;
  background:
    linear-gradient(180deg, var(--companion-bg-glow, rgba(214, 248, 252, .82)) 0, transparent 280px),
    var(--companion-bg, #eef8fa) !important;
  color: var(--companion-text, #0d1f24);
}

.hero {
  min-height: 150px;
  padding: 30px 32px;
  border: 1px solid var(--companion-hero-border, rgba(0, 184, 207, .22));
  border-radius: 8px;
  color: #f8fdff;
  background:
    repeating-linear-gradient(45deg, var(--companion-hero-line, rgba(0, 218, 243, .16)) 0 1px, transparent 1px 12px),
    linear-gradient(90deg, var(--companion-hero-start, #14d9e8) 0%, var(--companion-hero-end, #effdff) 100%);
  box-shadow: none;
}

.hero::after {
  display: none;
}

.hero h1 {
  margin-bottom: 12px;
  color: #f8fdff;
  font-size: 28px;
  line-height: 1.25;
  letter-spacing: 0;
  text-shadow: 0 2px 12px rgba(0, 0, 0, .28);
}

.hero p {
  max-width: 780px;
  color: rgba(238, 247, 255, .9);
  font-size: 15px;
  line-height: 1.7;
  text-shadow: 0 1px 8px rgba(0, 0, 0, .2);
}

.ai-state {
  width: 316px;
  padding: 22px;
  border: 1px solid var(--companion-line, #d6e4e7);
  border-radius: 8px;
  background: var(--companion-card-glass, rgba(255, 255, 255, .72));
  backdrop-filter: blur(12px);
}

.companion-face,
.chat-face {
  color: var(--companion-primary, #00788a);
  background: var(--companion-primary-soft, #d8f1f4);
}

.ai-state b,
.ai-state span {
  color: var(--companion-text, #0d1f24);
}

.ai-state span {
  opacity: .78;
}

.voice-bar {
  height: 22px;
  gap: 7px;
}

.voice-bar i {
  width: 5px;
  background: var(--companion-primary, #00788a);
}

.metrics {
  gap: 18px;
  margin-top: 24px;
}

.metric-card,
.panel {
  border: 1px solid var(--companion-line, #d6e4e7);
  border-radius: 8px;
  background: var(--companion-card, #ffffff);
  box-shadow: 0 1px 3px var(--companion-shadow, rgba(6, 35, 42, .12));
}

.metric-card {
  min-height: 102px;
  padding: 20px 22px;
}

.metric-card:hover,
.sensor-card:hover,
.camera-stat:hover {
  transform: none;
  box-shadow: 0 3px 10px var(--companion-shadow, rgba(6, 35, 42, .12));
}

.metric-icon {
  width: 58px;
  height: 58px;
  border-radius: 8px;
  color: var(--companion-primary, #00788a);
  background: var(--companion-primary-soft, #d8f1f4);
  border: 1px solid var(--companion-primary-line, #9adce5);
}

.metric-icon.blue { color: #008ca0; background: #d7f8fc; border-color: #9eeaf1; }
.metric-icon.green { color: #059669; background: #d9fbe8; border-color: #9be7bc; }
.metric-icon.orange { color: #f97316; background: #fff0db; border-color: #fdc988; }
.metric-icon.cyan { color: #2563eb; background: #dbeafe; border-color: #b7d2ff; }

.metric-card small,
.panel-tools,
.camera-stat small,
.sensor-value span,
.sensor-time,
.chart-empty,
.history-item span,
.history-empty,
.event-table th,
.footer {
  color: var(--companion-muted, #60747a);
}

.event-table td {
  border-bottom-color: var(--companion-line, #d6e4e7);
  color: var(--companion-text, #0d1f24);
}

.event-table .status.warn {
  color: #f59e0b;
}

.event-table .status.danger {
  color: #ef4444;
}

.event-table .status.success {
  color: #10b981;
}

.metric-card strong,
.camera-stat strong,
.sensor-value strong,
.panel-title,
.chat-person strong,
.history-item strong {
  color: var(--companion-text, #0d1f24);
}

.metric-card em,
.online {
  color: var(--companion-success, #16a34a);
}

.workspace {
  grid-template-columns: minmax(0, 1fr);
  gap: 20px;
  margin-top: 26px;
}

.left-stack {
  gap: 20px;
}

.panel {
  padding: 20px;
}

.panel-head {
  min-height: 32px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
}

.select-like,
.chip {
  height: 30px;
  border-color: var(--companion-line, #d6e4e7);
  border-radius: 5px;
  background: var(--companion-chip, #e7eef1);
  color: var(--companion-text, #0d1f24);
}

.camera-panel {
  grid-template-columns: minmax(0, 1fr) 250px;
  gap: 20px;
}

.camera-stage {
  min-height: 566px;
  border: 1px solid var(--companion-line, #d6e4e7);
  border-radius: 8px;
  background: var(--companion-video, #eef8fa);
  box-shadow: none;
}

.camera-stage.offline {
  background: var(--companion-video, #eef8fa);
}

.camera-empty {
  color: #f8fdff;
}

.camera-empty i {
  color: rgba(238, 247, 255, .88);
  background: rgba(238, 247, 255, .12);
}

.camera-empty span {
  color: rgba(238, 247, 255, .78);
}

.camera-hud,
.camera-bottom {
  color: var(--companion-muted, #60747a);
}

.camera-stat,
.sensor-card,
.alert-empty,
.history-item {
  border-color: var(--companion-line, #d6e4e7);
  background: var(--companion-subtle, #f1fbfd);
}

.camera-stat {
  min-height: 82px;
}

.camera-actions {
  margin-top: auto;
}

.camera-actions ::v-deep .el-button,
.send-button {
  margin-left: 0;
}

.chart-panel,
.footer {
  display: none;
}

.chat-panel {
  top: 90px;
  min-height: 708px;
  box-shadow: none;
}

.chat-head {
  height: 84px;
  padding: 0 20px;
  border-color: var(--companion-line, #d6e4e7);
  background: var(--companion-card, #fff);
}

.chat-body {
  height: 446px;
  padding: 22px 20px;
  background: var(--companion-card, #fff);
}

.bubble {
  max-width: 316px;
  border: 1px solid var(--companion-line, #d6e4e7);
  border-radius: 6px;
  background: var(--companion-subtle, #f1fbfd);
  color: var(--companion-text, #0d1f24);
  box-shadow: none;
}

.message.user .bubble {
  color: var(--companion-on-primary, #fff);
  background: var(--companion-primary, #00788a);
}

.chat-footer {
  min-height: 176px;
  padding: 20px;
  border-color: var(--companion-line, #d6e4e7);
  background: var(--companion-card, #fff);
}

.prompt {
  height: 28px;
  border-color: var(--companion-primary-line, #9adce5);
  color: var(--companion-primary, #00788a);
  background: transparent;
}

.chat-input {
  position: relative;
  gap: 0;
}

.chat-input ::v-deep .el-input__inner {
  height: 48px;
  padding-right: 54px;
  border-color: var(--companion-line, #d6e4e7);
  border-radius: 8px;
  background: var(--companion-subtle, #f1fbfd);
  color: var(--companion-text, #0d1f24);
}

.send-button {
  position: absolute;
  right: 10px;
  top: 9px;
  width: 30px;
  height: 30px;
  padding: 0;
  border-radius: 50%;
}

@media (max-width: 1320px) {
  .workspace,
  .camera-panel {
    grid-template-columns: 1fr;
  }

  .camera-stage {
    min-height: 420px;
  }

  .chat-panel {
    min-height: auto;
  }
}
</style>
