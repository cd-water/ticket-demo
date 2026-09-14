<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { listMovies, saveMovie } from '@/api/movies'
import ImageUpload from '@/components/ImageUpload.vue'
import ListPager from '@/components/ListPager.vue'
import StatusFilter from '@/components/StatusFilter.vue'
import StatusPill from '@/components/StatusPill.vue'
import { useFormDialog } from '@/composables/useFormDialog'
import { usePagedList } from '@/composables/useList'
import { formatDateTime } from '@/utils/format'
import type { MovieVO } from '@/types/api'

const STATUS_OPTIONS = [
  { label: '全部', value: null },
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const query = reactive({ title: '', status: null as number | null })

const { rows, total, loading, page, size, load, search } = usePagedList<MovieVO>((p, s) =>
  listMovies({
    page: p,
    size: s,
    title: query.title || undefined,
    status: query.status ?? undefined,
  }),
)

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

const formRef = ref<FormInstance>()

const { visible, editingId, saving, openCreate, openEdit, submit } = useFormDialog<
  typeof form,
  MovieVO
>({
  formRef,
  form,
  toForm: (row) =>
    row
      ? {
          title: row.title,
          poster: row.poster,
          description: row.description,
          duration: row.duration,
          releaseDate: row.releaseDate,
          status: row.status,
        }
      : { title: '', poster: '', description: '', duration: 90, releaseDate: '', status: 1 },
  save: (f, id) => saveMovie({ ...f, id }),
  onSaved: load,
})

/** 上传是程序化写 model，不会触发 el-form-item 的校验，手动补一次 */
function onUploaded() {
  formRef.value?.validateField('poster').catch(() => undefined)
}

function reset() {
  query.title = ''
  query.status = null
  search()
}

onMounted(load)
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
          maxlength="100"
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新增电影</el-button>
    </div>

    <StatusFilter v-model="query.status" :options="STATUS_OPTIONS" @change="search" />

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

    <ListPager v-model:page="page" v-model:size="size" :total="total" @change="load" />

    <el-dialog v-model="visible" :title="editingId === null ? '新增电影' : '编辑电影'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="片名" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="海报" prop="poster">
          <ImageUpload
            v-model="form.poster"
            placeholder="＋ 上传海报"
            alt="海报预览"
            @uploaded="onUploaded"
          />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1024" />
        </el-form-item>
        <el-form-item label="时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" />
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
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
