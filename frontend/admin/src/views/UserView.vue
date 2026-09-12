<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus } from '@/api/users'
import StatusPill from '@/components/StatusPill.vue'
import { formatDateTime } from '@/utils/format'
import type { UserAdminVO } from '@/types/api'

const loading = ref(false)
const rows = ref<UserAdminVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ phone: '' })

async function load() {
  loading.value = true
  try {
    const data = await listUsers({ page: page.value, size: size.value, phone: query.phone || undefined })
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
  query.phone = ''
  search()
}

async function toggleStatus(row: UserAdminVO) {
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
    await updateUserStatus(row.id, next)
    ElMessage.success(`已${action}`)
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  }
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
      <h3>用户管理</h3>
      <div class="search">
        <el-input
          v-model="query.phone"
          placeholder="搜索手机号"
          clearable
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="手机号" min-width="150" prop="phone" />
      <el-table-column label="昵称" min-width="150" prop="nickname" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '正常' : '禁用'" :tone="row.status === 1 ? 'ok' : 'danger'" />
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
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
