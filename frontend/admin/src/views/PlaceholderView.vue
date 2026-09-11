<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="placeholder">
    <span class="dot" aria-hidden="true"></span>
    <h1>{{ route.meta.title }}</h1>
    <p>页面建设中</p>
  </div>
</template>

<style scoped>
.placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: var(--brand);
  box-shadow: 0 0 18px rgba(255, 195, 0, 0.6);
  margin-bottom: 8px;
}

h1 {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 900;
}

p {
  margin: 0;
  color: var(--ink-2);
  font-size: 14px;
}
</style>
