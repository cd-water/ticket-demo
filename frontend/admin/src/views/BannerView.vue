<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { createBanner, deleteBanner, listBanners, updateBanner } from '@/api/banners'
import { uploadImage } from '@/api/files'
import StatusPill from '@/components/StatusPill.vue'
import type { BannerVO } from '@/types/api'

const loading = ref(false)
const rows = ref<BannerVO[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const uploading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ image: '', linkUrl: '', sort: 0, status: 1 })

const rules = {
  image: [{ required: true, message: '请上传轮播图片', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    rows.value = await listBanners()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    loading.value = false
  }
}

onMounted(load)

function openCreate() {
  editingId.value = null
  Object.assign(form, { image: '', linkUrl: '', sort: 0, status: 1 })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: BannerVO) {
  editingId.value = row.id
  Object.assign(form, {
    image: row.image,
    linkUrl: row.linkUrl ?? '',
    sort: row.sort,
    status: row.status,
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function onUpload(file: File) {
  uploading.value = true
  uploadImage(file)
    .then(({ url }) => {
      form.image = url
      formRef.value?.validateField('image').catch(() => undefined)
      ElMessage.success('图片已上传')
    })
    .catch(() => {
      /* 错误提示已由 http.ts 统一弹出 */
    })
    .finally(() => {
      uploading.value = false
    })
  return false
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  const body = { image: form.image, linkUrl: form.linkUrl, sort: form.sort, status: form.status }
  try {
    if (editingId.value === null) {
      await createBanner(body)
      ElMessage.success('已新增')
    } else {
      await updateBanner(editingId.value, body)
      ElMessage.success('已保存')
    }
    dialogVisible.value = false
    load()
  } catch {
    /* 错误提示已由 http.ts 统一弹出 */
  } finally {
    saving.value = false
  }
}

async function remove(row: BannerVO) {
  try {
    await ElMessageBox.confirm('删除这张轮播图？', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  try {
    await deleteBanner(row.id)
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
      <h3>轮播图管理</h3>
      <el-button type="primary" style="margin-left: auto" @click="openCreate">＋ 新增轮播图</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <img v-if="row.image" class="thumb-wide" :src="row.image" alt="轮播图" />
          <span v-else class="thumb-wide" />
        </template>
      </el-table-column>
      <el-table-column label="跳转链接" min-width="200">
        <template #default="{ row }">{{ row.linkUrl || '—' }}</template>
      </el-table-column>
      <el-table-column label="排序" width="90" prop="sort" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '上线' : '下线'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无轮播图，点右上角「新增轮播图」开始</template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增轮播图' : '编辑轮播图'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="图片" prop="image">
          <el-upload :show-file-list="false" accept="image/*" :disabled="uploading" :before-upload="onUpload">
            <img v-if="form.image" class="upload-preview wide" :src="form.image" alt="轮播图预览" />
            <div v-else class="upload-slot wide">{{ uploading ? '上传中…' : '＋ 上传图片' }}</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" maxlength="255" placeholder="如 /movies/1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" :value-on-clear="0" />
          <span class="hint">数字小者靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上线</el-radio>
            <el-radio :value="0">下线</el-radio>
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
