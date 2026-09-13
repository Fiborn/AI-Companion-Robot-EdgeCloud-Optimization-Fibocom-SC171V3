<template>
  <div class="app-container sensor-manage-dark">
    <template v-if="detailType">
      <div class="detail-header-card">
        <el-button class="btn-back" icon="el-icon-arrow-left" size="small" @click="backToList">返回传感器管理</el-button>
        <div class="detail-header-right">
          <span class="did-text">DID: {{ companionDid }}</span>
          <span class="status-badge" :class="{ online: selectedCurrent.online }">
            <span class="status-dot"></span>
            {{ selectedCurrent.online ? '数据在线' : '数据离线' }}
          </span>
        </div>
      </div>

      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">实时数据</span>
              <div class="kpi-icon kpi-icon-green">
                <i class="el-icon-connection"></i>
              </div>
            </div>
            <div class="kpi-value">
              {{ selectedCurrent.value }} <span class="kpi-unit">{{ selectedCurrent.unit }}</span>
            </div>
            <div class="kpi-sub">
              <span class="sub-dot" :class="{ active: selectedCurrent.online }"></span>
              {{ selectedCurrent.online ? '实时上报中' : '暂无实时上报' }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">连接状态</span>
              <div class="kpi-icon kpi-icon-blue">
                <i class="el-icon-share"></i>
              </div>
            </div>
            <div class="kpi-value status-text" :class="{ online: boardOnline }">
              {{ boardOnline ? '在线' : '未接入' }}
            </div>
            <div class="kpi-sub text-tertiary">
              {{ boardOnline ? '板端已连接' : '板端未连接' }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">更新时间</span>
              <div class="kpi-icon kpi-icon-amber">
                <i class="el-icon-time"></i>
              </div>
            </div>
            <div class="kpi-value">
              {{ selectedCurrent.updatedAt }}
            </div>
            <div class="kpi-sub text-tertiary">
              最近 {{ detailHistory.length }} 条记录
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="panel-card">
        <div class="panel-header">
          <div class="panel-title-group">
            <h3 class="panel-title">{{ sensorName(detailType) }}趋势</h3>
            <span class="panel-tag">{{ detailHistory.length }}条记录</span>
          </div>
          <el-button class="btn-primary" size="small" icon="el-icon-refresh" @click="refreshSensors">刷新数据</el-button>
        </div>
        <div class="trend-wrapper">
          <div v-if="detailHistory.length >= 2" ref="trendChart" class="trend-chart"></div>
          <div v-else class="empty-state">
            <i class="el-icon-data-line"></i>
            <span>暂无足够真实数据生成趋势</span>
          </div>
        </div>
      </div>

      <div class="panel-card">
        <div class="panel-header">
          <h3 class="panel-title">历史数据</h3>
          <span class="panel-subtle">仅记录板端实际上报的数据</span>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>时间</th>
                <th>类型</th>
                <th>数值</th>
                <th>来源</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in detailHistory" :key="index">
                <td class="mono">{{ item.time }}</td>
                <td>
                  <span class="type-tag">{{ item.type }}</span>
                </td>
                <td class="mono">{{ item.value }} {{ item.unit }}</td>
                <td class="mono source-text">{{ item.source }}</td>
              </tr>
              <tr v-if="!detailHistory.length" class="empty-row">
                <td colspan="4">
                  <div class="empty-state small">
                    <i class="el-icon-document"></i>
                    <span>暂无历史数据</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <template v-else>
      <el-row :gutter="16" class="kpi-row">
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">板端连接</span>
              <div class="kpi-icon kpi-icon-blue">
                <i class="el-icon-connection"></i>
              </div>
            </div>
            <div class="kpi-value status-text" :class="{ online: boardOnline }">
              {{ boardOnline ? '已连接' : '未连接' }}
            </div>
            <div class="kpi-sub text-tertiary">
              {{ boardOnline ? boardAddress : companionDid }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">在线传感器</span>
              <div class="kpi-icon kpi-icon-green">
                <i class="el-icon-monitor"></i>
              </div>
            </div>
            <div class="kpi-value">
              {{ onlineCount }} <span class="kpi-unit">/ {{ sensorRows.length }}</span>
            </div>
            <div class="kpi-sub">
              <span class="sub-dot" :class="{ active: onlineCount > 0 }"></span>
              {{ onlineCount ? '实时上报中' : '等待板端上报' }}
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="kpi-card">
            <div class="kpi-card-top">
              <span class="kpi-label">最近更新</span>
              <div class="kpi-icon kpi-icon-amber">
                <i class="el-icon-time"></i>
              </div>
            </div>
            <div class="kpi-value">
              {{ latestUpdate }}
            </div>
            <div class="kpi-sub text-tertiary">
              {{ autoRefresh ? '5秒自动刷新' : '手动刷新' }}
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="panel-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <div class="input-wrapper">
              <i class="el-icon-search input-icon"></i>
              <input
                v-model="companionDid"
                class="dark-input"
                placeholder="请输入设备 DID"
                @keyup.enter="refreshSensors"
              />
            </div>
          </div>
          <div class="toolbar-right">
            <el-button class="btn-primary" icon="el-icon-plus" @click="openAddDialog">新增传感器</el-button>
            <el-button class="btn-ghost" icon="el-icon-refresh" @click="refreshSensors">刷新</el-button>
            <el-button
              :class="autoRefresh ? 'btn-success' : 'btn-ghost'"
              icon="el-icon-timer"
              @click="toggleAutoRefresh"
            >
              {{ autoRefresh ? '关闭自动刷新' : '开启自动刷新' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="panel-card table-panel">
        <div v-loading="loading" class="table-wrapper">
          <table class="data-table sensor-list-table">
            <thead>
              <tr>
                <th>传感器</th>
                <th>类型</th>
                <th>实时数值</th>
                <th style="text-align:center;">状态</th>
                <th>更新时间</th>
                <th style="text-align:center;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in sensorRows" :key="row.id">
                <td>
                  <span class="sensor-name-cell">
                    <span class="sensor-icon-dot">
                      <i :class="sensorIcon(row.type)"></i>
                    </span>
                    {{ row.name }}
                  </span>
                </td>
                <td>
                  <span class="type-tag">{{ row.type }}</span>
                </td>
                <td class="mono">
                  <span class="value-text">{{ row.value }}</span>
                  <span class="unit-text">{{ row.unit }}</span>
                </td>
                <td style="text-align:center;">
                  <span class="status-tag" :class="{ online: row.online }">
                    <span class="status-dot-sm"></span>
                    {{ row.online ? '在线' : '未接入' }}
                  </span>
                </td>
                <td class="mono">{{ row.updatedAt }}</td>
                <td style="text-align:center;">
                  <button class="link-btn" @click="goSensorDetail(row)">
                    <i class="el-icon-view"></i> 查看数据
                  </button>
                  <button class="link-btn danger" @click="deleteSensor(row)">
                    <i class="el-icon-delete"></i> 删除
                  </button>
                </td>
              </tr>
              <tr v-if="!sensorRows.length" class="empty-row">
                <td colspan="6">
                  <div class="empty-state small">
                    <i class="el-icon-s-data"></i>
                    <span>暂无传感器数据</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <el-dialog title="新增传感器" :visible.sync="dialogVisible" width="520px" append-to-body custom-class="dark-dialog">
        <el-form ref="sensorForm" :model="sensorForm" :rules="rules" label-width="96px">
          <el-form-item label="传感器名称" prop="name">
            <el-input v-model="sensorForm.name" placeholder="例如：温湿度传感器" />
          </el-form-item>
          <el-form-item label="传感器类型" prop="type">
            <el-select v-model="sensorForm.type" filterable allow-create default-first-option placeholder="请选择或输入类型">
              <el-option label="温度" value="temperature" />
              <el-option label="湿度" value="humidity" />
              <el-option label="光照" value="light" />
              <el-option label="TVOC" value="tvoc" />
              <el-option label="CO2" value="co2" />
              <el-option label="PM2.5" value="pm25" />
              <el-option label="红外接近" value="proximity" />
              <el-option label="毫米波雷达" value="radar" />
            </el-select>
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="sensorForm.unit" placeholder="例如：°C、%RH、lux、ppm" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addSensor">确 定</el-button>
        </div>
      </el-dialog>
    </template>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCompanionSensors } from "@/api/companion"
import { listDevice } from "@/api/device/device"

const DEFAULT_SENSORS = [
  { name: "温度传感器", type: "temperature", unit: "°C" },
  { name: "湿度传感器", type: "humidity", unit: "%RH" },
  { name: "光照传感器", type: "light", unit: "lux" },
  { name: "光敏电阻传感器", type: "lightAdc", unit: "V" },
  { name: "空气质量传感器", type: "tvoc", unit: "ppb" },
  { name: "二氧化碳传感器", type: "co2", unit: "ppm" },
  { name: "红外接近传感器", type: "proximity", unit: "cm" },
  { name: "毫米波雷达", type: "radar", unit: "" }
]

const BOARD_SENSOR_TYPE_MAP = {
  1: "temperature",
  2: "humidity",
  3: "light",
  4: "tvoc",
  5: "co2",
  6: "pm25",
  7: "proximity",
  8: "presence",
  9: "distance",
  10: "breath",
  11: "heartbeat",
  12: "lightAdc"
}

const STORAGE_KEY = "ai-companion-sensor-manage-list"
const HIDDEN_KEY = "ai-companion-sensor-manage-hidden"
const HISTORY_KEY = "ai-companion-sensor-history"

export default {
  name: "SensorManage",
  data() {
    return {
      companionDid: "000001",
      loading: false,
      autoRefresh: true,
      timer: null,
      boardOnline: false,
      boardInfo: null,
      configuredSensors: [],
      hiddenTypes: [],
      sensorHistory: {},
      dialogVisible: false,
      sensorForm: {
        name: "",
        type: "",
        unit: ""
      },
      rules: {
        name: [{ required: true, message: "请输入传感器名称", trigger: "blur" }],
        type: [{ required: true, message: "请选择传感器类型", trigger: "change" }]
      },
      snapshot: {
        online: false,
        sensors: []
      },
      trendChart: null,
      resizeTimer: null
    }
  },
  computed: {
    sensorRows() {
      const normalizedReports = (this.snapshot.sensors || []).map(item => this.normalizeReportedSensor(item))
      const reportedMap = new Map(normalizedReports.map(item => [item.type, item]))
      const configuredTypes = new Set(this.configuredSensors.map(item => this.normalizeSensorType(item.type)))
      const hiddenSet = new Set(this.hiddenTypes)
      const rows = this.configuredSensors.filter(item => !hiddenSet.has(item.type)).map(item => {
        const reported = reportedMap.get(this.normalizeSensorType(item.type))
        return this.toSensorRow(item, reported)
      })
      normalizedReports.forEach(item => {
        if (!configuredTypes.has(item.type) && !hiddenSet.has(item.type)) {
          rows.push(this.toSensorRow({
            id: `reported-${item.type}`,
            name: this.sensorName(item.type),
            type: item.type,
            unit: item.unit || ""
          }, item))
        }
      })
      return rows
    },
    onlineCount() {
      return this.sensorRows.filter(item => item.online).length
    },
    latestUpdate() {
      const updated = this.sensorRows.find(item => item.updatedAt && item.updatedAt !== "-")
      return updated ? updated.updatedAt : "-"
    },
    selectedCurrent() {
      if (!this.detailType) {
        return { name: "", value: "--", unit: "", online: false, updatedAt: "-" }
      }
      const type = this.normalizeSensorType(this.detailType)
      return this.sensorRows.find(item => item.type === type) || {
        name: this.sensorName(type),
        type,
        value: "--",
        unit: "",
        online: false,
        updatedAt: "-"
      }
    },
    detailType() {
      return this.$route.query.type || ""
    },
    detailHistory() {
      if (!this.detailType) return []
      return this.sensorHistory[this.normalizeSensorType(this.detailType)] || []
    },
    boardAddress() {
      if (!this.boardInfo) return this.companionDid
      return [this.boardInfo.ip, this.boardInfo.port].filter(Boolean).join(":") || this.companionDid
    },
    chartLabels() {
      return this.detailHistory.map(item => item.time).slice(0, 50).reverse()
    },
    chartData() {
      return this.detailHistory.map(item => Number(item.value)).filter(v => Number.isFinite(v)).slice(0, 50).reverse()
    }
  },
  watch: {
    detailType() {
      if (this.trendChart) {
        this.trendChart.dispose()
        this.trendChart = null
      }
      this.loadSensorHistory()
      this.$nextTick(() => {
        this.$nextTick(() => {
          this.initTrendChart()
        })
      })
    },
    detailHistory: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          this.$nextTick(() => {
            if (!this.trendChart) {
              this.initTrendChart()
            } else {
              this.updateTrendChart()
            }
          })
        })
      }
    }
  },
  created() {
    if (this.$route.query.did) {
      this.companionDid = this.$route.query.did
    }
    this.loadConfiguredSensors()
    this.loadSensorHistory()
    this.refreshSensors()
    this.startAutoRefresh()
  },
  mounted() {
    this.$nextTick(() => {
      this.$nextTick(() => {
        this.initTrendChart()
      })
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeDestroy() {
    clearInterval(this.timer)
    clearTimeout(this.resizeTimer)
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) {
      this.trendChart.dispose()
      this.trendChart = null
    }
  },
  methods: {
    initTrendChart() {
      if (!this.$refs.trendChart) return
      if (this.trendChart) {
        this.trendChart.dispose()
        this.trendChart = null
      }
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.updateTrendChart()
    },
    updateTrendChart() {
      if (!this.trendChart || !this.detailHistory.length) return
      const unit = this.selectedCurrent.unit || ''
      const name = this.sensorName(this.detailType)
      const labels = this.chartLabels
      const data = this.chartData

      const option = {
        grid: {
          left: 50,
          right: 20,
          top: 20,
          bottom: 30,
          containLabel: false
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#1a2332',
          borderColor: 'rgba(148, 163, 184, 0.12)',
          borderWidth: 1,
          textStyle: {
            color: '#f1f5f9',
            fontSize: 13
          },
          axisPointer: {
            type: 'line',
            lineStyle: {
              color: 'rgba(59, 130, 246, 0.3)'
            }
          },
          formatter: function(params) {
            const p = params[0]
            return '<div style="padding: 4px 2px;">' +
              '<div style="color:#94a3b8;font-size:12px;margin-bottom:6px;">' + p.axisValue + '</div>' +
              '<div style="display:flex;align-items:center;gap:8px;">' +
                '<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#3b82f6;"></span>' +
                '<span style="font-weight:600;font-family:Consolas,monospace;">' + Number(p.value).toFixed(2) + ' ' + unit + '</span>' +
              '</div>' +
            '</div>'
          }
        },
        xAxis: {
          type: 'category',
          data: labels,
          boundaryGap: false,
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#64748b',
            fontSize: 11,
            fontFamily: 'Consolas, monospace',
            maxTicksLimit: 8
          },
          splitLine: {
            show: false
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#64748b',
            fontSize: 11,
            fontFamily: 'Consolas, monospace',
            formatter: function(value) {
              return value.toFixed(2)
            }
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(148, 163, 184, 0.06)',
              type: 'solid'
            }
          }
        },
        series: [{
          name: name,
          type: 'line',
          data: data,
          smooth: true,
          symbol: 'circle',
          symbolSize: 0,
          showSymbol: false,
          lineStyle: {
            width: 2.5,
            color: '#3b82f6'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.25)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
            ])
          },
          emphasis: {
            focus: 'series',
            itemStyle: {
              color: '#3b82f6',
              borderColor: '#f1f5f9',
              borderWidth: 2
            },
            symbolSize: 6
          }
        }]
      }

      this.trendChart.setOption(option, true)
    },
    handleResize() {
      clearTimeout(this.resizeTimer)
      this.resizeTimer = setTimeout(() => {
        if (this.trendChart) {
          this.trendChart.resize()
        }
      }, 100)
    },
    loadConfiguredSensors() {
      const hidden = localStorage.getItem(HIDDEN_KEY)
      try {
        this.hiddenTypes = hidden ? JSON.parse(hidden) : []
      } catch (e) {
        this.hiddenTypes = []
        localStorage.removeItem(HIDDEN_KEY)
      }
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        try {
          this.configuredSensors = this.mergeDefaultSensors(JSON.parse(stored))
          this.persistConfiguredSensors()
          return
        } catch (e) {
          localStorage.removeItem(STORAGE_KEY)
        }
      }
      this.configuredSensors = DEFAULT_SENSORS.map((item, index) => ({
        id: `default-${index}-${item.type}`,
        ...item
      }))
      this.persistConfiguredSensors()
    },
    persistConfiguredSensors() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.configuredSensors))
    },
    mergeDefaultSensors(sensors) {
      const list = Array.isArray(sensors) ? sensors : []
      const existingTypes = new Set(list.map(item => this.normalizeSensorType(item.type)))
      DEFAULT_SENSORS.forEach((item, index) => {
        if (!existingTypes.has(item.type)) {
          list.push({
            id: `default-${index}-${item.type}`,
            ...item
          })
          existingTypes.add(item.type)
        }
      })
      return list
    },
    persistHiddenTypes() {
      localStorage.setItem(HIDDEN_KEY, JSON.stringify(this.hiddenTypes))
    },
    loadSensorHistory() {
      const stored = localStorage.getItem(HISTORY_KEY)
      if (!stored) return
      try {
        this.sensorHistory = JSON.parse(stored) || {}
      } catch (e) {
        this.sensorHistory = {}
        localStorage.removeItem(HISTORY_KEY)
      }
    },
    persistSensorHistory() {
      localStorage.setItem(HISTORY_KEY, JSON.stringify(this.sensorHistory))
    },
    toSensorRow(config, reported) {
      const hasReport = Boolean(reported)
      const type = this.normalizeSensorType(config.type)
      return {
        id: config.id,
        name: config.name || this.sensorName(type),
        type: type || "-",
        value: hasReport && reported.value !== undefined && reported.value !== null ? reported.value : "--",
        unit: hasReport ? (reported.unit || config.unit || "") : (config.unit || ""),
        online: this.snapshot.online === true && hasReport,
        updatedAt: hasReport && reported.time ? this.parseTime(reported.time) : (hasReport ? this.nowText() : "-")
      }
    },
    refreshSensors() {
      this.loading = true
      this.refreshBoardStatus()
      return getCompanionSensors(this.companionDid || "000001").then(res => {
        this.snapshot = res.data || { online: false, sensors: [] }
        this.appendSensorHistory()
      }).finally(() => {
        this.loading = false
      })
    },
    refreshBoardStatus() {
      listDevice({
        pageNum: 1,
        pageSize: 1,
        did: this.companionDid || "000001"
      }).then(res => {
        const row = (res.rows || []).find(item => item.did === (this.companionDid || "000001")) || (res.rows || [])[0]
        this.boardInfo = row || null
        this.boardOnline = Boolean(row && row.online === 1)
      }).catch(() => {
        this.boardInfo = null
        this.boardOnline = false
      })
    },
    appendSensorHistory() {
      if (this.snapshot.online !== true) return
      ;(this.snapshot.sensors || []).forEach(item => {
        if (!item || !item.type || item.value === undefined || item.value === null) return
        const type = this.normalizeSensorType(item.type)
        const record = {
          time: item.time ? this.parseTime(item.time) : this.nowText(),
          type,
          value: item.value,
          unit: item.unit || "",
          source: this.companionDid || "000001"
        }
        const list = this.sensorHistory[type] ? [...this.sensorHistory[type]] : []
        const latest = list[0]
        if (latest && latest.time === record.time && String(latest.value) === String(record.value)) return
        list.unshift(record)
        this.$set(this.sensorHistory, type, list.slice(0, 50))
      })
      this.persistSensorHistory()
    },
    goSensorDetail(row) {
      if (row.online && row.value !== "--" && !(this.sensorHistory[row.type] || []).length) {
        this.$set(this.sensorHistory, row.type, [{
          time: row.updatedAt,
          type: row.type,
          value: row.value,
          unit: row.unit,
          source: this.companionDid || "000001"
        }])
        this.persistSensorHistory()
      }
      this.$router.push({ path: this.$route.path, query: { did: this.companionDid, type: row.type } })
    },
    backToList() {
      this.$router.push({ path: this.$route.path, query: { did: this.companionDid } })
    },
    openAddDialog() {
      this.sensorForm = {
        name: "",
        type: "",
        unit: ""
      }
      this.dialogVisible = true
      this.$nextTick(() => this.resetForm("sensorForm"))
    },
    addSensor() {
      this.$refs["sensorForm"].validate(valid => {
        if (!valid) return
        const type = String(this.sensorForm.type || "").trim()
        if (this.configuredSensors.some(item => item.type === type)) {
          this.$modal.msgWarning("该类型传感器已存在")
          return
        }
        this.hiddenTypes = this.hiddenTypes.filter(item => item !== type)
        this.persistHiddenTypes()
        this.configuredSensors.push({
          id: `custom-${Date.now()}`,
          name: this.sensorForm.name.trim(),
          type,
          unit: this.sensorForm.unit.trim()
        })
        this.persistConfiguredSensors()
        this.dialogVisible = false
        this.$modal.msgSuccess("新增成功")
      })
    },
    deleteSensor(row) {
      this.$modal.confirm(`是否确认删除传感器"${row.name}"？`).then(() => {
        this.configuredSensors = this.configuredSensors.filter(item => item.id !== row.id)
        if (!this.hiddenTypes.includes(row.type)) {
          this.hiddenTypes.push(row.type)
        }
        this.persistConfiguredSensors()
        this.persistHiddenTypes()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    toggleAutoRefresh() {
      this.autoRefresh = !this.autoRefresh
      if (this.autoRefresh) {
        this.startAutoRefresh()
      } else {
        clearInterval(this.timer)
      }
    },
    startAutoRefresh() {
      clearInterval(this.timer)
      this.timer = setInterval(this.refreshSensors, 5000)
    },
    nowText() {
      return new Date().toLocaleTimeString("zh-CN", { hour12: false })
    },
    normalizeSensorType(type) {
      const key = String(type || "")
      return BOARD_SENSOR_TYPE_MAP[key] || key
    },
    normalizeReportedSensor(item) {
      return {
        ...item,
        type: this.normalizeSensorType(item.type)
      }
    },
    sensorName(type) {
      type = this.normalizeSensorType(type)
      const map = {
        temperature: "温度",
        humidity: "湿度",
        light: "光照",
        lightAdc: "光敏电阻电压",
        tvoc: "TVOC",
        co2: "CO2",
        pm25: "PM2.5",
        proximity: "红外接近",
        presence: "人体存在",
        distance: "目标距离",
        radar: "毫米波雷达"
      }
      return map[type] || type || "传感器"
    },
    sensorIcon(type) {
      type = this.normalizeSensorType(type)
      if (/temperature|humidity/.test(type)) return "el-icon-odometer"
      if (/light/.test(type)) return "el-icon-sunny"
      if (/air|tvoc|co2|pm25/.test(type)) return "el-icon-cloudy"
      if (/proximity|distance|presence|radar/.test(type)) return "el-icon-aim"
      return "el-icon-monitor"
    }
  }
}
</script>

<style scoped>
.sensor-manage-dark {
  background: #0b1120;
  min-height: calc(100vh - 84px);
  padding: 24px;
  font-family: "Inter", "Noto Sans SC", "PingFang SC", "Microsoft YaHei", system-ui, -apple-system, sans-serif;
}

.mono {
  font-family: "Consolas", "Monaco", "Courier New", monospace;
  font-variant-numeric: tabular-nums;
}

.detail-header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #1a2332;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  margin-bottom: 24px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #14b8a6;
  background: transparent;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  transition: opacity 0.15s ease;
}

.btn-back:hover {
  opacity: 0.8;
}

.detail-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.did-text {
  font-size: 13px;
  color: #64748b;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 12px;
  border-radius: 4px;
  background: rgba(239, 68, 68, 0.12);
  color: #ef4444;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.online {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 9999px;
  background: currentColor;
}

.kpi-row {
  margin-bottom: 24px;
}

.kpi-card {
  padding: 20px;
  background: #1a2332;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  transition: border-color 0.2s ease;
}

.kpi-card:hover {
  border-color: rgba(148, 163, 184, 0.25);
}

.kpi-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.kpi-label {
  font-size: 13px;
  color: #94a3b8;
}

.kpi-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.kpi-icon-green {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.kpi-icon-blue {
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
}

.kpi-icon-amber {
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}

.kpi-value {
  font-family: "Consolas", "Monaco", "Courier New", monospace;
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  font-variant-numeric: tabular-nums;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.kpi-value .kpi-unit {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 400;
  margin-left: 4px;
}

.kpi-value.status-text {
  font-size: 24px;
  color: #ef4444;
}

.kpi-value.status-text.online {
  color: #22c55e;
}

.kpi-sub {
  margin-top: 8px;
  font-size: 12px;
  color: #22c55e;
  display: flex;
  align-items: center;
  gap: 6px;
}

.kpi-sub.text-tertiary {
  color: #64748b;
}

.sub-dot {
  width: 5px;
  height: 5px;
  border-radius: 9999px;
  background: #64748b;
}

.sub-dot.active {
  background: #22c55e;
}

.panel-card {
  background: #1a2332;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.panel-card.table-panel {
  padding: 0;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.panel-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #f1f5f9;
}

.panel-tag {
  font-size: 12px;
  color: #64748b;
  padding: 2px 10px;
  border-radius: 4px;
  background: #162032;
}

.panel-subtle {
  font-size: 12px;
  color: #64748b;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 8px;
  background: #14b8a6;
  color: #0f172a;
  font-size: 13px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: background 0.15s ease;
}

.btn-primary:hover {
  background: #0d9488;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 8px;
  background: transparent;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(148, 163, 184, 0.2);
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-ghost:hover {
  border-color: rgba(148, 163, 184, 0.4);
  color: #f1f5f9;
}

.btn-success {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 8px;
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(34, 197, 94, 0.3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-success:hover {
  background: rgba(34, 197, 94, 0.25);
}

.trend-wrapper {
  position: relative;
  width: 100%;
}

.trend-chart {
  width: 100%;
  height: 300px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #64748b;
  gap: 12px;
}

.empty-state i {
  font-size: 32px;
  opacity: 0.5;
}

.empty-state.small i {
  font-size: 24px;
}

.empty-state.small {
  padding: 40px 0;
  height: auto;
}

.table-wrapper {
  overflow-x: auto;
}

.table-wrapper::-webkit-scrollbar {
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: #162032;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.2);
  border-radius: 3px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.data-table thead tr {
  background: #162032;
}

.data-table th {
  padding: 12px 20px;
  text-align: left;
  font-size: 11px;
  font-weight: 500;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  white-space: nowrap;
}

.data-table td {
  padding: 12px 20px;
  font-size: 13px;
  color: #f1f5f9;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

.data-table tbody tr {
  transition: background 0.15s ease;
}

.data-table tbody tr:hover {
  background: #1f2b3d;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 4px;
  background: rgba(59, 130, 246, 0.12);
  color: #3b82f6;
  font-size: 12px;
  font-weight: 500;
}

.source-text {
  color: #94a3b8;
}

.empty-row td {
  padding: 0;
}

.value-text {
  font-size: 15px;
  font-weight: 600;
  color: #f1f5f9;
}

.unit-text {
  margin-left: 5px;
  color: #94a3b8;
  font-size: 12px;
}

.sensor-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.sensor-icon-dot {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: rgba(20, 184, 166, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #14b8a6;
  font-size: 14px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 12px;
  border-radius: 4px;
  background: rgba(100, 116, 139, 0.15);
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.online {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.status-dot-sm {
  width: 5px;
  height: 5px;
  border-radius: 9999px;
  background: currentColor;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: none;
  color: #14b8a6;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  transition: opacity 0.15s ease;
}

.link-btn:hover {
  opacity: 0.8;
}

.link-btn.danger {
  color: #ef4444;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  font-size: 14px;
  z-index: 1;
}

.dark-input {
  width: 240px;
  height: 36px;
  padding: 0 12px 0 36px;
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 8px;
  color: #f1f5f9;
  font-size: 13px;
  outline: none;
  transition: border-color 0.15s ease;
}

.dark-input::placeholder {
  color: #475569;
}

.dark-input:focus {
  border-color: rgba(20, 184, 166, 0.4);
}

@media (max-width: 768px) {
  .sensor-manage-dark {
    padding: 16px;
  }

  .detail-header-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-right {
    flex-wrap: wrap;
  }

  .dark-input {
    width: 100%;
  }

  .kpi-value {
    font-size: 22px;
  }

  .trend-chart {
    height: 240px;
  }
}
</style>
