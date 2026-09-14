<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/files'

const props = defineProps<{
  modelValue: string
  placeholder: string
  alt: string
  /** 轮播图那种宽幅预览 */
  wide?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  /** 上传成功并写回值之后触发，调用方据此补一次表单校验 */
  uploaded: []
}>()

const uploading = ref(false)

/** 同步返回 false 交给 el-upload 自己接管上传 */
function onUpload(file: File) {
  uploading.value = true
  uploadImage(file)
    .then((url) => {
      emit('update:modelValue', url)
      emit('uploaded')
      ElMessage.success('图片已上传')
    })
    .catch(() => {
      /* 错误提示已由 http.ts 统一弹出 */
    })
    .finally(() => {
      uploading.value = false
    })
  return false
}
</script>

<template>
  <el-upload
    :show-file-list="false"
    accept="image/*"
    :disabled="uploading"
    :before-upload="onUpload"
  >
    <img
      v-if="props.modelValue"
      class="upload-preview"
      :class="{ wide: props.wide }"
      :src="props.modelValue"
      :alt="props.alt"
    />
    <div v-else class="upload-slot" :class="{ wide: props.wide }">
      {{ uploading ? '上传中…' : props.placeholder }}
    </div>
  </el-upload>
</template>

<style scoped>
.upload-slot {
  width: 96px;
  height: 132px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed var(--line);
  border-radius: 8px;
  color: var(--ink-2);
  font-size: 12px;
  cursor: pointer;
}

.upload-slot.wide {
  width: 180px;
  height: 76px;
}

.upload-preview {
  width: 96px;
  height: 132px;
  border-radius: 8px;
  object-fit: cover;
  display: block;
}

.upload-preview.wide {
  width: 180px;
  height: 76px;
}
</style>
