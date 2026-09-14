<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { listMovies, saveMovie } from '@/api/movies'
import { uploadImage } from '@/api/files'
import StatusPill from '@/components/StatusPill.vue'
import { formatDateTime } from '@/utils/format'
import type { MovieVO } from '@/types/api'

const loading = ref(false)
const rows = ref<MovieVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ title: '', status: null as number | null })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const uploading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  poster: '',
  description: '',
  duration: 90,
  releaseDate: '',
  status: 1,
})

const rules = {
  title: [{ required: true, message: '请输入片名', trigger: 'blur' }],
  poster: [{ required: true, message: '请上传海报', trigger: 'change' }],
  description: [{ required: true, message: '请输入简介', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  releaseDate: [{ required: true, message: '请选择上映日期', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listMovies({
      page: page.value,
      size: size.value,
      title: query.title || undefined,
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
  query.title = ''
  query.status = null
  search()
}

function filterStatus(v: number | null) {
  query.status = v
  search()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', poster: '', description: '', duration: 90, releaseDate: '', status: 1 })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: MovieVO) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    poster: row.poster ?? '',
    description: row.description ?? '',
    duration: row.duration,
    releaseDate: row.releaseDate ?? '',
    status: row.status,
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function onUpload(file: File) {
  uploading.value = true
  uploadImage(file)
    .then((url) => {
      form.poster = url
      formRef.value?.validateField('poster').catch(() => undefined)
      ElMessage.success('海报已上传')
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
  const body = {
    title: form.title,
    poster: form.poster || null,
    description: form.description || null,
    duration: form.duration,
    releaseDate: form.releaseDate || null,
    status: form.status,
  }
  try {
    await saveMovie({ ...body, id: editingId.value })
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
      <h3>电影管理</h3>
      <div class="search">
        <el-input
          v-model="query.title"
          placeholder="搜索片名"
          clearable
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新增电影</el-button>
    </div>

    <div class="filters">
      <button class="f-btn" :class="{ on: query.status === null }" @click="filterStatus(null)">全部</button>
      <button class="f-btn" :class="{ on: query.status === 1 }" @click="filterStatus(1)">上架</button>
      <button class="f-btn" :class="{ on: query.status === 0 }" @click="filterStatus(0)">下架</button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="海报" width="90">
        <template #default="{ row }">
          <img v-if="row.poster" class="thumb" :src="row.poster" :alt="row.title" />
          <span v-else class="thumb" />
        </template>
      </el-table-column>
      <el-table-column label="片名" prop="title" min-width="180" />
      <el-table-column label="简介" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ row.description || '—' }}</template>
      </el-table-column>
      <el-table-column label="时长" width="110">
        <template #default="{ row }">{{ row.duration }} 分钟</template>
      </el-table-column>
      <el-table-column label="上映日期" width="130">
        <template #default="{ row }">{{ row.releaseDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '上架' : '下架'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无电影，点右上角「新增电影」开始</template>
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

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增电影' : '编辑电影'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="片名" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="海报" prop="poster">
          <el-upload :show-file-list="false" accept="image/*" :disabled="uploading" :before-upload="onUpload">
            <img v-if="form.poster" class="upload-preview" :src="form.poster" alt="海报预览" />
            <div v-else class="upload-slot">{{ uploading ? '上传中…' : '＋ 上传海报' }}</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1024" />
        </el-form-item>
        <el-form-item label="时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="600" />
          <span class="hint">分钟</span>
        </el-form-item>
        <el-form-item label="上映日期" prop="releaseDate">
          <el-date-picker
            v-model="form.releaseDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择上映日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
