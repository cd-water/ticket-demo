import http from '@/api/http'
import type { OrderItemVO, OrderListQuery, OrderVO, PageResult } from '@/types/api'

export function listOrdersByCinema(cinemaId: number, q: OrderListQuery) {
  return http.get<unknown, PageResult<OrderVO>>(`/admin/cinemas/${cinemaId}/orders`, {
    params: q,
  })
}

export function getOrderItems(orderId: number) {
  return http.get<unknown, OrderItemVO[]>(`/admin/orders/${orderId}/items`)
}
