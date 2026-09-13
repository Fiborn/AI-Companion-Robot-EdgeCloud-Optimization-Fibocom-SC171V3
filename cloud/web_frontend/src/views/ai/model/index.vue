<template>
  <div class="agent-page">
    <div class="agent-shell">
      <div class="agent-toolbar">
        <div class="agent-breadcrumb">
          <span>控制台</span>
          <i class="el-icon-arrow-right"></i>
          <strong>智能体</strong>
        </div>
        <div class="toolbar-actions">
          <el-button class="add-device-btn" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['ai:model:add']">
            添加设备
          </el-button>
          <el-dropdown trigger="click" @command="handleToolbarCommand">
            <el-button class="drop-btn">
              <i class="el-icon-arrow-down"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="refresh" icon="el-icon-refresh">刷新列表</el-dropdown-item>
              <el-dropdown-item command="search" icon="el-icon-search">筛选配置</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
        </div>
      </div>

      <el-collapse-transition>
        <el-form
          v-show="showSearch"
          :model="queryParams"
          ref="queryForm"
          size="small"
          :inline="true"
          class="agent-filter"
          label-width="78px"
        >
          <el-form-item label="配置名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入配置名称" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="供应商" prop="provider">
            <el-select v-model="queryParams.provider" placeholder="全部" clearable style="width: 150px">
              <el-option label="OpenAI" value="openai" />
              <el-option label="DeepSeek" value="deepseek" />
              <el-option label="Qwen" value="qwen" />
              <el-option label="本地模型" value="local" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型名" prop="modelName">
            <el-input v-model="queryParams.modelName" placeholder="请输入模型名" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="enabled">
            <el-select v-model="queryParams.enabled" placeholder="全部" clearable style="width: 110px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-transition>

      <div v-loading="loading" class="agent-board">
        <el-empty v-if="!loading && modelList.length === 0" description="暂无智能体配置">
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['ai:model:add']">添加设备</el-button>
        </el-empty>

        <div v-else class="agent-grid">
          <article v-for="item in modelList" :key="item.id" class="agent-card">
            <el-dropdown class="card-more" trigger="click" @command="command => handleCardCommand(command, item)">
              <button type="button" class="more-btn" aria-label="更多操作">
                <i class="el-icon-more"></i>
              </button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="activate" icon="el-icon-switch-button" :disabled="item.active === 1 || item.enabled !== 1">
                  设为当前
                </el-dropdown-item>
                <el-dropdown-item command="edit" icon="el-icon-edit">编辑配置</el-dropdown-item>
                <el-dropdown-item command="delete" icon="el-icon-delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <div class="card-title-row">
              <h3>{{ item.name || "未命名智能体" }}</h3>
              <el-tag v-if="item.active === 1" type="success" size="mini">当前</el-tag>
              <el-tag v-else-if="item.enabled !== 1" type="info" size="mini">停用</el-tag>
            </div>

            <dl class="agent-meta">
              <div>
                <dt>角色音色：</dt>
                <dd>{{ getVoiceName(item) }}</dd>
              </div>
              <div>
                <dt>语言模型：</dt>
                <dd>{{ item.modelName || "未配置" }}</dd>
              </div>
              <div>
                <dt>最近对话：</dt>
                <dd>{{ getRecentText(item) }}</dd>
              </div>
            </dl>

            <div class="card-actions">
              <el-button size="mini" @click="handleUpdate(item)" v-hasPermi="['ai:model:edit']">配置角色</el-button>
              <el-button size="mini" @click="openVoiceDialog(item)">声纹识别</el-button>
              <el-button size="mini" @click="openHistoryDialog(item)">历史对话</el-button>
              <el-button type="primary" size="mini" @click="handleBindDevice(item)">
                添加设备
              </el-button>
            </div>
          </article>
        </div>
      </div>

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </div>

    <el-dialog :title="title" :visible.sync="open" custom-class="role-config-dialog" width="860px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="112px" class="role-config-form">
        <div class="role-section">
          <div class="section-title">角色模板</div>
          <div class="template-list">
            <button
              v-for="template in roleTemplates"
              :key="template.name"
              type="button"
              class="template-pill"
              :class="{ active: form.roleTemplate === template.name }"
              @click="applyRoleTemplate(template)"
            >
              {{ template.name }}
            </button>
          </div>
        </div>

        <el-form-item label="助手昵称" prop="name">
          <el-input v-model="form.name" placeholder="如 台湾女友" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="对话语言">
              <el-select v-model="form.dialogLanguage" style="width: 100%">
                <el-option label="普通话" value="普通话" />
                <el-option label="粤语" value="粤语" />
                <el-option label="英语" value="English" />
                <el-option label="日语" value="日本語" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色音色">
              <el-select v-model="form.remark" filterable allow-create default-first-option style="width: 100%">
                <el-option label="湾湾小何" value="湾湾小何" />
                <el-option label="温柔女声" value="温柔女声" />
                <el-option label="活泼男孩" value="活泼男孩" />
                <el-option label="默认音色" value="默认音色" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="角色介绍" prop="systemPrompt">
          <div class="prompt-head">
            <span></span>
            <el-button type="text" icon="el-icon-magic-stick" @click="optimizePrompt">AI一键优化</el-button>
          </div>
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="6"
            placeholder="描述智能体身份、说话方式、任务边界、偏好和禁忌。"
          />
        </el-form-item>

        <div class="role-section memory-section">
          <el-form-item label="记忆类型">
            <el-radio-group v-model="form.memoryType">
              <el-radio-button label="记忆体 (短期记忆)" />
              <el-radio-button label="关闭记忆" />
            </el-radio-group>
          </el-form-item>
          <el-form-item label="当前记忆">
            <div class="memory-box">
              <el-input v-model="form.currentMemory" type="textarea" :rows="3" placeholder="每次对话后重新生成" />
              <el-button size="mini" @click="form.currentMemory = ''">清除记忆</el-button>
            </div>
          </el-form-item>
          <el-form-item label="说话人记忆">
            <el-switch v-model="form.speakerMemory" />
            <span class="form-hint">存在说话人记忆时，优先使用其记忆信息。</span>
          </el-form-item>
        </div>

        <el-form-item label="语言模型" prop="modelName">
          <el-select v-model="form.modelName" filterable allow-create default-first-option style="width: 100%">
            <el-option label="AI2 本地 Qwen3.5 0.8B" value="Qwen3.5-0.8B-UD-Q4_K_XL.gguf" />
            <el-option label="小智 Lite" value="deepseek-chat" />
            <el-option label="DeepSeek Chat" value="deepseek-chat" />
            <el-option label="Qwen Turbo" value="qwen-turbo" />
            <el-option label="GPT-4o mini" value="gpt-4o-mini" />
          </el-select>
          <div class="form-tip">改变模型后，建议清空记忆体，以免影响体验。</div>
        </el-form-item>

        <el-collapse class="advanced-collapse">
          <el-collapse-item title="高级设置" name="advanced">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="供应商" prop="provider">
                  <el-select v-model="form.provider" placeholder="请选择" style="width: 100%" @change="handleProviderChange">
                    <el-option label="OpenAI" value="openai" />
                    <el-option label="DeepSeek" value="deepseek" />
                    <el-option label="Qwen" value="qwen" />
                    <el-option label="本地模型" value="local" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="接口类型" prop="apiType">
                  <el-select v-model="form.apiType" style="width: 100%">
                    <el-option label="OpenAI兼容" value="openai" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="API地址" prop="baseUrl">
              <el-input v-model="form.baseUrl" placeholder="如 https://api.deepseek.com/v1/chat/completions" />
            </el-form-item>
            <el-form-item label="API Key" prop="apiKey">
              <el-input v-model="form.apiKey" placeholder="请输入API Key" show-password />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="启用" prop="enabled">
                  <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前模型" prop="active">
                  <el-switch v-model="form.active" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="MCP设置" name="mcp">
            <el-empty description="暂未接入 MCP 工具配置" />
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <span class="save-note">注意：保存配置后，需要重启设备，新的配置才会生效。</span>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="resetRoleConfig">重 置</el-button>
      </div>
    </el-dialog>

    <el-dialog title="声纹识别" :visible.sync="voiceOpen" width="520px" append-to-body>
      <div class="feature-dialog">
        <h4>{{ selectedAgent ? selectedAgent.name : "智能体" }}</h4>
        <p>当前页面先保留声纹识别入口，后续可接入设备端音频采集与声纹模板管理。</p>
        <el-button type="primary" plain icon="el-icon-microphone">开始录入</el-button>
      </div>
    </el-dialog>

    <el-dialog title="历史对话" :visible.sync="historyOpen" width="560px" append-to-body>
      <div class="feature-dialog">
        <h4>{{ selectedAgent ? selectedAgent.name : "智能体" }}</h4>
        <el-empty description="暂无历史对话记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAiModel, getAiModel, delAiModel, addAiModel, updateAiModel, activateAiModel } from "@/api/ai/model"

export default {
  name: "AiModel",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: false,
      total: 0,
      modelList: [],
      title: "",
      open: false,
      voiceOpen: false,
      historyOpen: false,
      selectedAgent: null,
      roleTemplates: [
        {
          name: "台湾女友",
          voice: "湾湾小何",
          prompt: "你是一位说话自然、温柔、带一点台湾口吻的中文陪伴助手。你会用轻松亲切的方式回应用户，适合日常聊天、情绪陪伴和生活建议。回答要简洁，不要夸张表演。"
        },
        {
          name: "土豆子",
          voice: "活泼男孩",
          prompt: "你叫土豆子，是一个活泼、机灵、好奇心很强的陪伴助手。你会主动关心用户的状态，用简单明快的话帮助用户完成日常任务。"
        },
        {
          name: "English Tutor",
          voice: "默认音色",
          prompt: "You are a friendly English tutor. Help the user practice English with short explanations, natural examples, and gentle corrections. Keep responses practical and encouraging."
        },
        {
          name: "好奇小男孩",
          voice: "活泼男孩",
          prompt: "你是一个好奇、开朗的小男孩角色，会用天真但不幼稚的方式陪用户聊天。回答要有亲和力，适合家庭陪伴场景。"
        },
        {
          name: "汪汪队队长",
          voice: "默认音色",
          prompt: "你是可靠、积极的任务队长，会用清晰步骤帮助用户解决问题。你说话坚定友好，优先给出可执行建议。"
        }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        provider: undefined,
        modelName: undefined,
        enabled: undefined
      },
      form: {},
      providerDefaults: {
        openai: {
          baseUrl: "https://api.openai.com/v1",
          modelName: "gpt-4o-mini",
          apiKey: ""
        },
        deepseek: {
          baseUrl: "https://api.deepseek.com/v1",
          modelName: "deepseek-chat",
          apiKey: ""
        },
        qwen: {
          baseUrl: "https://dashscope.aliyuncs.com/compatible-mode/v1",
          modelName: "qwen-turbo",
          apiKey: ""
        },
        local: {
          baseUrl: "http://127.0.0.1:18080/v1",
          modelName: "Qwen3.5-0.8B-UD-Q4_K_XL.gguf",
          apiKey: "local"
        }
      },
      rules: {
        name: [{ required: true, message: "配置名称不能为空", trigger: "blur" }],
        provider: [{ required: true, message: "供应商不能为空", trigger: "change" }],
        baseUrl: [{ required: true, message: "API地址不能为空", trigger: "blur" }],
        modelName: [{ required: true, message: "模型名不能为空", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listAiModel(this.queryParams).then(response => {
        this.modelList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getVoiceName(row) {
      if (row.remark) {
        return row.remark
      }
      const providerMap = {
        openai: "OpenAI 默认",
        deepseek: "DeepSeek 默认",
        qwen: "通义默认",
        local: "本地音色"
      }
      return providerMap[row.provider] || "默认音色"
    },
    getRecentText(row) {
      if (row.active === 1) {
        return "当前使用中"
      }
      return row.enabled === 1 ? "无" : "已停用"
    },
    handleToolbarCommand(command) {
      if (command === "refresh") {
        this.getList()
        return
      }
      if (command === "search") {
        this.showSearch = !this.showSearch
      }
    },
    handleCardCommand(command, row) {
      if (command === "activate") {
        this.handleActivate(row)
      } else if (command === "edit") {
        this.handleUpdate(row)
      } else if (command === "delete") {
        this.handleDelete(row)
      }
    },
    openVoiceDialog(row) {
      this.selectedAgent = row
      this.voiceOpen = true
    },
    openHistoryDialog(row) {
      this.selectedAgent = row
      this.historyOpen = true
    },
    handleBindDevice(row) {
      this.selectedAgent = row
      this.$modal.msg("设备绑定功能待接入")
    },
    handleProviderChange(provider) {
      const defaults = this.providerDefaults[provider]
      if (!defaults) {
        return
      }
      this.form.baseUrl = defaults.baseUrl
      this.form.modelName = defaults.modelName
      this.form.apiKey = defaults.apiKey
      if (provider === "local") {
        this.form.apiType = "openai"
        this.form.enabled = 1
        this.form.active = 1
        this.$modal.msgSuccess("已切换到 AI2 本地模型")
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        provider: "local",
        baseUrl: "http://127.0.0.1:18080/v1",
        apiKey: "local",
        modelName: "Qwen3.5-0.8B-UD-Q4_K_XL.gguf",
        apiType: "openai",
        temperature: 0.7,
        maxTokens: 1024,
        systemPrompt: undefined,
        enabled: 1,
        active: 0,
        remark: "湾湾小何",
        roleTemplate: "台湾女友",
        dialogLanguage: "普通话",
        memoryType: "记忆体 (短期记忆)",
        currentMemory: "",
        speakerMemory: true
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
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "配置角色"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getAiModel(id).then(response => {
        this.form = {
          ...this.form,
          ...response.data,
          roleTemplate: response.data.name || "台湾女友",
          dialogLanguage: "普通话",
          memoryType: "记忆体 (短期记忆)",
          currentMemory: "",
          speakerMemory: true
        }
        this.open = true
        this.title = "配置角色 " + (this.form.name || "")
      })
    },
    applyRoleTemplate(template) {
      this.form.roleTemplate = template.name
      this.form.name = template.name
      this.form.remark = template.voice
      this.form.systemPrompt = template.prompt
    },
    optimizePrompt() {
      const name = this.form.name || "智能体"
      const language = this.form.dialogLanguage || "普通话"
      const current = this.form.systemPrompt || ""
      this.form.systemPrompt = [
        current || `你是${name}，一个面向家庭和办公场景的中文陪伴助手。`,
        `请优先使用${language}与用户交流。`,
        "回答要简洁、自然、可靠；当涉及设备控制、传感器状态或摄像头信息时，要说明依据和不确定性；不要编造实时状态。"
      ].join("\n")
    },
    resetRoleConfig() {
      const id = this.form.id
      if (id) {
        this.handleUpdate({ id })
      } else {
        this.reset()
      }
    },
    buildSubmitPayload() {
      const {
        id,
        name,
        provider,
        baseUrl,
        apiKey,
        modelName,
        apiType,
        temperature,
        maxTokens,
        systemPrompt,
        enabled,
        active,
        remark
      } = this.form
      return {
        id,
        name,
        provider,
        baseUrl,
        apiKey,
        modelName,
        apiType,
        temperature,
        maxTokens,
        systemPrompt,
        enabled,
        active,
        remark
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return
        }
        const payload = this.buildSubmitPayload()
        const request = payload.id !== undefined ? updateAiModel(payload) : addAiModel(payload)
        request.then(() => {
          this.$modal.msgSuccess(payload.id !== undefined ? "保存成功" : "新增成功")
          this.open = false
          this.getList()
        })
      })
    },
    handleActivate(row) {
      this.$modal.confirm('确认切换到模型配置 "' + row.name + '" 吗？').then(() => {
        return activateAiModel(row.id)
      }).then(() => {
        this.$modal.msgSuccess("切换成功")
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('确认删除AI模型配置编号为 "' + ids + '" 的数据项吗？').then(() => {
        return delAiModel(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.agent-page {
  --agent-bg: #f5f6f8;
  --agent-shell: #fff;
  --agent-card: #fff;
  --agent-card-soft: #fafbfc;
  --agent-line: #edf0f4;
  --agent-line-strong: #d8dce3;
  --agent-text: #111827;
  --agent-muted: #8b95a1;
  --agent-icon: #9aa3af;
  --agent-icon-hover: #4b5563;
  --agent-shadow: 0 4px 18px rgba(15, 23, 42, 0.08);

  min-height: calc(100vh - 84px);
  padding: 0;
  background: var(--agent-bg);
}

.agent-shell {
  min-height: calc(100vh - 84px);
  padding: 28px 48px 48px;
  background: var(--agent-shell);
}

.agent-toolbar {
  max-width: 1040px;
}

.agent-breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  color: var(--agent-muted);
  font-size: 14px;

  strong {
    color: var(--agent-text);
    font-weight: 600;
  }

  i {
    font-size: 12px;
    color: var(--agent-muted);
  }
}

.toolbar-actions {
  display: flex;
  align-items: center;
  margin-top: 18px;

  ::v-deep .el-button {
    height: 32px;
    border-color: var(--agent-line-strong);
    background: var(--agent-card);
    color: var(--agent-text);
  }
}

.add-device-btn {
  min-width: 120px;
  border-radius: 4px 0 0 4px;
}

.drop-btn {
  width: 32px;
  padding: 0;
  border-left: 0;
  border-radius: 0 4px 4px 0;
}

.agent-filter {
  max-width: 1040px;
  margin-top: 16px;
  padding: 16px 16px 0;
  border: 1px solid var(--agent-line);
  border-radius: 6px;
  background: var(--agent-card-soft);
}

.agent-board {
  min-height: 360px;
  max-width: 1040px;
  margin-top: 14px;
  padding-top: 1px;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 400px);
  gap: 22px;
  padding: 0 0 24px 66px;
}

.agent-card {
  position: relative;
  min-height: 214px;
  padding: 28px 26px 22px;
  border: 1px solid var(--agent-line);
  border-radius: 8px;
  background: var(--agent-card);
  box-shadow: var(--agent-shadow);
}

.card-more {
  position: absolute;
  top: 14px;
  right: 14px;
}

.more-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: transparent;
  color: var(--agent-icon);
  cursor: pointer;

  &:hover {
    background: var(--agent-card-soft);
    color: var(--agent-icon-hover);
  }
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 28px;
  padding-right: 34px;

  h3 {
    margin: 0;
    color: var(--agent-text);
    font-size: 16px;
    font-weight: 700;
    line-height: 28px;
  }
}

.agent-meta {
  margin: 26px 0 18px;
  color: var(--agent-text);
  font-size: 14px;

  div {
    display: flex;
    min-height: 26px;
    line-height: 26px;
  }

  dt {
    flex: 0 0 auto;
    font-weight: 500;
  }

  dd {
    min-width: 0;
    margin: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  ::v-deep .el-button {
    margin-left: 0;
    border-radius: 4px;
  }
}

.feature-dialog {
  min-height: 120px;

  h4 {
    margin: 0 0 10px;
    color: var(--agent-text);
    font-size: 16px;
  }

  p {
    margin: 0 0 18px;
    color: var(--agent-muted);
    line-height: 1.7;
  }
}

::v-deep .role-config-dialog {
  border-radius: 8px;

  .el-dialog__header {
    padding: 24px 32px 12px;
  }

  .el-dialog__title {
    font-size: 18px;
    font-weight: 700;
  }

  .el-dialog__body {
    padding: 10px 32px 0;
  }

  .el-dialog__footer {
    padding: 16px 32px 26px;
  }
}

.role-config-form {
  max-height: 64vh;
  padding-right: 6px;
  overflow-y: auto;
}

.role-section {
  margin-bottom: 18px;
}

.section-title {
  margin: 0 0 10px 112px;
  color: var(--agent-text);
  font-size: 14px;
  font-weight: 600;
}

.template-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-left: 112px;
}

.template-pill {
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--agent-line-strong);
  border-radius: 4px;
  background: var(--agent-card);
  color: var(--agent-text);
  cursor: pointer;

  &.active,
  &:hover {
    border-color: #409eff;
    color: #1677ff;
    background: rgba(64, 158, 255, 0.08);
  }
}

.prompt-head {
  display: flex;
  justify-content: flex-end;
  min-height: 0;
  margin-bottom: 6px;
}

.memory-section {
  padding-top: 4px;
  border-top: 1px solid var(--agent-line);
}

.memory-box {
  display: flex;
  gap: 10px;
  align-items: flex-start;

  .el-button {
    flex: 0 0 auto;
  }
}

.form-hint,
.form-tip {
  color: var(--agent-muted);
  font-size: 12px;
  line-height: 1.7;
}

.form-hint {
  margin-left: 10px;
}

.advanced-collapse {
  margin-left: 112px;
  border-top: 1px solid var(--agent-line);
  border-bottom: 1px solid var(--agent-line);

  ::v-deep .el-collapse-item__header,
  ::v-deep .el-collapse-item__wrap {
    color: var(--agent-text);
    background: transparent;
    border-bottom-color: var(--agent-line);
  }

  ::v-deep .el-collapse-item__content {
    padding: 18px 0 6px;
  }
}

.save-note {
  float: left;
  max-width: 430px;
  color: var(--agent-muted);
  font-size: 13px;
  line-height: 32px;
  text-align: left;
}

@media (max-width: 768px) {
  .agent-shell {
    padding: 18px 14px 28px;
  }

  .toolbar-actions {
    margin-top: 16px;
  }

  .agent-grid {
    grid-template-columns: minmax(0, 1fr);
    padding-left: 0;
  }

  .agent-card {
    padding: 24px 18px 20px;
  }

  .section-title,
  .template-list,
  .advanced-collapse {
    margin-left: 0;
  }

  .memory-box {
    display: block;

    .el-button {
      margin-top: 8px;
    }
  }
}
</style>

<style lang="scss">
#app .app-wrapper.theme-dark .agent-page {
  --agent-bg: #061523;
  --agent-shell: #061523;
  --agent-card: #102033;
  --agent-card-soft: #142334;
  --agent-line: #273a4c;
  --agent-line-strong: #2d4053;
  --agent-text: #eef7ff;
  --agent-muted: #a7b7c4;
  --agent-icon: #7f93a3;
  --agent-icon-hover: #00daf3;
  --agent-shadow: 0 18px 42px rgba(0, 0, 0, 0.24);
}

#app .app-wrapper.theme-dark .agent-page .agent-shell {
  background:
    radial-gradient(circle at 78% 6%, rgba(0, 218, 243, 0.08), transparent 30%),
    var(--agent-shell);
}

#app .app-wrapper.theme-dark .agent-page .agent-card {
  background:
    linear-gradient(180deg, rgba(20, 35, 52, 0.98), rgba(16, 32, 51, 0.98)),
    var(--agent-card);
}

#app .app-wrapper.theme-dark .agent-page .agent-filter {
  background: rgba(16, 32, 51, 0.72);
}

#app .app-wrapper.theme-dark .agent-page .toolbar-actions .el-button,
#app .app-wrapper.theme-dark .agent-page .card-actions .el-button:not(.el-button--primary) {
  color: #d7e5f2 !important;
  background: #1d2c3e !important;
  border-color: #2d4053 !important;
}

#app .app-wrapper.theme-dark .agent-page .toolbar-actions .el-button:hover,
#app .app-wrapper.theme-dark .agent-page .card-actions .el-button:not(.el-button--primary):hover,
#app .app-wrapper.theme-dark .agent-page .more-btn:hover {
  color: #00daf3 !important;
  background: rgba(0, 218, 243, 0.1) !important;
  border-color: rgba(0, 218, 243, 0.32) !important;
}

#app .app-wrapper.theme-dark .agent-page .el-empty__description p,
#app .app-wrapper.theme-dark .agent-page .el-pagination,
#app .app-wrapper.theme-dark .agent-page .el-pagination__total,
#app .app-wrapper.theme-dark .agent-page .el-pagination__jump {
  color: #a7b7c4 !important;
}

#app .app-wrapper.theme-dark .agent-page .pagination-container,
#app .app-wrapper.theme-dark .agent-page .el-pagination button,
#app .app-wrapper.theme-dark .agent-page .el-pager li {
  color: #d7e5f2 !important;
  background: #061523 !important;
}

#app .app-wrapper.theme-dark .agent-page .el-pager li.active {
  color: #002c35 !important;
  background: #00daf3 !important;
}

#app .app-wrapper.theme-dark .role-config-dialog .el-dialog__header,
#app .app-wrapper.theme-dark .role-config-dialog .el-dialog__body,
#app .app-wrapper.theme-dark .role-config-dialog .el-dialog__footer {
  background: #102033 !important;
}

#app .app-wrapper.theme-dark .role-config-dialog .template-pill {
  color: #d7e5f2;
  background: #1d2c3e;
  border-color: #2d4053;
}

#app .app-wrapper.theme-dark .role-config-dialog .template-pill.active,
#app .app-wrapper.theme-dark .role-config-dialog .template-pill:hover {
  color: #00daf3;
  background: rgba(0, 218, 243, 0.1);
  border-color: rgba(0, 218, 243, 0.42);
}

#app .app-wrapper.theme-dark .role-config-dialog .advanced-collapse .el-collapse-item__header,
#app .app-wrapper.theme-dark .role-config-dialog .advanced-collapse .el-collapse-item__wrap {
  color: #d7e5f2 !important;
  background: transparent !important;
  border-bottom-color: #273a4c !important;
}
</style>
