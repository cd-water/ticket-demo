<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { getOrderItems, listOrdersByCinema } from '@/api/orders'
import ListPager from '@/components/ListPager.vue'
import StatusFilter from '@/components/StatusFilter.vue'
import StatusPill from '@/components/StatusPill.vue'
import { useCinemaId } from '@/composables/useCinemaId'
import { usePagedList } from '@/composables/useList'
import { formatAmount, formatDateTime } from '@/utils/format'
import type { OrderItemVO, OrderVO } from '@/types/api'

/** t_order.status：0-待支付 1-已支付 2-已取消 */
const STATUS_LABEL: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已取消' }
const STATUS_TONE: Record<number, 'warn' | 'ok' | 'muted'> = { 0: 'warn', 1: 'ok', 2: 'muted' }
const STATUS_OPTIONS = [
  { label: '全部', value: null as number | null },
  ...Object.entries(STATUS_LABEL).map(([value, label]) => ({ label, value: Number(value) })),
]

const cinemaId = useCinemaId()
const query = reactive({ orderNo: '', status: null as number | null })

/**
 * 订单号是 BIGINT 雪花 ID（后端按 long 解析），输入非数字会被拒成「参数类型错误」。
 * 按字符串透传，不要 Number()——19 位雪花超出 JS 安全整数范围。
 */
const orderNo = computed({
  get: () => query.orderNo,
  set: (v: string) => {
    query.orderNo = v.replace(/\D/g, '')
  },
})

const { rows, total, loading, page, size, load, search } = usePagedList<OrderVO>((p, s) =>
  listOrdersByCinema(cinemaId.value, {
    page: p,
    size: s,
    orderNo: query.orderNo || undefined,
    status: query.status ?? undefined,
  }),
)

function reset() {
  query.orderNo = ''
  query.status = null
  search()
}

/* ---------- 订单详情弹窗 ---------- */

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref<OrderVO | null>(null)
const detailItems = ref<OrderItemVO[]>([])

async function openDetail(row: OrderVO) {
  detailOrder.value = row
  detailItems.value = []
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailItems.value = await getOrderItems(row.id)
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  detailVisible.value = false
  detailOrder.value = null
  detailItems.value = []
}

const seatsTotal = computed(() =>
  detailItems.value.reduce((sum, it) => sum + it.price, 0),
)

onMounted(load)
watch(cinemaId, () => {
  query.orderNo = ''
  query.status = null
  search()
})
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>订单管理</h3>
      <div class="search">
        <el-input
          v-model="orderNo"
          placeholder="搜索订单号"
          clearable
          maxlength="19"
          style="width: 220px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <StatusFilter v-model="query.status" :options="STATUS_OPTIONS" @change="search" />

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="订单号" min-width="180" prop="orderNo" />
      <el-table-column label="用户" min-width="160">
        <template #default="{ row }">{{ row.userPhone || '—' }}</template>
      </el-table-column>
      <el-table-column label="影院 / 影厅" min-width="220">
        <template #default="{ row }">
          <div>{{ row.cinemaName || '—' }}</div>
          <div class="muted">{{ row.hallName || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="影片" min-width="150" prop="movieTitle" />
      <el-table-column label="场次时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="110">
        <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="STATUS_LABEL[row.status] ?? '未知'" :tone="STATUS_TONE[row.status] ?? 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="支付截止" width="150">
        <template #default="{ row }">{{ formatDateTime(row.payExpireTime) }}</template>
      </el-table-column>
      <el-table-column label="支付时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.payTime) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
      <template #empty>当前筛选下没有订单</template>
    </el-table>

    <ListPager v-model:page="page" v-model:size="size" :total="total" @change="load" />

    <el-dialog
      v-model="detailVisible"
      :title="`订单详情 — ${detailOrder?.orderNo ?? ''}`"
      width="640"
      :close-on-click-modal="false"
      @close="closeDetail"
    >
      <div v-if="detailOrder" class="detail-meta">
        <div class="meta-row"><span class="meta-key">用户</span>{{ detailOrder.userPhone || '—' }}</div>
        <div class="meta-row"><span class="meta-key">影片</span>{{ detailOrder.movieTitle }}</div>
        <div class="meta-row"><span class="meta-key">影院</span>{{ detailOrder.cinemaName || '—' }}</div>
        <div class="meta-row"><span class="meta-key">影厅</span>{{ detailOrder.hallName || '—' }}</div>
        <div class="meta-row"><span class="meta-key">场次时间</span>{{ formatDateTime(detailOrder.startTime) }}</div>
        <div class="meta-row">
          <span class="meta-key">状态</span>
          <StatusPill
            :label="STATUS_LABEL[detailOrder.status] ?? '未知'"
            :tone="STATUS_TONE[detailOrder.status] ?? 'muted'"
          />
        </div>
        <div class="meta-row"><span class="meta-key">下单时间</span>{{ formatDateTime(detailOrder.createTime) }}</div>
        <div v-if="detailOrder.payTime" class="meta-row">
          <span class="meta-key">支付时间</span>{{ formatDateTime(detailOrder.payTime) }}
        </div>
      </div>

      <el-table v-loading="detailLoading" :data="detailItems" size="small" style="margin-top: 16px">
        <el-table-column label="座位" min-width="120" prop="seatNo" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">{{ formatAmount(row.price) }}</template>
        </el-table-column>
        <el-table-column label="明细创建时间" min-width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>该订单没有座位明细</template>
      </el-table>

      <div v-if="detailItems.length" class="detail-total">
        共 <b>{{ detailItems.length }}</b> 张座位 · 小计 <b>{{ formatAmount(seatsTotal) }}</b>
      </div>

      <template #footer>
        <el-button @click="closeDetail">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.muted {
  color: var(--muted);
  font-size: 12px;
}
.detail-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
  font-size: 13px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.meta-key {
  color: var(--muted);
  min-width: 64px;
  text-align: right;
}
.detail-total {
  margin-top: 12px;
  text-align: right;
  font-size: 13px;
}
</style>
