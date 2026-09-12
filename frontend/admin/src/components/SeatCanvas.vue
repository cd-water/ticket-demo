<script setup lang="ts">
import type { SeatCellVO } from '@/types/api'

const props = defineProps<{ rows: number; cols: number; seats: SeatCellVO[] }>()
const emit = defineEmits<{ toggle: [row: number, col: number] }>()
</script>

<template>
  <div class="canvas">
    <div class="mini-screen">银幕</div>
    <div
      class="seat-canvas"
      :aria-label="`座位图 ${props.rows}排${props.cols}列`"
      :style="{ gridTemplateColumns: `repeat(${props.cols}, 26px)` }"
    >
      <button
        v-for="s in props.seats"
        :key="`${s.row}-${s.col}`"
        type="button"
        class="cseat"
        :class="{ off: s.status === 0 }"
        :title="`${s.seatNo} ${s.status === 1 ? '启用' : '禁用'}`"
        @click="emit('toggle', s.row, s.col)"
      />
    </div>
  </div>
</template>

<style scoped>
.canvas {
  text-align: center;
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
