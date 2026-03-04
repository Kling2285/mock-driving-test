<template>
  <div class="manager-layout">
    <!-- 顶部表头（两行布局） -->
    <header class="app-header">
      <div class="header-row first-row">
        <div class="header-left">
          <span class="site-name">用户端</span>
        </div>
        <div class="header-right" style="padding-right: 50px;">
          <el-button
              v-if="!admin.username"
              type="primary"
              text-color="#fff"
              @click="handleLogin"
          >
            登录
          </el-button>
          <el-popover
              v-else
              placement="bottom"
              :width="150"
              trigger="click"
          >
            <template #reference>
              <span class="username">{{ admin.username }}</span>
            </template>
            <div class="popover-menu">
              <el-button link size="small"  @click="personal">个人中心</el-button>
              <el-button link size="small" @click="handleLogout" style="margin-right: 10px;margin-top: 10px">退出登录</el-button>
            </div>
          </el-popover>
        </div>
      </div>

      <div class="header-row second-row">
        <div class="nav-links">
          <!-- 首页导航 -->
          <router-link
              to="/umanager/uhome"
              :class="['nav-link', $route.path === '/umanager/uhome' ? 'active' : '']"
          >
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>

        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-area">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import {
  HomeFilled,
  Document,
  ArrowDown
} from '@element-plus/icons-vue'

import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

// 路由实例
const $route = useRoute()
const $router = useRouter()

// 登录用户信息
const admin = ref({})

// 登录跳转（示例：跳转到登录页）
const handleLogin = () => {
  $router.push('/login')
}

const personal=()=>{
  $router.push('/umanager/center')
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('login_user')
  admin.value = {}
  $router.push('/')
}

// 初始化：读取本地存储的登录信息
onMounted(() => {
  const loginUser = localStorage.getItem('login_user')
  if (loginUser) {
    admin.value = JSON.parse(loginUser)
  }
})
</script>

<style scoped>
/* 其他原有样式保持不变 */

/* 关键：清除 el-dropdown 触发元素的默认边框和焦点边框 */
:deep(.el-dropdown) {
  outline: none; /* 清除焦点外边框 */
  border: none !important; /* 强制清除默认边框 */
}

/* 清除 el-dropdown 内部触发元素的边框（兜底） */
:deep(.el-dropdown .nav-main-item) {
  outline: none;
  border: none !important;
}

/* 清除下拉菜单的默认边框（可选，若菜单也有边框可添加） */
:deep(.el-dropdown-menu) {
  border: none !important; /* 去除下拉菜单的默认边框，按需选择 */
  box-shadow: 0 2px 12px rgba(0,0,0,0.1); /* 保留阴影，更美观 */
}

/* 你的原有样式... */
.manager-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: Arial, sans-serif;
  overflow: hidden;
}

.app-header {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-row {
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.first-row {
  height: 50px;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.site-name {
  font-size: 35px;
  font-weight: bold;
  color: #1E90FF;
  padding-left: 50px;
}

.username {
  cursor: pointer;
  font-size: 20px;
  padding-right: 50px;
}
.username:hover {
  text-decoration: underline;
}

.second-row {
  height: 60px;
  background-color: #1E90FF;
}

.nav-links {
  display: flex;
  gap: 5px;
  padding-left: 80px;
  width: fit-content;
  margin: 0 auto;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 100%;
  color: #fff;
  padding: 0 40px;
  text-decoration: none;
  font-size: 20px;
  transition: background-color 0.3s;
  border: none !important; /* 额外兜底：给导航项清除边框 */
  outline: none;
}

.nav-link.active {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none !important;
}

.nav-link:hover:not(.active) {
  background-color: rgba(255, 255, 255, 0.1);
  border: none !important;
}

.nav-main-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 100%;
  border: none !important;
  outline: none;
}

.dropdown-arrow {
  font-size: 20px;
  transition: transform 0.3s ease;
  color: #fff; /* 修正：原有颜色#1E90FF和导航栏背景同色，看不见，改为白色 */
  margin-top: 0; /* 移除多余的margin-top，避免箭头错位 */
}

.el-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

:deep(.el-dropdown-menu a) {
  color: #333;
  text-decoration: none;
  display: block;
  padding: 4px 16px;
}

:deep(.el-dropdown-menu a:hover) {
  color: #1E90FF;
  background-color: #f5fafe;
}

.popover-menu {
  display: flex;
  flex-direction: column;
  padding: 10px 0;
}
.popover-menu .el-button {
  padding: 8px 15px;
  text-align: left;
}
.popover-menu .el-button:hover {
  background-color: #f5f7fa;
}

.main-area {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>