<script setup lang="ts">
defineProps<{ total: number; page: number; size: number }>()

const emit = defineEmits<{
  'update:page': [value: number]
  'update:size': [value: number]
  /** 页码或每页条数变化后触发，父组件据此重新拉取 */
  change: []
}>()

function onPage(p: number) {
  emit('update:page', p)
  emit('change')
}

function onSize(s: number) {
  emit('update:size', s)
  emit('update:page', 1)
  emit('change')
}
</script>

<template>
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
</template>

<style scoped>
.pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid var(--line);
}

.pager .total {
  margin-right: auto;
  font-size: 12px;
  color: var(--ink-2);
}
</style>
