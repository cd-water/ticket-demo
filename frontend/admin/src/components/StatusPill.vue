<script setup lang="ts">
type Tone = 'ok' | 'warn' | 'muted' | 'danger'

defineProps<{ label: string; tone?: Tone }>()
</script>

<template>
  <span class="tag-pill" :class="`t-${tone ?? 'muted'}`">{{ label }}</span>
</template>
