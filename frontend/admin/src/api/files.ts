import http from '@/api/http'

export function uploadImage(file: File) {
  const body = new FormData()
  body.append('file', file)
  return http.post<unknown, string>('/admin/files', body)
}
