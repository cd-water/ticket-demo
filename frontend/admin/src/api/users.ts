import http from '@/api/http'
import type { PageResult, UserAdminVO } from '@/types/api'

export interface UserListQuery {
  page: number
  size: number
  phone?: string
}

export function listUsers(q: UserListQuery) {
  return http.get<unknown, PageResult<UserAdminVO>>('/admin/users', { params: q })
}

export function updateUserStatus(id: number, status: number) {
  return http.post<unknown, void>(`/admin/users/${id}/status`, { status })
}
