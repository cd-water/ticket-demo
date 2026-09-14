import http from '@/api/http'
import type { MovieOption, PageResult, ScreeningSaveRequest, ScreeningVO } from '@/types/api'

export interface CinemaScreeningQuery {
  cinemaId: number
  page: number
  size: number
  movieId?: number
}

export function listScreeningsByCinema(q: CinemaScreeningQuery) {
  return http.get<unknown, PageResult<ScreeningVO>>(
    `/admin/cinemas/${q.cinemaId}/screenings`,
    { params: { page: q.page, size: q.size, movieId: q.movieId } },
  )
}

export function listMovieOptions() {
  return http.get<unknown, MovieOption[]>('/admin/screenings/movie-options')
}

export function saveScreening(body: ScreeningSaveRequest) {
  return http.post<unknown, void>('/admin/screenings/save', body)
}
