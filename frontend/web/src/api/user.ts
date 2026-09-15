import http from '@/api/http'
import type { UpdateProfileRequest, UserInfo } from '@/types/api'

/** GET /api/user/me —— 需鉴权 */
export function getMe() {
  return http.get<unknown, UserInfo>('/user/me')
}

/** POST /api/user/me —— 需鉴权，更新个人资料（当前仅 nickname） */
export function updateProfile(req: UpdateProfileRequest) {
  return http.post<unknown, UserInfo>('/user/me', req)
}