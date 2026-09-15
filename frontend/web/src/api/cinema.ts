import http from '@/api/http'
import type {
  CinemaDetailVO,
  CinemaVO,
  PageResult,
  ScreeningVO,
} from '@/types/api'

/** GET /api/user/cinemas?page=&size= —— 免鉴权 */
export function pageCinemas(page = 1, size = 10) {
  return http.get<unknown, PageResult<CinemaVO>>('/user/cinemas', {
    params: { page, size },
  })
}

/** GET /api/user/cinemas/{id} —— 免鉴权 */
export function getCinema(id: number) {
  return http.get<unknown, CinemaDetailVO>(`/user/cinemas/${id}`)
}

/** GET /api/user/cinemas/{id}/screenings?movieId= —— 免鉴权 */
export function listScreenings(cinemaId: number, movieId?: number) {
  return http.get<unknown, ScreeningVO[]>(`/user/cinemas/${cinemaId}/screenings`, {
    params: movieId === undefined ? {} : { movieId },
  })
}