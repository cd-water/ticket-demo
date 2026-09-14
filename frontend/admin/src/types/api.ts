/**
 * 与后端 cd-ticket-admin 一一对应的线格式类型。
 *
 * 规则：凡出现在请求体、query 或响应里的类型都放在这里，`api/*.ts` 只放请求函数。
 *
 * 字段约束来自两处，取更严者：后端 DTO 上的校验注解，以及 script/sql/table.sql 的列定义。
 * 各表单里 `el-input` 的 maxlength 对应列长度（片名 100、简介 1024、影院名 100、地址 255、
 * 影厅名 50、跳转链接 255、管理员用户名 32、手机号 20），不要在前端自造额外上限。
 */

/** 后端统一响应信封：code 为数字，200 成功，其余沿用 HTTP 语义 */
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页响应的 data */
export interface PageResult<T> {
  total: number
  records: T[]
  page: number
  size: number
}

/** 分页查询公共参数（page ≥ 1，size 1–100） */
export interface PageQuery {
  page: number
  size: number
}

/* ---------- 认证与管理员 ---------- */

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

/** GET /api/admin/admins/list 的元素 */
export interface AdminManageVO {
  id: number
  username: string
  /** 0-禁用 1-启用 */
  status: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/admins/create 的请求体 */
export interface AdminSaveRequest {
  /** 5–32 位，全局唯一 */
  username: string
  /** 8–20 位且同时含字母和数字 */
  password: string
}

/** POST /api/admin/admins/{id}/reset-password 的请求体 */
export interface ResetPasswordRequest {
  password: string
}

/* ---------- 电影 ---------- */

/** GET /api/admin/movies 的元素，按 id 倒序 */
export interface MovieVO {
  id: number
  title: string
  poster: string
  description: string
  duration: number
  /** yyyy-MM-dd */
  releaseDate: string
  /** 0-下架 1-上架 */
  status: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/movies/save 的请求体，id 不传即新增 */
export interface MovieSaveRequest {
  id?: number | null
  title: string
  poster: string
  description: string
  duration: number
  releaseDate: string
  status: number
}

export interface MovieListQuery extends PageQuery {
  title?: string
  status?: number
}

/** GET /api/admin/screenings/movie-options 的元素（仅上架影片） */
export interface MovieOption {
  id: number
  title: string
}

/* ---------- 影院 ---------- */

/** GET /api/admin/cinemas 的元素，按 id 倒序 */
export interface CinemaVO {
  id: number
  name: string
  address: string
  /** 0-停业 1-营业 */
  status: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/cinemas/save 的请求体，id 不传即新增 */
export interface CinemaSaveRequest {
  id?: number | null
  name: string
  address: string
  status: number
}

export interface CinemaListQuery extends PageQuery {
  name?: string
  status?: number
}

/* ---------- 影厅与座位 ---------- */

/** GET /api/admin/cinemas/{cinemaId}/halls 的元素，按 id 升序 */
export interface HallVO {
  id: number
  cinemaId: number
  name: string
  /** 1–26 */
  seatRows: number
  /** 1–26 */
  seatCols: number
  /** 0-禁用 1-启用 */
  status: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/halls/save 的请求体，id 不传即新增 */
export interface HallSaveRequest {
  id?: number | null
  cinemaId: number
  name: string
  seatRows: number
  seatCols: number
  /** 新增不传时由数据库默认 1 */
  status?: number | null
}

/** GET /api/admin/halls/{id}/seats 的 data.seats 元素 */
export interface SeatCellVO {
  row: number
  col: number
  /** 展示座位号，如 "3排5座" */
  seatNo: string
  /** 0-禁用 1-启用 */
  status: number
}

/** POST /api/admin/halls/{id}/seats 的请求体元素（裸数组，整表覆盖） */
export interface SeatCellRequest {
  row: number
  col: number
  status: number
}

/** GET /api/admin/halls/{id}/seats 的 data */
export interface SeatGridVO {
  rows: number
  cols: number
  seats: SeatCellVO[]
}

/* ---------- 排场 ---------- */

/** GET /api/admin/cinemas/{cinemaId}/screenings 的元素，按开场时间倒序 */
export interface ScreeningVO {
  id: number
  movieId: number
  movieTitle: string
  hallId: number
  hallName: string
  cinemaId: number
  /** yyyy-MM-dd HH:mm:ss */
  startTime: string
  price: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/screenings/save 的请求体，id 不传即新增 */
export interface ScreeningSaveRequest {
  id?: number | null
  movieId: number
  hallId: number
  /** yyyy-MM-dd HH:mm:ss，且必须晚于当前时间 */
  startTime: string
  /** ≥ 0.01，DECIMAL(10,2) */
  price: number
}

export interface ScreeningListQuery extends PageQuery {
  movieId?: number
}

/* ---------- 订单（只读） ---------- */

/** GET /api/admin/cinemas/{cinemaId}/orders 的元素，按下单时间倒序 */
export interface OrderVO {
  id: number
  /** 雪花 ID，后端强制序列化为字符串以免超出 JS 安全整数范围 */
  orderNo: string
  userId: number
  userPhone: string | null
  screeningId: number
  movieId: number
  movieTitle: string
  /** 0-待支付 1-已支付 2-已取消 */
  status: number
  totalAmount: number
  payExpireTime: string
  payTime: string | null
  createTime: string
  updateTime: string
}

export interface OrderListQuery extends PageQuery {
  /** 精确匹配，按字符串透传（t_order.order_no 是 BIGINT） */
  orderNo?: string
  status?: number
}

/* ---------- 用户 ---------- */

/** GET /api/admin/users 的元素，按 id 倒序 */
export interface UserVO {
  id: number
  phone: string
  nickname: string
  /** 0-禁用 1-正常 */
  status: number
  createTime: string
  updateTime: string
}

export interface UserListQuery extends PageQuery {
  phone?: string
  status?: number
}

/* ---------- 仪表盘 ---------- */

/** GET /api/admin/dashboard */
export interface DashboardVO {
  /** 热映：上架中且已上映（release_date ≤ 今天） */
  hotMovieCount: number
  /** 待映：上架中且未上映（release_date > 今天） */
  upcomingMovieCount: number
  /** 营业中的影院数（status=1） */
  cinemaOpenCount: number
  /** 停业的影院数（status=0） */
  cinemaClosedCount: number
  /** 用户总数（不论状态） */
  userCount: number
  todayPendingCount: number
  todayPaidCount: number
  todayCancelledCount: number
  /** 今日已支付订单营收 */
  todayRevenue: number
  /** 全部已支付订单营收 */
  totalRevenue: number
  totalOrderCount: number
  totalPendingCount: number
  totalPaidCount: number
  totalCancelledCount: number
  /** 各影院经营汇总（已支付营收降序） */
  perCinemaStats: CinemaStat[]
  /** 影片票房排行 Top 5（已支付营收降序） */
  movieRanking: MovieRank[]
  /** 近 7 日已支付订单/营收，缺失日期补 0 */
  dailyStats: DailyOrderStat[]
}

/** 单个影院的经营汇总（仅已支付订单计营收与订单数） */
export interface CinemaStat {
  name: string
  /** 已支付订单数 */
  orderCount: number
  /** 已支付订单营收 */
  revenue: number
}

/** 影片票房排行条目（仅已支付订单） */
export interface MovieRank {
  title: string
  /** 已支付订单数 */
  orderCount: number
  /** 已支付订单营收 */
  revenue: number
}

/** 单日订单统计（仅已支付订单） */
export interface DailyOrderStat {
  /** yyyy-MM-dd */
  date: string
  orderCount: number
  revenue: number
}

/* ---------- 轮播图 ---------- */

/** GET /api/admin/banners 的元素，按 sort 升序、同 sort 按 id 倒序 */
export interface BannerVO {
  id: number
  image: string
  linkUrl: string
  /** 小者靠前 */
  sort: number
  /** 0-禁用 1-启用（t_banner 是唯一 DEFAULT 0 的表） */
  status: number
  createTime: string
  updateTime: string
}

/** POST /api/admin/banners/save 的请求体，id 不传即新增 */
export interface BannerSaveRequest {
  id?: number | null
  image: string
  linkUrl: string
  sort: number
  status: number
}
