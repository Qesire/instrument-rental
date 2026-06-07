<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <div class="logo">
        <h3 style="color: #fff; text-align: center; padding: 16px 0; margin: 0">
          🎵 乐器租赁
        </h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/">
          <el-icon><DataAnalysis /></el-icon>
          <span>库存看板</span>
        </el-menu-item>
        <el-menu-item index="/instruments">
          <el-icon><Monitor /></el-icon>
          <span>乐器管理</span>
        </el-menu-item>
        <el-menu-item index="/models">
          <el-icon><Collection /></el-icon>
          <span>型号管理</span>
        </el-menu-item>
        <el-menu-item index="/reservations">
          <el-icon><Calendar /></el-icon>
          <span>预约日历</span>
        </el-menu-item>
        <el-menu-item index="/revenue">
          <el-icon><TrendCharts /></el-icon>
          <span>营收统计</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/maintenance">
          <el-icon><Setting /></el-icon>
          <span>维护管理</span>
        </el-menu-item>
        <el-menu-item index="/scan">
          <el-icon><Scan /></el-icon>
          <span>扫码操作</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Tools /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: flex-end; border-bottom: 1px solid #e6e6e6">
        <span style="margin-right: 16px">{{ userInfo?.username || '管理员' }}</span>
        <el-button type="danger" text @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataAnalysis, Monitor, Collection, Calendar,
  TrendCharts, User, Setting, Scan, Tools
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const userInfo = computed(() => authStore.user)

function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
.logo {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-menu {
  border-right: none;
}
</style>