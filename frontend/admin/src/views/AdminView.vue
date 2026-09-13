<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { createAdmin, deleteAdmin, listAdmins, resetPassword, updateAdminStatus } from '@/api/admins'
import { listCinemasSimple } from '@/api/cinemas'
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

const createVisible = ref(false)
const resetVisible = ref(false)
const resetTarget = ref<AdminManageVO | null>(null)
const saving = ref(false)

const createFormRef = ref<FormInstance>()
const resetFormRef = ref<FormInstance>()
const createForm = reactive({
  username: '',
  password: '',
  role: 0 as number,
  cinemaId: null as number | null,
})
const resetForm = reactive({ password: '' })

const createRules = computed(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 32, message: '用户名需5-32位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/,
      message: '密码需8-20位，且包含字母与数字',
      trigger: 'blur',
    },
  ],
  cinemaId:
    createForm.role === 1 ? [{ required: true, message: '请选择所属影院', trigger: 'change' }] : [],
}))

const resetRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/,
      message: '密码需8-20位，且包含字母与数字',
      trigger: 'blur',
    },
  ],
}

const cinemaName = (row: AdminManageVO) =>
  row.role === 0 ? '全部影院' : (row.cinemaName ?? '未知影院')

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
  if (!isCinemaAdmin.value) {
    try {
      cinemas.value = await listCinemasSimple()
    } catch { /* handled by http.ts */ }
  }
  load()
})

function filterRole(v: number | null) {
  roleFilter.value = v
  load()
}

function openCreate() {
  Object.assign(createForm, {
    username: '',
    password: '',
    role: isCinemaAdmin.value ? 1 : 0,
    cinemaId: isCinemaAdmin.value ? (auth.admin?.cinemaId ?? null) : null,
  })
  createFormRef.value?.clearValidate()
  createVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createAdmin({
      username: createForm.username,
      password: createForm.password,
      role: createForm.role,
      cinemaId: createForm.role === 1 ? createForm.cinemaId : 0,
    })
    ElMessage.success('已新增管理员')
    createVisible.value = false
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    saving.value = false
  }
}

function openReset(row: AdminManageVO) {
  resetTarget.value = row
  resetForm.password = ''
  resetFormRef.value?.clearValidate()
  resetVisible.value = true
}

async function submitReset() {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!resetTarget.value) return
  saving.value = true
  try {
    await resetPassword(resetTarget.value.id, { password: resetForm.password })
    ElMessage.success('密码已重置，该账号已下线')
    resetVisible.value = false
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: AdminManageVO) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}管理员「${row.username}」？${next === 0 ? '该账号将立即下线。' : ''}`, `${action}确认`, {
      type: 'warning',
      confirmButtonText: action,
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await updateAdminStatus(row.id, next)
    ElMessage.success(`已${action}`)
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
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
      <button class="f-btn" :class="{ on: roleFilter === 0 }" @click="filterRole(0)">平台管理员</button>
      <button class="f-btn" :class="{ on: roleFilter === 1 }" @click="filterRole(1)">影院管理员</button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="用户名" min-width="160" prop="username" />
      <el-table-column label="角色" width="130">
        <template #default="{ row }">
          <StatusPill
            :label="row.role === 0 ? '平台管理员' : '影院管理员'"
            :tone="row.role === 0 ? 'warn' : 'muted'"
          />
        </template>
      </el-table-column>
      <el-table-column label="所属影院" min-width="180">
        <template #default="{ row }">
          <span :class="row.role === 0 ? 'detail-text' : 'detail-strong'">
            {{ cinemaName(row) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column v-if="!isCinemaAdmin" label="操作" width="240" align="right">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" :disabled="row.id === auth.admin?.id" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="primary" @click="openReset(row)">
            重置密码
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

    <!-- 新增弹窗 -->
    <el-dialog v-model="createVisible" title="新增管理员" width="480px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" maxlength="32" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="8-20位，含字母与数字" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="createForm.role" :disabled="isCinemaAdmin">
            <el-radio :value="0">平台管理员</el-radio>
            <el-radio :value="1">影院管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.role === 1" label="所属影院" prop="cinemaId">
          <el-select v-model="createForm.cinemaId" placeholder="选择影院" :disabled="isCinemaAdmin" style="width: 100%">
            <el-option
              v-for="c in isCinemaAdmin ? [] : cinemas"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
            <el-option
              v-if="isCinemaAdmin"
              :label="cinemas.find(c => c.id === auth.admin?.cinemaId)?.name ?? `影院 #${auth.admin?.cinemaId}`"
              :value="auth.admin?.cinemaId ?? 0"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="88px">
        <el-form-item v-if="resetTarget" label="账号">
          <span>{{ resetTarget.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetForm.password" type="password" show-password placeholder="8-20位，含字母与数字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>