<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import { listCinemas, saveCinema } from '@/api/cinemas'
import ListPager from '@/components/ListPager.vue'
import StatusFilter from '@/components/StatusFilter.vue'
import StatusPill from '@/components/StatusPill.vue'
import { useFormDialog } from '@/composables/useFormDialog'
import { usePagedList } from '@/composables/useList'
import { formatDateTime } from '@/utils/format'
import type { CinemaVO } from '@/types/api'

const STATUS_OPTIONS = [
  { label: '全部', value: null },
  { label: '营业', value: 1 },
  { label: '停业', value: 0 },
]

const router = useRouter()
const query = reactive({ name: '', status: null as number | null })

const { rows, total, loading, page, size, load, search } = usePagedList<CinemaVO>((p, s) =>
  listCinemas({
    page: p,
    size: s,
    name: query.name || undefined,
    status: query.status ?? undefined,
  }),
)

const form = reactive({ name: '', address: '', status: 1 })

const rules = {
  name: [{ required: true, message: '请输入影院名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入影院地址', trigger: 'blur' }],
}

const formRef = ref<FormInstance>()

const { visible, editingId, saving, openCreate, openEdit, submit } = useFormDialog<
  typeof form,
  CinemaVO
>({
  formRef,
  form,
  toForm: (row) =>
    row
      ? { name: row.name, address: row.address, status: row.status }
      : { name: '', address: '', status: 1 },
  save: (f, id) => saveCinema({ ...f, id }),
  onSaved: load,
})

function reset() {
  query.name = ''
  query.status = null
  search()
}

function enterCinema(row: CinemaVO) {
  router.push(`/cinemas/${row.id}/halls`)
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel-head">
      <h3>影院管理</h3>
      <div class="search">
        <el-input
          v-model="query.name"
          placeholder="搜索影院名称"
          clearable
          maxlength="100"
          style="width: 200px"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-button type="primary" @click="openCreate">＋ 新增影院</el-button>
    </div>

    <StatusFilter v-model="query.status" :options="STATUS_OPTIONS" @change="search" />

    <el-table v-loading="loading" :data="rows" style="margin-top: 12px">
      <el-table-column label="影院名称" min-width="200" prop="name" />
      <el-table-column label="地址" min-width="280" prop="address" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusPill :label="row.status === 1 ? '营业' : '停业'" :tone="row.status === 1 ? 'ok' : 'muted'" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="enterCinema(row)">进入</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>暂无影院，点右上角「新增影院」开始</template>
    </el-table>

    <ListPager v-model:page="page" v-model:size="size" :total="total" @change="load" />

    <el-dialog v-model="visible" :title="editingId === null ? '新增影院' : '编辑影院'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="影院名称" prop="name">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" maxlength="255" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">营业</el-radio>
            <el-radio :value="0">停业</el-radio>
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
