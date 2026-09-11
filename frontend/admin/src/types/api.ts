/** 后端统一响应信封（code 为字符串，"0000" 成功） */
export interface Result<T = unknown> {
  code: string
  message: string
  data: T
}

/** POST /api/admin/auth/login 的 data.admin */
export interface AdminInfo {
  id: number
  username: string
  /** 0=超级管理员 1=影院管理员 */
  role: number
  /** 关联影院 ID，超级管理员为 0 */
  cinemaId: number
}

/** POST /api/admin/auth/login 的 data */
export interface AdminLoginResponse {
  token: string
  admin: AdminInfo
}
