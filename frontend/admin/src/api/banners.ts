import http from '@/api/http'
import type { BannerSaveRequest, BannerVO } from '@/types/api'

export function listBanners() {
  return http.get<unknown, BannerVO[]>('/admin/banners')
}

export function createBanner(body: BannerSaveRequest) {
  return http.post<unknown, void>('/admin/banners', body)
}

export function updateBanner(id: number, body: BannerSaveRequest) {
  return http.put<unknown, void>(`/admin/banners/${id}`, body)
}

export function deleteBanner(id: number) {
  return http.delete<unknown, void>(`/admin/banners/${id}`)
}
