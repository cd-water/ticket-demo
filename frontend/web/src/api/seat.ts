import http from '@/api/http'
import type { SeatMapVO } from '@/types/api'

/** GET /api/user/screenings/{id}/seats —— 免鉴权 */
export function getSeatMap(screeningId: number) {
  return http.get<unknown, SeatMapVO>(`/user/screenings/${screeningId}/seats`)
}