<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { listCinemas, saveCinema } from '@/api/cinemas'
import StatusPill from '@/components/StatusPill.vue'
import { formatDateTime } from '@/utils/format'
import type { CinemaVO } from '@/types/api'

const router = useRouter()
const loading = ref(false)
const rows = ref<CinemaVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ name: '', status: null as number | null })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', address: '', status: 1 })

const rules = {
  name: [{ required: true, message: '请输入影院名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入影院地址', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listCinemas({
      page: page.value,
      size: size.value,
      name: query.name || undefined,
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
  query.name = ''
  query.status = null
  search()
}

function filterStatus(v: number | null) {
  query.status = v
  search()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', address: '', status: 1 })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: CinemaVO) {
  editingId.value = row.id
  Object.assign(form, { name: row.name, address: row.address, status: row.status })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function enterCinema(row: CinemaVO) {
  router.push(`/cinemas/${row.id}/halls`)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await saveCinema({ ...form, id: editingId.value })
    ElMessage.success(editingId.value === null ? '已新增' : '已保存')
    dialogVisible.value = false
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    saving.value = false
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
      <h3>影院管理</h3>
      <div class="search">
        <el-input
          v-model="query.name"
          placeholder="搜索影院名称"
          clearable
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新增影院</el-button>
    </div>

    <div class="filters">
      <button class="f-btn" :class="{ on: query.status === null }" @click="filterStatus(null)">全部</button>
      <button class="f-btn" :class="{ on: query.status === 1 }" @click="filterStatus(1)">营业</button>
      <button class="f-btn" :class="{ on: query.status === 0 }" @click="filterStatus(0)">停业</button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="影院名称" min-width="200" prop="name" />
      <el-table-column label="地址" min-width="280" prop="address" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '营业' : '停业'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="enterCinema(row)">进入</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无影院，点右上角「新增影院」开始</template>
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

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增影院' : '编辑影院'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="影院名称" prop="name">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" maxlength="255" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">营业</el-radio>
            <el-radio :value="0">停业</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
