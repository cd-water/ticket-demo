import http from '@/api/http'
import type { AdminManageVO, AdminSaveRequest } from '@/types/api'

export function listAdmins(role?: number) {
  return http.get<unknown, AdminManageVO[]>('/admin/admins/list', { params: { role } })
}

export function saveAdmin(body: AdminSaveRequest) {
  return http.post<unknown, void>('/admin/admins/save', body)
}

export function deleteAdmin(id: number) {
  return http.post<unknown, void>(`/admin/admins/${id}/delete`)
}
