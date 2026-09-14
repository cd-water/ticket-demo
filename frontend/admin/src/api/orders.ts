import http from '@/api/http'
import type { OrderVO, PageResult } from '@/types/api'

export interface CinemaOrderQuery {
  cinemaId: number
  page: number
  size: number
  orderNo?: string
  status?: number
}

export function listOrdersByCinema(q: CinemaOrderQuery) {
  return http.get<unknown, PageResult<OrderVO>>(
    `/admin/cinemas/${q.cinemaId}/orders`,
    { params: { page: q.page, size: q.size, orderNo: q.orderNo, status: q.status } },
  )
}
