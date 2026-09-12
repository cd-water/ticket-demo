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

export function createMovie(body: MovieSaveRequest) {
  return http.post<unknown, void>('/admin/movies', body)
}

export function updateMovie(id: number, body: MovieSaveRequest) {
  return http.put<unknown, void>(`/admin/movies/${id}`, body)
}

export function deleteMovie(id: number) {
  return http.delete<unknown, void>(`/admin/movies/${id}`)
}
