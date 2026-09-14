import { ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

interface Options<T extends object, R extends { id: number }> {
  /** 视图里声明的 el-form 模板 ref（模板上仍写 ref="formRef"） */
  formRef: Ref<FormInstance | undefined>
  /** 调用方的 reactive 模型，保持引用不变，这里只做 Object.assign 回填 */
  form: T
  /** row 为 null 时返回新增用的默认值 */
  toForm: (row: R | null) => T
  /** id 为 null 表示新增 */
  save: (form: T, id: number | null) => Promise<unknown>
  /** 保存成功且弹窗关闭后调用，默认什么都不做 */
  onSaved?: (id: number | null) => void | Promise<void>
  messages?: { created?: string; updated?: string }
}

/**
 * 列表页的新增/编辑弹窗。
 *
 * 返回的是普通对象里的 ref，调用方**必须解构**后再用，模板里不会自动解包：
 * `const { visible, saving, openCreate, submit } = useFormDialog({...})`
 */
export function useFormDialog<T extends object, R extends { id: number } = { id: number }>(
  opts: Options<T, R>,
) {
  const { formRef } = opts
  const visible = ref(false)
  const editingId = ref<number | null>(null)
  const saving = ref(false)

  const created = opts.messages?.created ?? '已新增'
  const updated = opts.messages?.updated ?? '已保存'

  function open(row: R | null) {
    Object.assign(opts.form, opts.toForm(row))
    editingId.value = row?.id ?? null
    formRef.value?.clearValidate()
    visible.value = true
  }

  function openCreate() {
    open(null)
  }

  function openEdit(row: R) {
    open(row)
  }

  async function submit() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    saving.value = true
    const id = editingId.value
    try {
      await opts.save(opts.form, id)
      ElMessage.success(id === null ? created : updated)
      visible.value = false
      await opts.onSaved?.(id)
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      saving.value = false
    }
  }

  return { visible, editingId, saving, openCreate, openEdit, submit }
}
