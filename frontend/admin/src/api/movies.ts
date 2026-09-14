import http from '@/api/http'
import type { MovieListQuery, MovieSaveRequest, MovieVO, PageResult } from '@/types/api'

export function listMovies(q: MovieListQuery) {
  return http.get<unknown, PageResult<MovieVO>>('/admin/movies', { params: q })
}

export function saveMovie(body: MovieSaveRequest) {
  return http.post<unknown, void>('/admin/movies/save', body)
}
