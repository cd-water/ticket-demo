import http from '@/api/http'
import type { BannerVO } from '@/types/api'

/** GET /api/user/banners —— 免鉴权 */
export function listBanners() {
  return http.get<unknown, BannerVO[]>('/user/banners')
}