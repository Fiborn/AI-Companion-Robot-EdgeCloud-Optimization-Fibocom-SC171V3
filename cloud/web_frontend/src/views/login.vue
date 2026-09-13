<template>
  <div class="login-page" :class="loginThemeClass">
    <div class="login-card">
      <div class="card-header">
        <img src="@/assets/logo/logo.jpg" alt="logo" class="card-logo" />
        <button type="button" class="theme-toggle" :title="isDarkTheme ? '切换浅色模式' : '切换深色模式'" @click="toggleLoginTheme">
          <i :class="isDarkTheme ? 'el-icon-sunny' : 'el-icon-moon'" />
          <span>{{ isDarkTheme ? '浅色' : '深色' }}</span>
        </button>
      </div>
      <h1 class="card-title">{{ title }}</h1>
      <p class="card-subtitle">欢迎使用</p>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="用户名"
          >
            <svg-icon slot="prefix" icon-class="user" class="input-prefix-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            auto-complete="off"
            placeholder="密码"
            @keyup.enter.native="handleLogin"
          >
            <svg-icon slot="prefix" icon-class="password" class="input-prefix-icon" />
            <svg-icon
              slot="suffix"
              :icon-class="showPassword ? 'eye-open' : 'eye'"
              class="input-suffix-icon"
              @click.native="showPassword = !showPassword"
            />
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="验证码"
            style="width: 63%"
            @keyup.enter.native="handleLogin"
          >
            <svg-icon slot="prefix" icon-class="validCode" class="input-prefix-icon" />
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img"/>
          </div>
        </el-form-item>
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </div>
        <el-button
          :loading="loading"
          type="primary"
          class="login-btn"
          @click.native.prevent="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
        <div class="register-link" v-if="register">
          还没有账号？<router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form>
    </div>
    <div class="page-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE || '暖芯陪伴机器人后台',
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      showPassword: false,
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入用户名" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined,
      sideTheme: 'theme-dark'
    }
  },
  computed: {
    isDarkTheme() {
      return this.sideTheme === 'theme-dark'
    },
    loginThemeClass() {
      return this.isDarkTheme ? 'login-dark' : 'login-light'
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.initLoginTheme()
    this.getCode()
    this.getCookie()
  },
  methods: {
    initLoginTheme() {
      let storageSetting = {}
      try {
        storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || {}
      } catch (e) {
        storageSetting = {}
      }
      this.sideTheme = storageSetting.sideTheme || defaultSettings.sideTheme || 'theme-dark'
      this.syncStoreTheme()
      this.applyLoginTheme()
    },
    applyLoginTheme() {
      document.body.classList.toggle('iot-theme-dark', this.isDarkTheme)
      document.body.classList.toggle('iot-theme-light', !this.isDarkTheme)
    },
    syncStoreTheme() {
      this.$store.dispatch('settings/changeSetting', {
        key: 'sideTheme',
        value: this.sideTheme
      })
    },
    toggleLoginTheme() {
      this.sideTheme = this.isDarkTheme ? 'theme-light' : 'theme-dark'
      let storageSetting = {}
      try {
        storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || {}
      } catch (e) {
        storageSetting = {}
      }
      localStorage.setItem('layout-setting', JSON.stringify({
        ...storageSetting,
        sideTheme: this.sideTheme
      }))
      this.syncStoreTheme()
      this.applyLoginTheme()
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  width: 100%;
  background-color: #f0f2f5;
  position: relative;
  padding: 24px;
}

.login-card {
  width: 400px;
  max-width: 90vw;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 36px 36px 28px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-logo {
  width: 42px;
  height: 42px;
}

.theme-toggle {
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  border: 1px solid #d9e2ef;
  border-radius: 8px;
  color: #426176;
  background: #f8fbff;
  cursor: pointer;
  font-size: 13px;
  transition: all .2s ease;

  &:hover {
    color: #00788a;
    border-color: #9adce5;
    background: #e9f8fb;
  }
}

.card-title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.card-subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin: 0 0 28px 0;
}

.login-form {
  .el-form-item {
    margin-bottom: 18px;
  }

  .el-input {
    height: 42px;
  }

  ::v-deep .el-input__inner {
    height: 42px;
    line-height: 42px;
    border-radius: 8px;
    border: 1px solid #dcdfe6;
    font-size: 14px;
    color: #333;
    padding-left: 38px;
    transition: all 0.25s ease;

    &::placeholder {
      color: #c0c4cc;
    }

    &:focus {
      border-color: #3b82f6;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.08);
    }
  }

  ::v-deep .el-input__prefix {
    left: 10px;
    color: #c0c4cc;
    transition: color 0.25s ease;
  }

  ::v-deep .el-input__suffix {
    right: 12px;
    cursor: pointer;
  }

  &:focus-within {
    ::v-deep .el-input__prefix {
      color: #3b82f6;
    }
  }
}

.input-prefix-icon {
  font-size: 15px;
}

.input-suffix-icon {
  font-size: 15px;
  color: #c0c4cc;
  cursor: pointer;

  &:hover {
    color: #666;
  }
}

.form-options {
  margin-bottom: 18px;

  ::v-deep .el-checkbox__label {
    font-size: 13px;
    color: #606266;
  }

  ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #3b82f6;
    border-color: #3b82f6;
  }

  ::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
    color: #3b82f6;
  }
}

.login-code {
  width: 35%;
  height: 42px;
  float: right;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  transition: border-color 0.25s ease;

  &:hover {
    border-color: #3b82f6;
  }

  img {
    cursor: pointer;
    vertical-align: middle;
    width: 100%;
    height: 100%;
  }
}

.login-code-img {
  height: 42px;
}

.login-btn {
  width: 100%;
  height: 42px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  background-color: #2b8bd6;
  border-color: #2b8bd6;
  letter-spacing: 4px;
  transition: all 0.25s ease;

  &:hover,
  &:focus {
    background-color: #2377ba;
    border-color: #2377ba;
  }

  &.is-loading {
    opacity: 0.85;
  }
}

.register-link {
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
  margin-top: 16px;

  .link-type {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      color: #2563eb;
    }
  }
}

.page-footer {
  position: fixed;
  bottom: 20px;
  text-align: center;
  color: #bbb;
  font-size: 12px;
}

.login-page.login-dark {
  background-color: #061523;

  .login-card {
    color: #d7e5f2;
    background: #102033;
    border-color: #273a4c;
    box-shadow: 0 22px 56px rgba(0, 0, 0, .34);
  }

  .theme-toggle {
    color: #bae6fd;
    background: rgba(14, 165, 233, .12);
    border-color: rgba(56, 189, 248, .34);

    &:hover {
      color: #002c35;
      background: #00daf3;
      border-color: #00daf3;
    }
  }

  .card-title {
    color: #eef7ff;
  }

  .card-subtitle,
  .register-link {
    color: #8fa4b6;
  }

  .input-prefix-icon,
  .input-suffix-icon {
    color: #8094a8;
  }

  .input-suffix-icon:hover {
    color: #00daf3;
  }

  .login-form {
    ::v-deep .el-input__inner {
      color: #eef7ff;
      background: #142334;
      border-color: #2d4053;

      &::placeholder {
        color: #7f93a7;
      }

      &:focus {
        border-color: #00daf3;
        box-shadow: 0 0 0 2px rgba(0, 218, 243, .12);
      }
    }
  }

  .form-options {
    ::v-deep .el-checkbox__inner {
      background: #142334;
      border-color: #2d4053;
    }

    ::v-deep .el-checkbox__label {
      color: #b6c6d4;
    }
  }

  .login-code {
    background: #142334;
    border-color: #2d4053;

    &:hover {
      border-color: #00daf3;
    }
  }

  .page-footer {
    color: rgba(215, 229, 242, .56);
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 24px 24px;
  }

  .card-title {
    font-size: 20px;
  }
}
</style>
