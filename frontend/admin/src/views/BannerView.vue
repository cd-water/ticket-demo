<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { listBanners, saveBanner } from '@/api/banners'
import ImageUpload from '@/components/ImageUpload.vue'
import StatusPill from '@/components/StatusPill.vue'
import { useFormDialog } from '@/composables/useFormDialog'
import { useList } from '@/composables/useList'
import { formatDateTime } from '@/utils/format'
import type { BannerVO } from '@/types/api'

const { rows, loading, load } = useList<BannerVO>(listBanners)

// t_banner 是全库唯一 status 默认 0 的表：新增即禁用，需手动启用
const form = reactive({ image: '', linkUrl: '', sort: 0, status: 0 })

const rules = {
  image: [{ required: true, message: '请上传轮播图片', trigger: 'change' }],
  linkUrl: [{ required: true, message: '请输入跳转链接', trigger: 'blur' }],
}

const formRef = ref<FormInstance>()

const { visible, editingId, saving, openCreate, openEdit, submit } = useFormDialog<
  typeof form,
  BannerVO
>({
  formRef,
  form,
  toForm: (row) =>
    row
      ? { image: row.image, linkUrl: row.linkUrl, sort: row.sort, status: row.status }
      : { image: '', linkUrl: '', sort: 0, status: 0 },
  save: (f, id) => saveBanner({ ...f, id }),
  onSaved: load,
})

/** 上传是程序化写 model，不会触发 el-form-item 的校验，手动补一次 */
function onUploaded() {
  formRef.value?.validateField('image').catch(() => undefined)
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>轮播图管理</h3>
      <el-button type="primary" style="margin-left: auto" @click="openCreate">＋ 新增轮播图</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <img v-if="row.image" class="thumb-wide" :src="row.image" alt="轮播图" />
          <span v-else class="thumb-wide" />
        </template>
      </el-table-column>
      <el-table-column label="跳转链接" min-width="200">
        <template #default="{ row }">{{ row.linkUrl || '—' }}</template>
      </el-table-column>
      <el-table-column label="排序" width="90" prop="sort" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '启用' : '禁用'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无轮播图，点右上角「新增轮播图」开始</template>
    </el-table>

    <el-dialog v-model="visible" :title="editingId === null ? '新增轮播图' : '编辑轮播图'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="图片" prop="image">
          <ImageUpload
            v-model="form.image"
            placeholder="＋ 上传图片"
            alt="轮播图预览"
            wide
            @uploaded="onUploaded"
          />
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" maxlength="255" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :value-on-clear="0" />
          <span class="hint">数字小者靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
