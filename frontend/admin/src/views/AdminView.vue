<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { createAdmin, listAdmins, resetPassword, updateAdminStatus } from '@/api/admins'
import StatusPill from '@/components/StatusPill.vue'
import { useFormDialog } from '@/composables/useFormDialog'
import { useList } from '@/composables/useList'
import { formatDateTime } from '@/utils/format'
import { passwordRules, usernameRules } from '@/utils/rules'
import type { AdminManageVO } from '@/types/api'

const { rows, loading, load } = useList<AdminManageVO>(listAdmins)

/* ---- 新增管理员 ---- */

const createForm = reactive({ username: '', password: '' })
const createFormRef = ref<FormInstance>()
const createRules = { username: usernameRules, password: passwordRules() }

const {
  visible: createVisible,
  saving: createSaving,
  openCreate,
  submit: submitCreate,
} = useFormDialog<typeof createForm, AdminManageVO>({
  formRef: createFormRef,
  form: createForm,
  toForm: () => ({ username: '', password: '' }),
  save: (form) => createAdmin(form),
  messages: { created: '已新增管理员' },
  onSaved: load,
})

/* ---- 重置密码 ---- */

const resetForm = reactive({ password: '' })
const resetFormRef = ref<FormInstance>()
const resetTarget = ref<AdminManageVO | null>(null)
const resetRules = { password: passwordRules('请输入新密码') }

const {
  visible: resetVisible,
  saving: resetSaving,
  openEdit: openReset,
  submit: submitReset,
} = useFormDialog<typeof resetForm, AdminManageVO>({
  formRef: resetFormRef,
  form: resetForm,
  toForm: () => ({ password: '' }),
  // 重置弹窗只由 openEdit 打开，id 不会为 null
  save: (form, id) => (id === null ? Promise.resolve() : resetPassword(id, { password: form.password })),
  messages: { updated: '密码已重置，该账号已下线' },
  onSaved: load,
})

function openResetDialog(row: AdminManageVO) {
  resetTarget.value = row
  openReset(row)
}

async function toggleStatus(row: AdminManageVO) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确认${action}管理员「${row.username}」？${next === 0 ? '该账号将立即下线。' : ''}`,
      `${action}确认`,
      { type: 'warning', confirmButtonText: action, cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await updateAdminStatus(row.id, next)
    ElMessage.success(`已${action}`)
    load()
  } catch {
    /* 错误由 http.ts 统一弹 */
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>管理员管理</h3>
      <el-button type="primary" style="margin-left: auto" @click="openCreate">＋ 新增管理员</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="用户名" min-width="160" prop="username" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '启用' : '禁用'" :tone="row.status === 1 ? 'ok' : 'muted'" />
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
          <!-- 后端不允许操作当前登录的管理员自己（403），但仍给到按钮，由后端判定 -->
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="primary" @click="openResetDialog(row)">重置密码</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无管理员</template>
    </el-table>

    <el-dialog v-model="createVisible" title="新增管理员" width="420px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" maxlength="32" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSaving" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="88px">
        <el-form-item v-if="resetTarget" label="账号">
          <span>{{ resetTarget.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetForm.password" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetSaving" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>
