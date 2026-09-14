<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { listHallsByCinema } from '@/api/halls'
import { deleteScreening, listMovieOptions, listScreeningsByCinema, saveScreening } from '@/api/screenings'
import StatusPill from '@/components/StatusPill.vue'
import { formatDateTime } from '@/utils/format'
import type { HallVO, MovieOption, ScreeningVO } from '@/types/api'

const route = useRoute()
const cinemaId = Number(route.params.cinemaId)

const loading = ref(false)
const rows = ref<ScreeningVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ movieId: null as number | null })

const movies = ref<MovieOption[]>([])
const halls = ref<HallVO[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  movieId: null as number | null,
  hallId: null as number | null,
  startTime: '',
  price: 45,
})

const rules = {
  movieId: [{ required: true, message: '请选择电影', trigger: 'change' }],
  hallId: [{ required: true, message: '请选择影厅', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开场时间', trigger: 'change' }],
}

async function load() {
  loading.value = true
  try {
    const data = await listScreeningsByCinema({
      cinemaId,
      page: page.value,
      size: size.value,
      movieId: query.movieId ?? undefined,
    })
    rows.value = data.records
    total.value = data.total
  } catch {
    /* 错误由 http.ts 统一弹 */
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [opts, hallList] = await Promise.all([listMovieOptions(), listHallsByCinema(cinemaId)])
    movies.value = opts
    halls.value = hallList
  } catch {
    /* 错误由 http.ts 统一弹 */
  }
  load()
})

watch(() => route.params.cinemaId, () => load())

function filterMovies(v: number | null) {
  query.movieId = v
  page.value = 1
  load()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { movieId: null, hallId: null, startTime: '', price: 45 })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: ScreeningVO) {
  if (row.status === 1) {
    ElMessage.warning('已开场的排场不能修改')
    return
  }
  editingId.value = row.id
  Object.assign(form, {
    movieId: row.movieId,
    hallId: row.hallId,
    startTime: row.startTime,
    price: row.price,
  })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  const body = {
    id: editingId.value,
    cinemaId,
    movieId: form.movieId as number,
    hallId: form.hallId as number,
    startTime: form.startTime,
    price: form.price,
  }
  try {
    await saveScreening(body)
    ElMessage.success(editingId.value === null ? '已新增排场' : '已保存')
    dialogVisible.value = false
    load()
  } catch {
    /* 错误由 http.ts 统一弹（含 C501 时间冲突） */
  } finally {
    saving.value = false
  }
}

async function remove(row: ScreeningVO) {
  if (row.status === 1) {
    ElMessage.warning('已开场的排场不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(
      `删除「${row.movieTitle}」${formatDateTime(row.startTime)} 的排场？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }
  try {
    await deleteScreening(row.id)
    ElMessage.success('已删除')
    load()
  } catch {
    /* 错误由 http.ts 统一弹 */
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
      <h3>排场管理</h3>
      <div class="search">
        <el-select
          v-model="query.movieId"
          placeholder="全部电影"
          clearable
          style="width: 200px"
          @change="filterMovies"
        >
          <el-option v-for="m in movies" :key="m.id" :label="m.title" :value="m.id" />
        </el-select>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新增排场</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="影片" min-width="180" prop="movieTitle" />
      <el-table-column label="影厅" width="140" prop="hallName" />
      <el-table-column label="开场时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="票价" width="110">
        <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '已开场' : '未开始'" :tone="row.status === 1 ? 'muted' : 'ok'" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 1" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.status === 1" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无排场，点右上角「新增排场」开始</template>
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

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增排场' : '编辑排场'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="电影" prop="movieId">
          <el-select v-model="form.movieId" placeholder="选择电影" style="width: 100%">
            <el-option v-for="m in movies" :key="m.id" :label="m.title" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="影厅" prop="hallId">
          <el-select v-model="form.hallId" placeholder="选择影厅" style="width: 100%">
            <el-option v-for="h in halls" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开场时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择开场时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="form.price" :min="0.01" :max="999" :precision="2" :step="5" :value-on-clear="45" />
          <span class="hint">元</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.panel-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.panel-head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.panel-head .search {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
}

.pager .total {
  font-size: 12px;
  color: var(--ink-2);
}

.hint {
  margin-left: 8px;
  color: var(--ink-2);
  font-size: 12px;
}
</style>
