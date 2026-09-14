import http from '@/api/http'
import type { AdminManageVO } from '@/types/api'
import type { AdminSaveRequest } from '@/types/api'
import type { ResetPasswordRequest } from '@/types/api'

export function listAdmins() {
  return http.get<unknown, AdminManageVO[]>('/admin/admins/list')
}

export function createAdmin(body: AdminSaveRequest) {
  return http.post<unknown, void>('/admin/admins/create', body)
}

export function resetPassword(id: number, body: ResetPasswordRequest) {
  return http.post<unknown, void>(`/admin/admins/${id}/reset-password`, body)
}

export function updateAdminStatus(id: number, status: number) {
  return http.post<unknown, void>(`/admin/admins/${id}/status`, null, { params: { status } })
}
