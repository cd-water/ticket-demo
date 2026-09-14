<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCinema } from '@/api/cinemas'
import { useCinemaId } from '@/composables/useCinemaId'
import type { CinemaVO } from '@/types/api'

const route = useRoute()
const router = useRouter()
const cinemaId = useCinemaId()
const cinema = ref<CinemaVO | null>(null)

/** 当前 tab，取自路径最后一段 */
const active = computed(() => route.path.split('/').pop() ?? 'halls')

async function loadCinema() {
  try {
    cinema.value = await getCinema(cinemaId.value)
  } catch {
    /* 错误由 http.ts 统一弹 */
  }
}

onMounted(loadCinema)
watch(cinemaId, loadCinema)

function back() {
  router.push('/cinemas')
}

function go(tab: string) {
  router.push(`/cinemas/${cinemaId.value}/${tab}`)
}
</script>

<template>
  <div class="detail">
    <header class="head">
      <button class="back" @click="back">← 返回影院列表</button>
      <h3 class="title">{{ cinema?.name ?? `影院 #${cinemaId}` }}</h3>
    </header>

    <el-tabs :model-value="active" @update:model-value="go" class="tabs">
      <el-tab-pane name="halls" label="影厅管理" />
      <el-tab-pane name="screenings" label="排场管理" />
      <el-tab-pane name="orders" label="订单管理" />
    </el-tabs>

    <div class="body">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.detail {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 18px 22px;
}

.head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 8px;
}

.back {
  background: none;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 6px 14px;
  font-family: inherit;
  font-size: 12px;
  color: var(--ink-2);
  cursor: pointer;
}

.back:hover {
  color: var(--brand);
  border-color: var(--brand);
}

.title {
  margin: 0;
  font-size: 18px;
  font-family: var(--font-serif);
  font-weight: 800;
}

.tabs {
  margin-top: 6px;
}

.body {
  margin-top: 10px;
}
</style>
