import http from '@/api/http'
import type {
  MovieOption,
  PageResult,
  ScreeningListQuery,
  ScreeningSaveRequest,
  ScreeningVO,
} from '@/types/api'

export function listScreeningsByCinema(cinemaId: number, q: ScreeningListQuery) {
  return http.get<unknown, PageResult<ScreeningVO>>(`/admin/cinemas/${cinemaId}/screenings`, {
    params: q,
  })
}

export function listMovieOptions() {
  return http.get<unknown, MovieOption[]>('/admin/screenings/movie-options')
}

export function saveScreening(body: ScreeningSaveRequest) {
  return http.post<unknown, void>('/admin/screenings/save', body)
}
