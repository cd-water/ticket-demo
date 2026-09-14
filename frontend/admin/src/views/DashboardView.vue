<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/dashboard'
import { formatAmount } from '@/utils/format'
import type { DashboardVO } from '@/types/api'

const loading = ref(false)
const data = ref<DashboardVO | null>(null)
const trendEl = ref<HTMLDivElement>()
const statusEl = ref<HTMLDivElement>()
const cinemaEl = ref<HTMLDivElement>()
const charts: echarts.ECharts[] = []

/** 统计卡片；金额类用品牌色突出，明细类带小字副行 */
const cards = computed(() => {
  const d = data.value
  if (!d) return []
  return [
    {
      label: '今日订单',
      breakdown: [
        { label: '待支付', value: d.todayPendingCount },
        { label: '已支付', value: d.todayPaidCount },
        { label: '已取消', value: d.todayCancelledCount },
      ],
    },
    { label: '今日营收', value: formatAmount(d.todayRevenue), brand: true },
    { label: '总营收', value: formatAmount(d.totalRevenue), brand: true },
    { label: '用户总数', value: d.userCount },
    { label: '热映电影', value: d.hotMovieCount },
    { label: '待映电影', value: d.upcomingMovieCount },
    {
      label: '影院数',
      breakdown: [
        { label: '营业', value: d.cinemaOpenCount },
        { label: '停业', value: d.cinemaClosedCount },
      ],
    },
  ]
})

async function load() {
  loading.value = true
  try {
    data.value = await getDashboard()
    renderCharts()
  } catch {
    /* 错误由 http.ts 统一弹 */
  } finally {
    loading.value = false
  }
}

/* ---- 图表 ---- */

/* echarts 在 setOption 中不解析 CSS 变量，使用与 tokens.css 等值的字面色 */
const INK = '#F5F2E8'
const INK_2 = '#9A927F'
const LINE = '#333333'
const BRAND = '#FFC300'
const PANEL = '#1E1E1E'
const FONT_SERIF = "'Noto Serif SC', 'Songti SC', SimSun, serif"

const AXIS_LABEL = { color: INK_2 }
const AXIS_LINE = { lineStyle: { color: LINE } }
const SPLIT_LINE = { splitLine: { lineStyle: { color: LINE } } }
const LINE_COLOR = '#57c46f'

function renderCharts() {
  const d = data.value
  if (!d) return
  renderTrend(charts[0], d)
  renderStatus(charts[1], d)
  renderCinema(charts[2], d)
}

/** 近 7 日已支付订单（黄柱，左轴）+ 营收（绿线，右轴） */
function renderTrend(chart: echarts.ECharts, d: DashboardVO) {
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['订单数', '营收'], textStyle: { color: INK_2 } },
    grid: { left: 44, right: 56, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: d.dailyStats.map((s) => s.date.slice(5)),
      axisLabel: AXIS_LABEL,
      axisLine: AXIS_LINE,
    },
    yAxis: [
      { type: 'value', name: '订单', axisLabel: AXIS_LABEL, ...SPLIT_LINE },
      { type: 'value', name: '营收', axisLabel: AXIS_LABEL, splitLine: { show: false } },
    ],
    series: [
      { name: '订单数', type: 'bar', barMaxWidth: 24, data: d.dailyStats.map((s) => s.orderCount), itemStyle: { color: BRAND } },
      { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: d.dailyStats.map((s) => s.revenue), itemStyle: { color: LINE_COLOR } },
    ],
  })
}

/** 总订单状态分布环形图 */
function renderStatus(chart: echarts.ECharts, d: DashboardVO) {
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}（{d}%）' },
    legend: { bottom: 0, textStyle: { color: INK_2 } },
    title: {
      text: String(d.totalOrderCount),
      subtext: '总订单',
      left: 'center',
      top: '36%',
      textStyle: { color: INK, fontSize: 24, fontWeight: 800, fontFamily: FONT_SERIF },
      subtextStyle: { color: INK_2, fontSize: 12 },
    },
    series: [
      {
        type: 'pie',
        radius: ['52%', '72%'],
        center: ['50%', '44%'],
        label: { show: false },
        itemStyle: { borderRadius: 4, borderColor: PANEL, borderWidth: 2 },
        data: [
          { name: '待支付', value: d.totalPendingCount, itemStyle: { color: BRAND } },
          { name: '已支付', value: d.totalPaidCount, itemStyle: { color: LINE_COLOR } },
          { name: '已取消', value: d.totalCancelledCount, itemStyle: { color: INK_2 } },
        ],
      },
    ],
  })
}

/** 分影院对比：已支付订单（黄柱，左轴）+ 营收（绿线，右轴），按营收降序 */
function renderCinema(chart: echarts.ECharts, d: DashboardVO) {
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['订单数', '营收'], textStyle: { color: INK_2 } },
    grid: { left: 44, right: 56, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: d.perCinemaStats.map((s) => s.name),
      axisLabel: AXIS_LABEL,
      axisLine: AXIS_LINE,
    },
    yAxis: [
      { type: 'value', name: '订单', axisLabel: AXIS_LABEL, ...SPLIT_LINE },
      { type: 'value', name: '营收', axisLabel: AXIS_LABEL, splitLine: { show: false } },
    ],
    series: [
      { name: '订单数', type: 'bar', barMaxWidth: 24, data: d.perCinemaStats.map((s) => s.orderCount), itemStyle: { color: BRAND } },
      { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: d.perCinemaStats.map((s) => s.revenue), itemStyle: { color: LINE_COLOR } },
    ],
  })
}

/** 票房占全站已支付营收比例 */
function share(part: number): string {
  const total = data.value?.totalRevenue
  if (!total) return ''
  return `· ${((part / total) * 100).toFixed(1)}%`
}

function onResize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  charts.push(echarts.init(trendEl.value!), echarts.init(statusEl.value!), echarts.init(cinemaEl.value!))
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  charts.forEach((c) => c.dispose())
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="cards">
      <div v-for="c in cards" :key="c.label" class="card">
        <div class="label">{{ c.label }}</div>
        <div v-if="c.value !== undefined" class="value" :class="{ brand: c.brand }">{{ c.value }}</div>
        <div v-if="c.breakdown" class="breakdown">
          <div v-for="b in c.breakdown" :key="b.label" class="row">
            <span class="b-label">{{ b.label }}</span>
            <span class="b-value">{{ b.value }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <div class="panel-head"><h3>近 7 日订单 / 营收</h3></div>
        <div ref="trendEl" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-head"><h3>总订单状态分布</h3></div>
        <div ref="statusEl" class="chart" />
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <div class="panel-head"><h3>分影院营收对比</h3></div>
        <div ref="cinemaEl" class="chart" />
      </div>
      <div class="panel">
        <div class="panel-head"><h3>影片票房排行 TOP 5</h3></div>
        <el-table :data="data?.movieRanking ?? []">
          <el-table-column label="排名" width="70">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="片名" prop="title" min-width="140" />
          <el-table-column label="订单数" width="90">
            <template #default="{ row }">{{ row.orderCount }}</template>
          </el-table-column>
          <el-table-column label="票房" min-width="150">
            <template #default="{ row }">
              {{ formatAmount(row.revenue) }}
              <span class="share">{{ share(row.revenue) }}</span>
            </template>
          </el-table-column>
          <template #empty>暂无票房数据</template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.card {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px 16px;
}

.card .label {
  font-size: 12px;
  color: var(--ink-2);
}

.card .value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 800;
  font-family: var(--font-serif);
}

.card .value.brand {
  color: var(--brand);
}

.card .breakdown {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card .breakdown .row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.card .breakdown .b-label {
  font-size: 12px;
  color: var(--ink-2);
}

.card .breakdown .b-value {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--brand);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.chart {
  height: 260px;
  padding: 4px 10px 10px;
}

.share {
  font-size: 12px;
  color: var(--ink-2);
}
</style>
