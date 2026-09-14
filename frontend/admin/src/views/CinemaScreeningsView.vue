<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { listHallsByCinema } from '@/api/halls'
import { listMovieOptions, listScreeningsByCinema, saveScreening } from '@/api/screenings'
import ListPager from '@/components/ListPager.vue'
import StatusPill from '@/components/StatusPill.vue'
import { useCinemaId } from '@/composables/useCinemaId'
import { useFormDialog } from '@/composables/useFormDialog'
import { usePagedList } from '@/composables/useList'
import { formatAmount, formatDateTime } from '@/utils/format'
import type { HallVO, MovieOption, ScreeningVO } from '@/types/api'

const cinemaId = useCinemaId()
const query = reactive({ movieId: null as number | null })
const movies = ref<MovieOption[]>([])
const halls = ref<HallVO[]>([])

const { rows, total, loading, page, size, load, search } = usePagedList<ScreeningVO>((p, s) =>
  listScreeningsByCinema(cinemaId.value, {
    page: p,
    size: s,
    movieId: query.movieId ?? undefined,
  }),
)

const form = reactive({
  movieId: null as number | null,
  hallId: null as number | null,
  startTime: '',
  price: 45,
})
const formRef = ref<FormInstance>()

const rules = {
  movieId: [{ required: true, message: '请选择电影', trigger: 'change' }],
  hallId: [{ required: true, message: '请选择影厅', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开场时间', trigger: 'change' }],
}

const { visible, editingId, saving, openCreate, openEdit, submit: submitDialog } = useFormDialog<
  typeof form,
  ScreeningVO
>({
  formRef,
  form,
  toForm: (row) =>
    row
      ? { movieId: row.movieId, hallId: row.hallId, startTime: row.startTime, price: row.price }
      : { movieId: null, hallId: null, startTime: '', price: 45 },
  save: (f, id) => {
    // 表单规则已保证二者非空
    if (f.movieId === null || f.hallId === null) return Promise.resolve()
    return saveScreening({ id, movieId: f.movieId, hallId: f.hallId, startTime: f.startTime, price: f.price })
  },
  messages: { created: '已新增排场' },
  onSaved: load,
})

/** 是否已开场（前端按 startTime 计算，后端 409 兜底） */
function started(row: ScreeningVO) {
  // 后端的 "yyyy-MM-dd HH:mm:ss" 不是 ES 规范的 Date 字面量，Safari 直接 new 会得到 Invalid Date
  return new Date(row.startTime.replace(' ', 'T')).getTime() < Date.now()
}

async function submit() {
  // 后端「开场时间必须晚于当前时间」的前置校验，放在提交时判以免选完时间后过期
  if (new Date(form.startTime.replace(' ', 'T')).getTime() <= Date.now()) {
    ElMessage.warning('开场时间必须晚于当前时间')
    return
  }
  await submitDialog()
}

/** 影片下拉是全量的，影厅下拉跟随当前影院，切影院时要一起刷新 */
async function loadOptions() {
  try {
    const [opts, hallList] = await Promise.all([
      listMovieOptions(),
      listHallsByCinema(cinemaId.value),
    ])
    movies.value = opts
    halls.value = hallList
  } catch {
    /* 错误由 http.ts 统一弹 */
  }
}

onMounted(() => {
  loadOptions()
  load()
})

watch(cinemaId, () => {
  query.movieId = null
  loadOptions()
  search()
})
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
          @change="search"
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
        <template #default="{ row }">{{ formatAmount(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <StatusPill :label="started(row) ? '已开场' : '未开始'" :tone="started(row) ? 'muted' : 'ok'" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="started(row)" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无排场，点右上角「新增排场」开始</template>
    </el-table>

    <ListPager v-model:page="page" v-model:size="size" :total="total" @change="load" />

    <el-dialog v-model="visible" :title="editingId === null ? '新增排场' : '编辑排场'" width="520px">
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
          <!-- 线格式是 "yyyy-MM-dd HH:mm:ss"（空格），后端 @JsonFormat 不接受 T 分隔 -->
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开场时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="票价">
          <!-- 上限取 DECIMAL(10,2) 的容量，不再自造 999 -->
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :max="99999999.99"
            :precision="2"
            :step="5"
            :value-on-clear="45"
          />
          <span class="hint">元</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
