/** 后端统一响应信封（code 为字符串，"0000" 成功） */
export interface Result<T = unknown> {
  code: string
  message: string
  data: T
}

/** GET /api/user/me 与登录响应中的用户信息 */
export interface UserInfo {
  id: number
  phone: string
  nickname: string
  hasPassword: boolean
}

/** POST /api/user/auth/login/sms 与 /login/password 的 data */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserInfo
}
