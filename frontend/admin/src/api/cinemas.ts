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

export function createCinema(body: CinemaSaveRequest) {
  return http.post<unknown, void>('/admin/cinemas', body)
}

export function updateCinema(id: number, body: CinemaSaveRequest) {
  return http.put<unknown, void>(`/admin/cinemas/${id}`, body)
}

export function deleteCinema(id: number) {
  return http.delete<unknown, void>(`/admin/cinemas/${id}`)
}
