import http from '@/api/http'
import type { MovieOption, PageResult, ScreeningSaveRequest, ScreeningVO } from '@/types/api'

export interface ScreeningListQuery {
  page: number
  size: number
  movieId?: number
}

export function listScreenings(q: ScreeningListQuery) {
  return http.get<unknown, PageResult<ScreeningVO>>('/admin/screenings', { params: q })
}

export function listMovieOptions() {
  return http.get<unknown, MovieOption[]>('/admin/screenings/movie-options')
}

export function saveScreening(body: ScreeningSaveRequest) {
  return http.post<unknown, void>('/admin/screenings/save', body)
}

export function deleteScreening(id: number) {
  return http.post<unknown, void>(`/admin/screenings/${id}/delete`)
}
