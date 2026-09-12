import http from '@/api/http'
import type { OrderAdminRecord, PageResult } from '@/types/api'

export interface OrderListQuery {
  page: number
  size: number
  orderNo?: string
  status?: number
}

export function listOrders(q: OrderListQuery) {
  return http.get<unknown, PageResult<OrderAdminRecord>>('/admin/orders', { params: q })
}
