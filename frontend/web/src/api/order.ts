import http from '@/api/http'
import type {
  CreateOrderRequest,
  OrderVO,
  PageResult,
} from '@/types/api'

/** POST /api/user/orders —— 创建订单（锁座+落库） */
export function createOrder(req: CreateOrderRequest) {
  return http.post<CreateOrderRequest, OrderVO>('/user/orders', req)
}

/** GET /api/user/orders?status=&page=&size= —— 我的订单列表 */
export function pageOrders(page = 1, size = 10, status?: number) {
  return http.get<unknown, PageResult<OrderVO>>('/user/orders', {
    params: { page, size, status },
  })
}

/** GET /api/user/orders/{id} —— 订单详情 */
export function orderDetail(id: number) {
  return http.get<unknown, OrderVO>(`/user/orders/${id}`)
}

/** POST /api/user/orders/{id}/cancel —— 取消订单（仅待支付） */
export function cancelOrder(id: number) {
  return http.post<unknown, void>(`/user/orders/${id}/cancel`)
}

/** POST /api/user/orders/{id}/pay —— 发起支付（当前阶段模拟成功） */
export function payOrder(id: number) {
  return http.post<unknown, OrderVO>(`/user/orders/${id}/pay`)
}