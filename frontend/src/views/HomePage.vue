<template>
  <div class="home-page">
    <el-container>
      <el-header class="header">
        <h1>非遗戏曲唱腔教学平台</h1>
        <div class="header-actions">
          <template v-if="authStore.isAuthenticated">
            <span>{{ authStore.user?.nickname }}</span>
            <el-button @click="goToDashboard">进入工作台</el-button>
            <el-button @click="authStore.logout()">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </el-header>
      <el-main class="main-content">
        <div class="hero">
          <h2>传承非遗文化，在线学习戏曲唱腔</h2>
          <p>AI 驱动的音高比对技术，帮助您精准掌握每一个唱腔细节</p>
          <div class="features">
            <div class="feature-card">
              <h3>名师教学</h3>
              <p>专业教师上传标准唱段，标注板式、调门和难点</p>
            </div>
            <div class="feature-card">
              <h3>智能评测</h3>
              <p>CREPE 音高提取 + 对比分析，生成准确率曲线</p>
            </div>
            <div class="feature-card">
              <h3>练习工具</h3>
              <p>慢速循环播放、节拍器辅助，逐句精练</p>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function goToDashboard() {
  router.push(authStore.isTeacher ? '/teacher' : '/student')
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}
.header h1 { font-size: 20px; color: #c41d1d; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.main-content { max-width: 1200px; margin: 0 auto; padding: 60px 20px; }
.hero { text-align: center; }
.hero h2 { font-size: 32px; margin-bottom: 16px; }
.hero p { color: #666; font-size: 18px; margin-bottom: 40px; }
.features { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.feature-card {
  padding: 32px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #eee;
}
.feature-card h3 { color: #c41d1d; margin-bottom: 12px; }
</style>
