import http from '@/api/http'
import type { OrderVO, PageResult } from '@/types/api'

export interface OrderListQuery {
  page: number
  size: number
  orderNo?: string
  status?: number
}

export function listOrders(q: OrderListQuery) {
  return http.get<unknown, PageResult<OrderVO>>('/admin/orders', { params: q })
}
