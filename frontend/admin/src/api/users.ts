import http from '@/api/http'
import type { PageResult, UserVO } from '@/types/api'

export interface UserListQuery {
  page: number
  size: number
  phone?: string
  status?: number
}

export function listUsers(q: UserListQuery) {
  return http.get<unknown, PageResult<UserVO>>('/admin/users', { params: q })
}

export function toggleUserStatus(id: number, status: number) {
  return http.post<unknown, void>(`/admin/users/${id}/status`, null, { params: { status } })
}
