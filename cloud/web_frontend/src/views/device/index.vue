<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="设备标识" prop="did">
        <el-input
          v-model="queryParams.did"
          placeholder="请输入设备标识"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备类型" prop="deviceTypeId">
        <el-select v-model="queryParams.deviceTypeId" placeholder="全部" clearable filterable style="width: 200px">
          <el-option v-for="item in deviceTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="在线状态" prop="online">
        <el-select v-model="queryParams.online" placeholder="全部" clearable style="width: 120px">
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['device:info:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['device:info:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['device:info:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备标识" align="center" prop="did" :show-overflow-tooltip="true" />
      <el-table-column label="设备名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="设备类型" align="center" prop="typeName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="在线状态" align="center" prop="online" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.online === 1 ? 'success' : 'info'" size="mini">
            {{ scope.row.online === 1 ? "在线" : "离线" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="IP" align="center" prop="ip" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="端口" align="center" prop="port" width="90" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" min-width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-setting"
            :disabled="scope.row.online !== 1"
            @click="handlePwm(scope.row)"
            v-hasPermi="['device:info:pwm']"
          >PWM设置</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['device:info:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['device:info:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="设备标识" prop="did">
          <el-input v-model="form.did" placeholder="请输入设备标识" />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceTypeId">
          <el-select v-model="form.deviceTypeId" placeholder="请选择设备类型" filterable style="width: 100%">
            <el-option v-for="item in deviceTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="在线状态">
          <el-tag :type="form.online === 1 ? 'success' : 'info'" size="mini">
            {{ form.online === 1 ? "在线" : "离线" }}
          </el-tag>
        </el-form-item>
        <el-form-item label="IP">
          <el-input :value="form.ip || '-'" disabled />
        </el-form-item>
        <el-form-item label="端口">
          <el-input :value="form.port !== undefined && form.port !== null ? form.port : '-'" disabled />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="扩展内容/描述" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="PWM 设置（RGB）" :visible.sync="pwmOpen" width="520px" append-to-body @close="pwmDialogClosed">
      <el-form ref="pwmFormRef" :model="pwmForm" label-width="100px">
        <el-form-item label="设备">
          <span>{{ pwmForm.name }}（{{ pwmForm.did }}）</span>
        </el-form-item>
        <el-form-item label="红色 占空比">
          <el-slider v-model="pwmForm.pwmLamp1" :min="0" :max="255" :step="1" show-input />
          <span class="pwm-hint">0～255（255 为最亮）</span>
        </el-form-item>
        <el-form-item label="绿色 占空比">
          <el-slider v-model="pwmForm.pwmLamp2" :min="0" :max="255" :step="1" show-input />
        </el-form-item>
        <el-form-item label="蓝色 占空比">
          <el-slider v-model="pwmForm.pwmLamp3" :min="0" :max="255" :step="1" show-input />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPwm">下 发</el-button>
        <el-button @click="pwmOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDevice, getDevice, delDevice, addDevice, updateDevice, setDevicePwm } from "@/api/device/device"
import { listDeviceType } from "@/api/device/deviceType"

export default {
  name: "Device",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      deviceList: [],
      deviceTypeOptions: [],
      title: "",
      open: false,
      pwmOpen: false,
      pwmForm: {
        did: "",
        name: "",
        pwmLamp1: 0,
        pwmLamp2: 0,
        pwmLamp3: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        did: undefined,
        name: undefined,
        deviceTypeId: undefined,
        online: undefined
      },
      form: {},
      rules: {
        did: [
          { required: true, message: "设备标识不能为空", trigger: "blur" }
        ],
        name: [
          { required: true, message: "设备名称不能为空", trigger: "blur" }
        ],
        deviceTypeId: [
          { required: true, message: "设备类型不能为空", trigger: "change" }
        ],
        
      }
    }
  },
  created() {
    this.loadDeviceTypeOptions()
    this.getList()
  },
  methods: {
    loadDeviceTypeOptions() {
      listDeviceType({ pageNum: 1, pageSize: 500 }).then(response => {
        this.deviceTypeOptions = response.rows || []
      })
    },
    getList() {
      this.loading = true
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        did: undefined,
        name: undefined,
        deviceTypeId: undefined,
        online: 0,
        ip: undefined,
        port: undefined,
        content: undefined,
        remark: undefined
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加设备信息"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getDevice(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改设备信息"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const payload = { ...this.form }
          // 页面层不允许修改在线状态、IP、端口
          delete payload.online
          delete payload.ip
          delete payload.port
          if (this.form.id != undefined) {
            updateDevice(payload).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDevice(payload).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除设备编号为"' + ids + '"的数据项？').then(function() {
        return delDevice(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handlePwm(row) {
      if (!row || row.online !== 1) {
        this.$modal.msgWarning("仅在线设备可设置 PWM")
        return
      }
      this.pwmForm = {
        did: row.did,
        name: row.name || row.did,
        pwmLamp1: 0,
        pwmLamp2: 0,
        pwmLamp3: 0
      }
      this.pwmOpen = true
    },
    pwmDialogClosed() {
      this.pwmForm = {
        did: "",
        name: "",
        pwmLamp1: 0,
        pwmLamp2: 0,
        pwmLamp3: 0
      }
    },
    submitPwm() {
      const payload = {
        did: this.pwmForm.did,
        pwmLamp1: this.pwmForm.pwmLamp1,
        pwmLamp2: this.pwmForm.pwmLamp2,
        pwmLamp3: this.pwmForm.pwmLamp3
      }
      setDevicePwm(payload).then(() => {
        this.$modal.msgSuccess("指令已下发")
        this.pwmOpen = false
        this.getList()
      })
    }
  }
}
</script>

<style scoped>
.pwm-hint {
  display: block;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
