<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminLogout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { MENUS } from '@/config/menu'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const activeTitle = computed(() => (route.meta.title as string) || '')

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
  <div class="layout">
    <aside class="sidebar">
      <div class="side-brand">
        <span class="dot"></span>
        <div class="brand-text">
          <div class="brand-name">电影票务系统</div>
          <div class="brand-name">管理后台</div>
        </div>
      </div>

      <nav class="menu">
        <div
          v-for="m in MENUS"
          :key="m.key"
          class="menu-item"
          :class="{ on: route.path.startsWith(`/${m.key}`) }"
          @click="router.push(`/${m.key}`)"
        >
          <el-icon class="ic"><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </div>
      </nav>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="crumb"><b>{{ activeTitle }}</b></div>
        <div class="topbar-right">
          <div class="admin-chip">
            <span class="avatar">{{ auth.admin?.username?.charAt(0).toUpperCase() }}</span>
            <span>{{ auth.admin?.username }}</span>
          </div>
          <button class="logout-btn" @click="onLogout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* ---- 侧边栏 ---- */
.sidebar {
  width: 232px;
  flex: 0 0 232px;
  background: var(--panel);
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 22px;
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 800;
  border-bottom: 1px solid var(--line);
}

.side-brand .dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  background: var(--brand);
}

.brand-name {
  line-height: 1.35;
}

.menu {
  padding: 14px 12px;
  flex: 1;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--ink-2);
  cursor: pointer;
  margin-bottom: 2px;
}

.menu-item:hover {
  background: var(--panel-2);
  color: var(--ink);
}

.menu-item.on {
  background: rgba(255, 195, 0, 0.12);
  color: var(--brand);
  font-weight: 700;
}

.menu-item .ic {
  font-size: 16px;
}

/* ---- 主区 ---- */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 28px;
  border-bottom: 1px solid var(--line);
  background: var(--panel);
}

.crumb {
  font-size: 14px;
  color: var(--ink-2);
}

.crumb b {
  color: var(--ink);
}

.topbar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.admin-chip .avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--brand);
  color: var(--on-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.logout-btn {
  padding: 7px 16px;
  border-radius: 8px;
  border: 1px solid var(--line);
  background: none;
  font-size: 12px;
  font-family: inherit;
  color: var(--ink-2);
  cursor: pointer;
}

.logout-btn:hover {
  color: var(--danger);
  border-color: var(--danger);
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 26px 28px;
}

/* ---- 窄屏：侧边栏收成图标栏 ---- */
@media (max-width: 900px) {
  .sidebar {
    width: 70px;
    flex-basis: 70px;
  }

  .side-brand {
    font-size: 0;
    justify-content: center;
    padding: 20px 0;
  }

  .side-brand .brand-text {
    display: none;
  }

  .menu {
    padding: 14px 8px;
  }

  .menu-item {
    justify-content: center;
    padding: 12px 0;
  }

  .menu-item span:not(.ic) {
    display: none;
  }
}
</style>
