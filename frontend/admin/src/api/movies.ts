import http from '@/api/http'
import type { MovieSaveRequest, MovieVO, PageResult } from '@/types/api'

export interface MovieListQuery {
  page: number
  size: number
  title?: string
  status?: number
}

export function listMovies(q: MovieListQuery) {
  return http.get<unknown, PageResult<MovieVO>>('/admin/movies', { params: q })
}

export function saveMovie(body: MovieSaveRequest) {
  return http.post<unknown, void>('/admin/movies/save', body)
}
