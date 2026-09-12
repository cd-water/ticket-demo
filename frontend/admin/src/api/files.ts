import http from '@/api/http'
import type { UploadResponse } from '@/types/api'

export function uploadImage(file: File) {
  const body = new FormData()
  body.append('file', file)
  return http.post<unknown, UploadResponse>('/admin/files', body)
}
