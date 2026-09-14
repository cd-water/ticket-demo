import http from '@/api/http'
import type { DashboardVO } from '@/types/api'

export function getDashboard() {
  return http.get<unknown, DashboardVO>('/admin/dashboard')
}
