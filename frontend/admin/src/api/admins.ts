import http from '@/api/http'
import type { AdminCreateRequest, AdminManageVO, AdminUpdateRequest } from '@/types/api'

export function listAdmins(role?: number) {
  return http.get<unknown, AdminManageVO[]>('/admin/admins', { params: { role } })
}

export function createAdmin(body: AdminCreateRequest) {
  return http.post<unknown, void>('/admin/admins', body)
}

export function updateAdmin(id: number, body: AdminUpdateRequest) {
  return http.put<unknown, void>(`/admin/admins/${id}`, body)
}

export function deleteAdmin(id: number) {
  return http.delete<unknown, void>(`/admin/admins/${id}`)
}
