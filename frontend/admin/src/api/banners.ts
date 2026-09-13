import http from '@/api/http'
import type { BannerSaveRequest, BannerVO } from '@/types/api'

export function listBanners() {
  return http.get<unknown, BannerVO[]>('/admin/banners')
}

export function saveBanner(body: BannerSaveRequest) {
  return http.post<unknown, void>('/admin/banners/save', body)
}

export function deleteBanner(id: number) {
  return http.post<unknown, void>(`/admin/banners/${id}/delete`)
}
