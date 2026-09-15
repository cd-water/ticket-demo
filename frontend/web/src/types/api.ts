/** 后端统一响应信封（code 为字符串，"0000" 成功） */
export interface Result<T = unknown> {
  code: string
  message: string
  data: T
}

/** 通用分页响应（与 PageResult<T> 对齐） */
export interface PageResult<T> {
  total: number
  records: T[]
  page: number
  size: number
}

/* ===== auth ===== */

/** GET /api/user/me 与登录响应中的用户信息 */
export interface UserInfo {
  id: number
  phone: string
  nickname: string
}

/** POST /api/user/auth/login/sms 与 /login/password 的 data */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserInfo
}

/** POST /api/user/auth/refresh 的 data */
export interface RefreshResponse {
  accessToken: string
  refreshToken: string
}

/* ===== banner ===== */

/** GET /api/user/banners 的 data 项 */
export interface BannerVO {
  id: number
  image: string
  linkUrl: string
}

/* ===== movie ===== */

/** GET /api/user/movies/hot 与 /coming 的 data 项 */
export interface MovieVO {
  id: number
  title: string
  poster: string
}

/** GET /api/user/movies/box-office 的 data 项 */
export interface BoxOfficeVO {
  id: number
  title: string
  boxOffice: number
}

/** GET /api/user/movies/{id} 的 data */
export interface MovieDetailVO {
  id: number
  title: string
  poster: string
  description: string
  duration: number
  releaseDate: string
  showStatus: 'hot' | 'coming'
}

/** GET /api/user/cinemas/{id}/screenings 的 data 项 */
export interface ScreeningVO {
  id: number
  movieId: number
  hallId: number
  hallName: string
  startTime: string
  endTime: string
  price: number
}

/* ===== cinema ===== */

/** GET /api/user/cinemas 分页中的 data 项 */
export interface CinemaVO {
  id: number
  name: string
  address: string
  /** 0 停业 / 1 营业 */
  status: number
}

/** GET /api/user/cinemas/{id} 的 data（含该影院正在排片的电影） */
export interface CinemaDetailVO extends CinemaVO {
  movies: MovieVO[]
}

/* ===== user ===== */

/** POST /api/user/me 请求体 */
export interface UpdateProfileRequest {
  nickname: string
}