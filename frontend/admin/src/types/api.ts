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
}

/** POST /api/admin/auth/login 的 data */
export interface AdminLoginResponse {
  token: string
  admin: AdminInfo
}

/** 管理端列表接口的 data */
export interface PageResult<T> {
  total: number
  records: T[]
  page: number
  size: number
}

/** 电影（/api/admin/movies） */
export interface MovieVO {
  id: number
  title: string
  poster: string
  description: string | null
  duration: number
  releaseDate: string | null
  status: number
  createTime: string
  updateTime: string
}

export interface MovieSaveRequest {
  id?: number | null
  title: string
  poster?: string | null
  description?: string | null
  duration: number
  releaseDate?: string | null
  status: number
}

export interface MovieOption {
  id: number
  title: string
}

/** 轮播图（/api/admin/banners） */
export interface BannerVO {
  id: number
  image: string
  linkUrl: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

export interface BannerSaveRequest {
  id?: number | null
  image: string
  linkUrl?: string
  sort: number
  status: number
}

/** 影院（/api/admin/cinemas） */
export interface CinemaVO {
  id: number
  name: string
  address: string
  status: number
  createTime: string
  updateTime: string
}

export interface CinemaSaveRequest {
  id?: number | null
  name: string
  address: string
  status: number
}

/** 影厅与座位 */
export interface HallVO {
  id: number
  cinemaId: number
  name: string
  seatRows: number
  seatCols: number
  status: number
  createTime: string
  updateTime: string
}

export interface HallSaveRequest {
  id?: number | null
  cinemaId: number
  name: string
  seatRows: number
  seatCols: number
}

export interface SeatCellVO {
  row: number
  col: number
  seatNo: string
  status: number
}

export interface SeatCellRequest {
  row: number
  col: number
  status: number
}

export interface SeatGridVO {
  rows: number
  cols: number
  seats: SeatCellVO[]
}

/** 排场 */
export interface ScreeningVO {
  id: number
  movieId: number
  movieTitle: string
  hallId: number
  hallName: string
  cinemaId: number
  startTime: string
  price: number
  status: number
  createTime: string
  updateTime: string
}

export interface ScreeningSaveRequest {
  id?: number | null
  cinemaId: number
  movieId: number
  hallId: number
  startTime: string
  price: number
}

/** 订单（只读） */
export interface OrderVO {
  id: number
  orderNo: string
  userId: number
  userPhone: string | null
  userNickname: string | null
  screeningId: number
  movieId: number
  movieTitle: string
  cinemaId: number
  status: number
  totalAmount: number
  payExpireTime: string | null
  payTime: string | null
  cancelType: number | null
  createTime: string
  updateTime: string
}

/** 用户（/api/admin/users） */
export interface UserVO {
  id: number
  phone: string
  nickname: string
  status: number
  createTime: string
  updateTime: string
}

/** 管理员管理（/api/admin/admins/list） */
export interface AdminManageVO {
  id: number
  username: string
  status: number
  createTime: string
  updateTime: string
}

/** 新增管理员 */
export interface AdminSaveRequest {
  username: string
  password: string
}

/** 重置密码 */
export interface ResetPasswordRequest {
  password: string
}

/** 文件上传（/api/admin/files） */
export interface UploadResponse {
  url: string
}
