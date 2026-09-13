<template>
  <div class="sidebar-logo-container" :class="{ collapse: collapse }" :style="{ backgroundColor: sideTheme === 'theme-dark' && navType !== 3 ? variables.menuBackground : variables.menuLightBackground }">
    <transition name="sidebarLogoFade">
      <router-link key="brand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <span class="sidebar-brand">
          <h1 class="sidebar-title" :style="{ color: sideTheme === 'theme-dark' && navType !== 3 ? variables.logoTitleColor : variables.logoLightTitleColor }">{{ shortTitle }}</h1>
          <small>陪伴机器人后台</small>
        </span>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/logo.jpg'
import variables from '@/assets/styles/variables.scss'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    variables() {
      return variables
    },
    sideTheme() {
      return this.$store.state.settings.sideTheme
    },
    navType() {
      return this.$store.state.settings.navType
    },
    shortTitle() {
      return '暖芯陪伴'
    }
  },
  data() {
    return {
      logo: logoImg
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 0.3s ease;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 56px;
  line-height: 56px;
  background: linear-gradient(135deg, #2C3E50 0%, #34495E 100%);
  text-align: left;
  overflow: hidden;
  padding-left: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(180deg, #4080FF 0%, #6BA3FF 100%);
  }

  & .sidebar-logo-link {
    display: flex;
    align-items: center;
    height: 100%;
    width: 100%;
    text-decoration: none;
    min-width: 0;

    & .sidebar-logo {
      width: 40px;
      height: 40px;
      vertical-align: middle;
      margin-right: 10px;
      border-radius: 8px;
      flex-shrink: 0;
      object-fit: cover;
      box-shadow: 0 8px 20px rgba(7, 136, 155, 0.22);
    }

    & .sidebar-brand {
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 2px;
      min-width: 0;
      max-width: calc(100% - 54px);
      opacity: 1;
      visibility: visible;
    }

    & .sidebar-title {
      display: block;
      margin: 0;
      color: #ffffff;
      font-weight: 600;
      line-height: 20px;
      font-size: 16px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'PingFang SC', 'Noto Sans CJK SC', Arial, sans-serif;
      letter-spacing: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    & small {
      display: block;
      color: rgba(255, 255, 255, 0.58);
      font-size: 11px;
      line-height: 14px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', 'PingFang SC', 'Noto Sans CJK SC', Arial, sans-serif;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &.collapse {
    justify-content: center;
    padding-left: 0;

    .sidebar-logo {
      margin-right: 0px;
    }
  }

  &.theme-light {
    background: #ffffff;
    border-bottom: 1px solid #e8eaf0;

    &::before {
      background: linear-gradient(180deg, #4080FF 0%, #6BA3FF 100%);
    }

    .sidebar-title {
      color: #2C3E50;
    }
  }
}

#app.hideSidebar .sidebar-logo-container,
#app .hideSidebar .sidebar-logo-container {
  padding-left: 0;
  justify-content: center;
}

#app.hideSidebar .sidebar-logo-container .sidebar-logo,
#app .hideSidebar .sidebar-logo-container .sidebar-logo {
  margin-right: 0;
}

#app.hideSidebar .sidebar-logo-container .sidebar-brand,
#app .hideSidebar .sidebar-logo-container .sidebar-brand {
  display: none;
}

#app .openSidebar .sidebar-logo-container:not(.collapse) .sidebar-brand {
  display: flex !important;
  opacity: 1 !important;
  visibility: visible !important;
}
</style>
