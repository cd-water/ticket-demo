import { computed, type ComputedRef } from 'vue'
import { useRoute } from 'vue-router'

/**
 * 影院详情路由（/cinemas/:cinemaId）下的当前影院 ID。
 *
 * 必须用 computed 而不是在 setup 里取一次值：子路由切换时组件会复用，
 * 一次性取值会让请求继续打向旧影院。
 */
export function useCinemaId(): ComputedRef<number> {
  const route = useRoute()
  return computed(() => Number(route.params.cinemaId))
}
