<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { getSeatGrid, listHallsByCinema, saveHall, saveSeatGrid } from '@/api/halls'
import SeatCanvas from '@/components/SeatCanvas.vue'
import StatusPill from '@/components/StatusPill.vue'
import type { HallVO, SeatCellVO } from '@/types/api'

const route = useRoute()
const cinemaId = Number(route.params.cinemaId)

const halls = ref<HallVO[]>([])
const current = ref<HallVO | null>(null)
const seats = ref<SeatCellVO[]>([])
const grid = reactive({ rows: 0, cols: 0 })
const loadingSeats = ref(false)
const dirty = ref(false)
const savingSeats = ref(false)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', seatRows: 8, seatCols: 10, status: 1 })

const rules = {
  name: [{ required: true, message: '请输入影厅名称', trigger: 'blur' }],
}

async function loadSeats(hallId: number) {
  loadingSeats.value = true
  try {
    const data = await getSeatGrid(hallId)
    grid.rows = data.rows
    grid.cols = data.cols
    seats.value = data.seats
    dirty.value = false
  } catch {
    /* 错误由 http.ts 统一弹 */
  } finally {
    loadingSeats.value = false
  }
}

async function loadHalls(selectId?: number) {
  try {
    halls.value = await listHallsByCinema(cinemaId)
    const target =
      (selectId !== undefined ? halls.value.find((h) => h.id === selectId) : undefined) ?? halls.value[0]
    if (target) {
      current.value = target
      await loadSeats(target.id)
    } else {
      current.value = null
      seats.value = []
      grid.rows = 0
      grid.cols = 0
      dirty.value = false
    }
  } catch {
    /* 错误由 http.ts 统一弹 */
  }
}

onMounted(() => loadHalls())
watch(() => route.params.cinemaId, () => loadHalls())

async function selectHall(hall: HallVO) {
  if (current.value?.id === hall.id) return
  if (dirty.value) {
    try {
      await ElMessageBox.confirm('有未保存的座位改动，切换影厅后将丢失。', '未保存的改动', {
        type: 'warning',
        confirmButtonText: '放弃改动',
        cancelButtonText: '继续编辑',
      })
    } catch {
      return
    }
  }
  current.value = hall
  await loadSeats(hall.id)
}

function toggleSeat(row: number, col: number) {
  const seat = seats.value.find((s) => s.row === row && s.col === col)
  if (!seat) return
  seat.status = seat.status === 1 ? 0 : 1
  dirty.value = true
}

async function saveSeats() {
  if (!current.value) return
  savingSeats.value = true
  try {
    await saveSeatGrid(
      current.value.id,
      seats.value.map((s) => ({ row: s.row, col: s.col, status: s.status })),
    )
    ElMessage.success('座位模板已保存')
    await loadSeats(current.value.id)
  } catch {
    /* 错误由 http.ts 统一弹 */
  } finally {
    savingSeats.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { name: '', seatRows: 8, seatCols: 10, status: 1 })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(hall: HallVO) {
  editingId.value = hall.id
  Object.assign(form, { name: hall.name, seatRows: hall.seatRows, seatCols: hall.seatCols, status: hall.status })
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function editCurrent() {
  if (current.value) openEdit(current.value)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await saveHall({ ...form, cinemaId, id: editingId.value })
    dialogVisible.value = false
    if (editingId.value === null) {
      ElMessage.success('已新增影厅')
      halls.value = await listHallsByCinema(cinemaId)
      const newest = halls.value.reduce<HallVO | null>((a, b) => (!a || b.id > a.id ? b : a), null)
      if (newest) {
        current.value = newest
        await loadSeats(newest.id)
      }
    } else {
      ElMessage.success('已保存')
      await loadHalls(editingId.value)
    }
  } catch {
    /* 错误由 http.ts 统一弹 */
  } finally {
    saving.value = false
  }
}

</script>

<template>
  <div class="seat-editor">
    <div class="hall-list">
      <h3>影厅列表</h3>
      <div
        v-for="h in halls"
        :key="h.id"
        class="hall-row"
        :class="{ on: current?.id === h.id }"
        @click="selectHall(h)"
      >
        <span>{{ h.name }}</span>
        <span class="meta">{{ h.seatRows }}×{{ h.seatCols }}</span>
        <StatusPill :label="h.status === 1 ? '启用' : '禁用'" :tone="h.status === 1 ? 'ok' : 'muted'" />
      </div>
      <p v-if="!halls.length" class="hall-empty">本影院暂无影厅</p>

      <div class="hall-actions">
        <el-button size="small" :disabled="!current" @click="editCurrent">编辑影厅</el-button>
      </div>
      <button class="hall-add" type="button" @click="openCreate">＋ 新增影厅</button>
    </div>

    <div class="canvas-panel">
      <h3>{{ current ? `${current.name} · 座位模板` : '座位模板' }}</h3>
      <div v-if="current" v-loading="loadingSeats" class="canvas-body">
        <SeatCanvas :rows="grid.rows" :cols="grid.cols" :seats="seats" @toggle="toggleSeat" />
        <div class="legend">
          <span><i class="lg lg-on" />启用</span>
          <span><i class="lg lg-off" />禁用</span>
        </div>
        <el-button type="primary" :loading="savingSeats" :disabled="!dirty" @click="saveSeats">
          {{ dirty ? '保存座位模板' : '座位模板已保存' }}
        </el-button>
        <p class="canvas-hint">点击座位切换「启用 / 禁用」，改完点保存生效。</p>
      </div>
      <p v-else class="canvas-hint">先在左侧新增影厅。</p>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId === null ? '新增影厅' : '编辑影厅'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="影厅名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="如 1号厅" />
        </el-form-item>
        <el-form-item label="座位排数">
          <el-input-number v-model="form.seatRows" :min="1" :max="26" :value-on-clear="8" />
        </el-form-item>
        <el-form-item label="每排座位">
          <el-input-number v-model="form.seatCols" :min="1" :max="26" :value-on-clear="10" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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

<style scoped>
.seat-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.hall-list,
.canvas-panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 16px;
}

.hall-list h3,
.canvas-panel h3 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
}

.hall-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}

.hall-row:hover {
  background: var(--panel-2);
}

.hall-row.on {
  background: rgba(255, 195, 0, 0.1);
  color: var(--brand);
}

.hall-row .meta {
  color: var(--ink-2);
}

.hall-empty {
  margin: 8px 0;
  font-size: 13px;
  color: var(--ink-2);
}

.hall-actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

.hall-add {
  margin-top: 12px;
  width: 100%;
  padding: 9px;
  border: 1px dashed var(--line);
  border-radius: 8px;
  background: none;
  font-family: inherit;
  font-size: 12px;
  color: var(--ink-2);
  cursor: pointer;
}

.hall-add:hover {
  color: var(--brand);
  border-color: var(--brand);
}

.canvas-body {
  text-align: center;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 18px;
  margin: 16px 0 12px;
  font-size: 12px;
  color: var(--ink-2);
}

.legend .lg {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: -1px;
}

.legend .lg-on {
  background: #3a3a3a;
}

.legend .lg-off {
  background: var(--brand);
}

.canvas-hint {
  margin: 10px 0 0;
  font-size: 11px;
  color: var(--ink-2);
}

@media (max-width: 900px) {
  .seat-editor {
    grid-template-columns: 1fr;
  }
}
</style>
