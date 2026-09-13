import http from '@/api/http'
import type { CinemaSaveRequest, CinemaVO, PageResult } from '@/types/api'

export interface CinemaListQuery {
  page: number
  size: number
  name?: string
}

export function listCinemas(q: CinemaListQuery) {
  return http.get<unknown, PageResult<CinemaVO>>('/admin/cinemas', { params: q })
}

export function listCinemasSimple() {
  return http.get<unknown, CinemaVO[]>('/admin/cinemas/simple')
}

export function saveCinema(body: CinemaSaveRequest) {
  return http.post<unknown, void>('/admin/cinemas/save', body)
}

export function deleteCinema(id: number) {
  return http.post<unknown, void>(`/admin/cinemas/${id}/delete`)
}
