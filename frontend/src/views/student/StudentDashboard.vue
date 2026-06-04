<template>
  <el-container class="dashboard">
    <el-aside width="200px" class="sidebar">
      <h3>学生学习台</h3>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/student">课程列表</el-menu-item>
        <el-menu-item index="/student/progress">我的进度</el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text @click="logout">退出登录</el-button>
      </div>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.dashboard { min-height: 100vh; }
.sidebar {
  background: #f8f8f8;
  border-right: 1px solid #eee;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}
.sidebar h3 { padding: 0 20px; color: #c41d1d; margin-bottom: 20px; }
.sidebar-footer { margin-top: auto; padding: 20px; }
</style>
