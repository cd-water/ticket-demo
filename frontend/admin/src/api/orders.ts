import http from '@/api/http'
import type { OrderListQuery, OrderVO, PageResult } from '@/types/api'

export function listOrdersByCinema(cinemaId: number, q: OrderListQuery) {
  return http.get<unknown, PageResult<OrderVO>>(`/admin/cinemas/${cinemaId}/orders`, {
    params: q,
  })
}
