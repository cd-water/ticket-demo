import http from '@/api/http'
import type {
  BoxOfficeVO,
  MovieDetailVO,
  MovieVO,
  PageResult,
} from '@/types/api'

/** GET /api/user/movies/hot —— 免鉴权 */
export function listHotMovies() {
  return http.get<unknown, MovieVO[]>('/user/movies/hot')
}

/** GET /api/user/movies/coming —— 免鉴权 */
export function listComingMovies() {
  return http.get<unknown, MovieVO[]>('/user/movies/coming')
}

/** GET /api/user/movies/box-office —— 免鉴权 */
export function listBoxOffice() {
  return http.get<unknown, BoxOfficeVO[]>('/user/movies/box-office')
}

/** GET /api/user/movies?showStatus=&page=&size= —— 免鉴权 */
export function pageMovies(showStatus: 'hot' | 'coming', page = 1, size = 10) {
  return http.get<unknown, PageResult<MovieVO>>('/user/movies', {
    params: { showStatus, page, size },
  })
}

/** GET /api/user/movies/{id} —— 免鉴权 */
export function getMovie(id: number) {
  return http.get<unknown, MovieDetailVO>(`/user/movies/${id}`)
}