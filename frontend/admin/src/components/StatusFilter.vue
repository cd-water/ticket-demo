<script setup lang="ts">
defineProps<{
  modelValue: number | null
  /** 选项文案随页面而变（上架/营业/正常/待支付…），故由调用方传入 */
  options: { label: string; value: number | null }[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  change: []
}>()

function pick(value: number | null) {
  emit('update:modelValue', value)
  emit('change')
}
</script>

<template>
  <div class="filters">
    <button
      v-for="o in options"
      :key="String(o.value)"
      type="button"
      class="f-btn"
      :class="{ on: modelValue === o.value }"
      @click="pick(o.value)"
    >
      {{ o.label }}
    </button>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  padding: 12px 20px 0;
}

.f-btn {
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 999px;
  color: var(--ink-2);
  border: 1px solid var(--line);
  background: none;
  font-family: inherit;
  cursor: pointer;
}

.f-btn.on {
  background: var(--brand);
  color: var(--on-brand);
  font-weight: 700;
  border-color: var(--brand);
}
</style>
