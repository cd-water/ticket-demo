<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { createAdmin, deleteAdmin, listAdmins, updateAdmin } from '@/api/admins'
import { listCinemas } from '@/api/cinemas'
import StatusPill from '@/components/StatusPill.vue'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/format'
import type { AdminManageVO, CinemaVO } from '@/types/api'

const auth = useAuthStore()
const isCinemaAdmin = computed(() => auth.admin?.role === 1)

const loading = ref(false)
const rows = ref<AdminManageVO[]>([])
const cinemas = ref<CinemaVO[]>([])
const roleFilter = ref<number | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  role: 0 as number,
  cinemaId: null as number | null,
})

const rules = computed(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password:
    editingId.value === null
      ? [
          { required: true, message: '请输入密码', trigger: 'blur' },
          {
            pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/,
            message: '密码需8-20位，且包含字母与数字',
            trigger: 'blur',
          },
        ]
      : [],
  cinemaId:
    form.role === 1 ? [{ required: true, message: '请选择所属影院', trigger: 'change' }] : [],
}))

const cinemaName = (id: number) => cinemas.value.find((c) => c.id === id)?.name ?? `#${id}`

async function load() {
  loading.value = true
  try {
    rows.value = await listAdmins(roleFilter.value ?? undefined)
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 仅超管加载影院列表（影院管理员调用 /api/admin/cinemas 会得 C003「无权限」，导致 toast 错误且 cinemas 仍空 → 表格 #id）
  // T10-minor #1 的彻底修复需要后端把 cinemas 接口按角色作用域（影院管理员至少能读自己那家），属后续任务
  if (!isCinemaAdmin.value) {
    try {
      const data = await listCinemas({ page: 1, size: 100 })
      cinemas.value = data.records
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    }
  }
  load()
})

function filterRole(v: number | null) {
  roleFilter.value = v
  load()
}

function openCreate() {
  editingId.value = null
  if (isCinemaAdmin.value) {
    Object.assign(form, {
      username: '',
      password: '',
      role: 1,
      cinemaId: auth.admin?.cinemaId ?? null,
    })
  } else {
    Object.assign(form, { username: '', password: '', role: 0, cinemaId: null })
  }
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: AdminManageVO) {
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    role: row.role,
    cinemaId: row.cinemaId === 0 ? null : row.cinemaId,
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value === null) {
      await createAdmin({
        username: form.username,
        password: form.password,
        role: form.role,
        cinemaId: form.role === 1 ? form.cinemaId : 0,
      })
      ElMessage.success('已新增管理员')
    } else {
      await updateAdmin(editingId.value, {
        username: form.username,
        role: form.role,
        cinemaId: form.role === 1 ? form.cinemaId : 0,
        status: rows.value.find((r) => r.id === editingId.value)?.status ?? 1,
      })
      ElMessage.success('已保存')
    }
    dialogVisible.value = false
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出（C201 用户名已存在 / C202 / C203） */
  } finally {
    saving.value = false
  }
}

async function remove(row: AdminManageVO) {
  try {
    await ElMessageBox.confirm(`删除管理员「${row.username}」？该账号将立即失效。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  try {
    await deleteAdmin(row.id)
    ElMessage.success('已删除')
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  }
}
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>管理员管理</h3>
      <el-button type="primary" style="margin-left: auto" @click="openCreate">＋ 新增管理员</el-button>
    </div>

    <div class="filters">
      <button class="f-btn" :class="{ on: roleFilter === null }" @click="filterRole(null)">全部</button>
      <button class="f-btn" :class="{ on: roleFilter === 0 }" @click="filterRole(0)">超级管理员</button>
      <button class="f-btn" :class="{ on: roleFilter === 1 }" @click="filterRole(1)">影院管理员</button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="用户名" min-width="160" prop="username" />
      <el-table-column label="角色" width="130">
        <template #default="{ row }">
          <StatusPill
            :label="row.role === 0 ? '超级管理员' : '影院管理员'"
            :tone="row.role === 0 ? 'warn' : 'muted'"
          />
        </template>
      </el-table-column>
      <el-table-column label="所属影院" min-width="180">
        <template #default="{ row }">
          <span :class="row.role === 0 ? 'detail-text' : 'detail-strong'">
            {{ row.role === 0 ? '全部影院' : cinemaName(row.cinemaId) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '启用' : '禁用'" :tone="row.status === 1 ? 'ok' : 'danger'" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column v-if="!isCinemaAdmin" label="操作" width="150" align="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="row.id === auth.admin?.id"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            :disabled="row.id === auth.admin?.id"
            @click="remove(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>暂无管理员</template>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? '新增管理员' : '编辑管理员'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" maxlength="32" :disabled="editingId !== null" />
        </el-form-item>
        <el-form-item v-if="editingId === null" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="8-20位，含字母与数字" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role" :disabled="isCinemaAdmin || editingId !== null">
            <el-radio :value="0">超级管理员</el-radio>
            <el-radio :value="1">影院管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.role === 1" label="所属影院" prop="cinemaId">
          <el-select
            v-model="form.cinemaId"
            placeholder="选择影院"
            :disabled="isCinemaAdmin"
            style="width: 100%"
          >
            <el-option
              v-for="c in isCinemaAdmin ? [] : cinemas"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
            <el-option
              v-if="isCinemaAdmin"
              :label="cinemaName(auth.admin?.cinemaId ?? 0)"
              :value="auth.admin?.cinemaId ?? 0"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
