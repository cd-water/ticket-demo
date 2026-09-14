<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, toggleUserStatus } from '@/api/users'
import ListPager from '@/components/ListPager.vue'
import StatusFilter from '@/components/StatusFilter.vue'
import StatusPill from '@/components/StatusPill.vue'
import { usePagedList } from '@/composables/useList'
import { formatDateTime } from '@/utils/format'
import type { UserVO } from '@/types/api'

const STATUS_OPTIONS = [
  { label: '全部', value: null },
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 },
]

const query = reactive({ phone: '', status: null as number | null })

const { rows, total, loading, page, size, load, search } = usePagedList<UserVO>((p, s) =>
  listUsers({
    page: p,
    size: s,
    phone: query.phone || undefined,
    status: query.status ?? undefined,
  }),
)

async function toggleStatus(row: UserVO) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}用户 ${row.nickname}（${row.phone}）？`, `${action}确认`, {
      type: 'warning',
      confirmButtonText: action,
      cancelButtonText: '取消',
      confirmButtonClass: next === 1 ? '' : 'el-button--danger',
    })
  } catch {
    return
  }
  try {
    await toggleUserStatus(row.id, next)
    ElMessage.success(`已${action}`)
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  }
}

function reset() {
  query.phone = ''
  query.status = null
  search()
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>用户管理</h3>
      <div class="search">
        <el-input
          v-model="query.phone"
          placeholder="搜索手机号"
          clearable
          maxlength="20"
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <StatusFilter v-model="query.status" :options="STATUS_OPTIONS" @change="search" />

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="手机号" min-width="150" prop="phone" />
      <el-table-column label="昵称" min-width="150" prop="nickname" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '正常' : '禁用'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="right">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
      <template #empty>没有符合条件的用户</template>
    </el-table>

    <ListPager v-model:page="page" v-model:size="size" :total="total" @change="load" />
  </div>
</template>
