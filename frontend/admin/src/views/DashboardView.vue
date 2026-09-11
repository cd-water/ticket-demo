<script setup lang="ts">
import { useRouter } from 'vue-router'
import { adminLogout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

async function onLogout() {
  try {
    await adminLogout()
  } catch {
    /* 即使后端吊销失败也本地退出 */
  }
  auth.clear()
  await router.push('/login')
}
</script>

<template>
  <div class="placeholder">
    <span class="dot" aria-hidden="true"></span>
    <h1>{{ auth.roleLabel }}</h1>
    <p>{{ auth.scopeLabel }} · 管理后台建设中</p>
    <p class="meta">{{ auth.admin?.username }}</p>
    <el-button plain @click="onLogout">退出登录</el-button>
  </div>
</template>

<style scoped>
.placeholder {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: radial-gradient(1000px 600px at 70% -10%, #3a3200 0%, var(--bg) 60%);
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: var(--brand);
  box-shadow: 0 0 18px rgba(255, 195, 0, 0.6);
  margin-bottom: 8px;
}

h1 {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 28px;
  font-weight: 900;
}

p {
  margin: 0;
  color: var(--ink-2);
  font-size: 14px;
}

.meta {
  font-family: var(--font-din);
  letter-spacing: 1px;
}

.el-button {
  margin-top: 22px;
}
</style>
