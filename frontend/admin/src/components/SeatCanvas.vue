<script setup lang="ts">
import type { SeatCellVO } from '@/types/api'

defineProps<{ rows: number; cols: number; seats: SeatCellVO[] }>()
const emit = defineEmits<{ toggle: [row: number, col: number] }>()

/** 座位按钮没有可见文字，title 与 aria-label 共用同一份描述 */
function seatLabel(seat: SeatCellVO) {
  return `${seat.seatNo} ${seat.status === 1 ? '启用' : '禁用'}`
}
</script>

<template>
  <div class="canvas">
    <div class="mini-screen">银幕</div>
    <div
      class="seat-canvas"
      :aria-label="`座位图 ${rows}排${cols}列`"
      :style="{ gridTemplateColumns: `repeat(${cols}, 26px)` }"
    >
      <button
        v-for="s in seats"
        :key="`${s.row}-${s.col}`"
        type="button"
        class="cseat"
        :class="{ off: s.status === 0 }"
        :title="seatLabel(s)"
        :aria-label="seatLabel(s)"
        :aria-pressed="s.status === 1"
        @click="emit('toggle', s.row, s.col)"
      />
    </div>
  </div>
</template>

<style scoped>
.canvas {
  text-align: center;
  /* 26 列时固定 26px 网格会超出半宽面板，在面板内横向滚动而不是撑破布局 */
  overflow-x: auto;
}

.mini-screen {
  height: 16px;
  background: linear-gradient(180deg, #3a3a3a, #222);
  border-radius: 6px;
  margin-bottom: 14px;
  font-size: 10px;
  color: var(--ink-2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.seat-canvas {
  display: inline-grid;
  gap: 6px;
}

.cseat {
  width: 26px;
  height: 26px;
  padding: 0;
  border: none;
  border-radius: 6px;
  background: #3a3a3a;
  cursor: pointer;
  transition: background 0.12s;
}

.cseat:hover {
  background: #555;
}

.cseat.off {
  background: var(--brand);
}

.cseat.off:hover {
  background: var(--brand-deep);
}
</style>
