import http from '@/api/http'
import type { AdminLoginResponse } from '@/types/api'

export function adminLogin(username: string, password: string) {
  return http.post<unknown, AdminLoginResponse>('/admin/auth/login', { username, password })
}

export function adminLogout() {
  return http.post<unknown, void>('/admin/auth/logout')
}
