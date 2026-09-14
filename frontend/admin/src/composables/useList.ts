import { ref, type Ref } from 'vue'
import type { PageResult } from '@/types/api'

/**
 * 列表数据加载。请求失败已由 http.ts 统一提示，这里只负责 loading 与赋值。
 * 不在这里调 onMounted——何时发请求留在调用点，读代码时看得见。
 */

/** 不分页的一次性列表 */
export function useList<T>(fetch: () => Promise<T[]>) {
  const rows = ref([]) as Ref<T[]>
  const loading = ref(false)

  async function load() {
    loading.value = true
    try {
      rows.value = await fetch()
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      loading.value = false
    }
  }

  return { rows, loading, load }
}

/** 分页列表 */
export function usePagedList<T>(fetch: (page: number, size: number) => Promise<PageResult<T>>) {
  const rows = ref([]) as Ref<T[]>
  const total = ref(0)
  const loading = ref(false)
  const page = ref(1)
  const size = ref(10)

  async function load() {
    loading.value = true
    try {
      const data = await fetch(page.value, size.value)
      rows.value = data.records
      total.value = data.total
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      loading.value = false
    }
  }

  /** 回到第一页再查：筛选条件变化时用 */
  async function search() {
    page.value = 1
    await load()
  }

  return { rows, total, loading, page, size, load, search }
}
