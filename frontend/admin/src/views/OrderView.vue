<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listOrders } from '@/api/orders'
import StatusPill from '@/components/StatusPill.vue'
import { formatAmount, formatDateTime } from '@/utils/format'
import type { OrderAdminRecord } from '@/types/api'

const loading = ref(false)
const rows = ref<OrderAdminRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ orderNo: '', status: null as number | null })

const STATUS_TONE: Record<number, 'warn' | 'ok' | 'muted'> = { 0: 'warn', 1: 'ok', 2: 'muted' }
const STATUS_LABEL: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已取消' }

async function load() {
  loading.value = true
  try {
    const data = await listOrders({
      page: page.value,
      size: size.value,
      orderNo: query.orderNo || undefined,
      status: query.status ?? undefined,
    })
    rows.value = data.records
    total.value = data.total
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    loading.value = false
  }
}

onMounted(load)

function search() {
  page.value = 1
  load()
}

function reset() {
  query.orderNo = ''
  query.status = null
  search()
}

function filterStatus(v: number | null) {
  query.status = v
  search()
}

function onPage(p: number) {
  page.value = p
  load()
}

function onSize(s: number) {
  size.value = s
  page.value = 1
  load()
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>订单管理</h3>
      <div class="search">
        <el-input
          v-model="query.orderNo"
          placeholder="搜索订单号"
          clearable
          style="width: 220px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <div class="filters">
      <button class="f-btn" :class="{ on: query.status === null }" @click="filterStatus(null)">全部</button>
      <button class="f-btn" :class="{ on: query.status === 0 }" @click="filterStatus(0)">待支付</button>
      <button class="f-btn" :class="{ on: query.status === 1 }" @click="filterStatus(1)">已支付</button>
      <button class="f-btn" :class="{ on: query.status === 2 }" @click="filterStatus(2)">已取消</button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="订单号" min-width="180" prop="orderNo" />
      <el-table-column label="用户" min-width="160">
        <template #default="{ row }">
          {{ row.userNickname || '—' }}
          <span class="detail-text">{{ row.userPhone || '' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="影片" min-width="150" prop="movieTitle" />
      <el-table-column label="金额" width="110">
        <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="STATUS_LABEL[row.status] ?? '未知'" :tone="STATUS_TONE[row.status] ?? 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="支付截止" width="150">
        <template #default="{ row }">{{ formatDateTime(row.payExpireTime) }}</template>
      </el-table-column>
      <template #empty>当前筛选下没有订单</template>
    </el-table>

    <div class="pager">
      <span class="total">共 {{ total }} 条</span>
      <el-pagination
        layout="sizes, prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        @current-change="onPage"
        @size-change="onSize"
      />
    </div>
  </div>
</template>
