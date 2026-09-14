# 电影票务系统 · 管理端 API

- **服务**：`cd-ticket-admin`（端口 8601，独立 Spring Boot 应用）
- **Base URL**：`/api/admin`
- **前端代理**：`frontend/admin` 的 vite 把 `/api` 代理到 8601

## 目录

1. [通用约定](#1-通用约定)
2. [认证与管理员](#2-认证与管理员admincontroller)
3. [电影管理](#3-电影管理moviecontroller)
4. [影院管理](#4-影院管理cinemacontroller)
5. [影厅与座位](#5-影厅与座位hallcontrollerseatconfigservice)
6. [排场管理](#6-排场管理screeningcontroller)
7. [订单查询（只读）](#7-订单查询只读ordercontroller)
8. [用户管理](#8-用户管理usercontroller)
9. [轮播图管理](#9-轮播图管理bannercontroller)
10. [文件上传](#10-文件上传filecontroller)
11. [接口速查](#11-接口速查)

---

## 1. 通用约定

### 1.1 鉴权

除登录接口外，**所有接口必须携带 Token**：

```
Authorization: Bearer {token}
```

Token 由登录接口返回，有效期 24 小时（`token.expire-seconds`），**每次有效请求自动续期**；同一管理员重新登录会踢掉上一个 Token（单设备登录）。

Token 缺失或失效：**HTTP 401**（不是 HTTP 200），响应体仍是统一信封：

```json
{ "code": 401, "message": "未认证或登录已过期", "data": null }
```

### 1.2 响应信封

所有接口（含失败）都返回同一结构：

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | `200` 成功，其余为业务失败 |
| `message` | string | 提示文案，成功恒为 `请求成功` |
| `data` | 任意 | 业务数据；无数据时为 `null` |

**业务失败仍返回 HTTP 200**（只有未认证是 HTTP 401），前端按 `code !== 200` 判定失败并提示 `message`。

| code | 含义 | 典型 message |
|---|---|---|
| `200` | 成功 | `请求成功` |
| `400` | 参数错误 | 校验失败、`缺少参数: xxx`、`参数类型错误: xxx`、`请求体缺失或格式错误`、业务校验如 `电影不存在或已下架` |
| `401` | 未认证 | `未认证或登录已过期`、`用户名或密码错误` |
| `403` | 无权限 | `无权限`（不允许操作当前登录的管理员自己） |
| `404` | 不存在 | `不存在` |
| `409` | 数据冲突 | `用户名已存在`、`排场已开场，禁止修改`、`同影厅同一开场时间已有排场`、`数据已存在（唯一键冲突）` |
| `500` | 系统异常 | `系统异常`、`图片上传失败，请稍后重试` |

> 参数校验失败时 `message` 直接来自校验注解（英文，如 `must be less than or equal to 1`）。

### 1.3 分页约定

**请求**（各分页接口通用）：

| 参数 | 类型 | 必填 | 默认 | 约束 |
|---|---|---|---|---|
| `page` | int | 否 | `1` | ≥ 1 |
| `size` | int | 否 | `10` | 1–100 |

**响应 `data`**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | long | 总条数 |
| `records` | array | 当前页数据，元素结构见各接口 |
| `page` | long | 当前页码 |
| `size` | long | 每页条数 |

```json
{ "total": 42, "records": [], "page": 1, "size": 10 }
```

### 1.4 字段格式

| 类型 | JSON 表示 | 说明 |
|---|---|---|
| 日期 | string | `yyyy-MM-dd`（如 `2026-09-14`） |
| 日期时间 | string | `yyyy-MM-dd HH:mm:ss`（入参、出参一致） |
| 金额 | number | 两位小数（如 `45.00`） |
| ID | number | 雪花 ID（订单号）例外，见 7.1 |
| 状态位 | number | 0/1（订单为 0/1/2） |

### 1.5 写操作约定

- 管理端**增改统一用 POST**（`/save`、`/{id}/status`），不使用 PUT / PATCH / DELETE
- 管理端**不提供删除接口**：电影、影院、轮播图、管理员、用户、影厅、排场一律不可删除，靠状态字段（下架/停业/禁用）或开场时间区分

---

## 2. 认证与管理员（AdminController）

### 2.1 登录

`POST /api/admin/auth/login` —— **唯一免鉴权接口**

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `username` | string | **是** | 5–32 位 | 用户名 |
| `password` | string | **是** | 8–20 位，须同时含字母和数字 | 密码 |

```json
{ "username": "admin", "password": "Aa123456" }
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `token` | string | 后续请求放在 `Authorization: Bearer {token}` |
| `admin.id` | long | 管理员 ID |
| `admin.username` | string | 用户名 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "token": "0140025d-85e7-49ba-ac7f-883fe907f5da",
    "admin": { "id": 1, "username": "admin" }
  }
}
```

> 用户名不存在、密码错误、账号被禁用一律返回 `401 用户名或密码错误`（不区分，避免账号枚举）。

### 2.2 退出登录

`POST /api/admin/auth/logout`

**请求**：无参数、无请求体

**响应 `data`**：`null`

```json
{ "code": 200, "message": "请求成功", "data": null }
```

> 服务端吊销当前 Token 并解除单设备绑定。

### 2.3 管理员列表

`GET /api/admin/admins/list`

**请求**：无参数（不分页）

**响应 `data`**：数组，元素结构：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 管理员 ID |
| `username` | string | 用户名 |
| `status` | int | 0-禁用 1-启用 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": [
    { "id": 1, "username": "admin", "status": 1, "createTime": "2026-09-14 10:00:00", "updateTime": "2026-09-14 10:00:00" }
  ]
}
```

### 2.4 新增管理员

`POST /api/admin/admins/create`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `username` | string | **是** | 5–32 位 | 全局唯一 |
| `password` | string | **是** | 8–20 位，须同时含字母和数字 | |

```json
{ "username": "cinema03", "password": "Aa123456" }
```

**响应 `data`**：`null`

**错误**：`409 用户名已存在`

> 新账号默认启用（`status = 1`），无需传状态。

### 2.5 重置密码

`POST /api/admin/admins/{id}/reset-password`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 约束 |
|---|---|---|---|---|
| `id` | path | long | **是** | 管理员 ID |

**请求体**

| 字段 | 类型 | 必填 | 约束 |
|---|---|---|---|
| `password` | string | **是** | 8–20 位，须同时含字母和数字 |

```json
{ "password": "Aa654321" }
```

**响应 `data`**：`null`

**错误**：`404 不存在`（id 不存在）

> 重置成功后该账号当前 Token 立即失效，需重新登录。

### 2.6 启用 / 禁用管理员

`POST /api/admin/admins/{id}/status`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|---|
| `id` | path | long | **是** | — | 管理员 ID |
| `status` | query | int | **是** | 0 或 1 | 0-禁用 1-启用 |

示例：`POST /api/admin/admins/2/status?status=0`

**响应 `data`**：`null`

**错误**：`403 无权限`（id 等于当前登录管理员）、`404 不存在`

> 禁用后该账号 Token 立即失效。

---

## 3. 电影管理（MovieController）

### 3.1 电影分页

`GET /api/admin/movies`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 默认 | 约束 | 说明 |
|---|---|---|---|---|---|---|
| `page` | query | int | 否 | 1 | ≥ 1 | 页码 |
| `size` | query | int | 否 | 10 | 1–100 | 每页条数 |
| `title` | query | string | 否 | — | ≤ 100 | 片名模糊搜索 |
| `status` | query | int | 否 | — | 0 或 1 | 0-下架 1-上架 |

排序：ID 倒序。

**响应 `data`**：分页结构（见 1.3），`records` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 电影 ID |
| `title` | string | 片名 |
| `poster` | string | 海报 URL |
| `description` | string | 简介 |
| `duration` | int | 时长（分钟） |
| `releaseDate` | string | 上映日期 `yyyy-MM-dd` |
| `status` | int | 0-下架 1-上架 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "total": 1,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "title": "星际穿越",
        "poster": "https://picsum.photos/seed/m1/300/420",
        "description": "人类接近灭亡，前宇航员穿越虫洞为人类寻找新家园。",
        "duration": 169,
        "releaseDate": "2026-09-09",
        "status": 1,
        "createTime": "2026-09-14 10:00:00",
        "updateTime": "2026-09-14 10:00:00"
      }
    ]
  }
}
```

### 3.2 新增 / 编辑电影

`POST /api/admin/movies/save`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | long | 否 | — | 不传=新增；传=编辑 |
| `title` | string | **是** | ≤ 100 | 片名 |
| `poster` | string | **是** | ≤ 255 | 海报 URL，先调上传接口（10.1） |
| `description` | string | **是** | ≤ 1024 | 简介 |
| `duration` | int | **是** | ≥ 1 | 时长（分钟） |
| `releaseDate` | string | **是** | `yyyy-MM-dd` | 上映日期 |
| `status` | int | **是** | 0 或 1 | 0-下架 1-上架 |

```json
{
  "id": null,
  "title": "星际穿越",
  "poster": "http://localhost:9000/cd-ticket/20260914/xxx.jpg",
  "description": "人类接近灭亡，前宇航员穿越虫洞为人类寻找新家园。",
  "duration": 169,
  "releaseDate": "2026-09-09",
  "status": 1
}
```

**响应 `data`**：`null`

**错误**：`404 不存在`（编辑时 id 不存在）

---

## 4. 影院管理（CinemaController）

### 4.1 影院分页

`GET /api/admin/cinemas`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 默认 | 约束 | 说明 |
|---|---|---|---|---|---|---|
| `page` | query | int | 否 | 1 | ≥ 1 | 页码 |
| `size` | query | int | 否 | 10 | 1–100 | 每页条数 |
| `name` | query | string | 否 | — | ≤ 100 | 影院名模糊搜索 |
| `status` | query | int | 否 | — | 0 或 1 | 0-停业 1-营业 |

排序：ID 倒序。

**响应 `data`**：分页结构，`records` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影院 ID |
| `name` | string | 影院名称 |
| `address` | string | 详细地址 |
| `status` | int | 0-停业 1-营业 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "total": 2,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "name": "CGV影城（成都SKP店）",
        "address": "四川省成都市高新区天府大道中段1388号成都SKP购物中心7层",
        "status": 1,
        "createTime": "2026-09-14 10:00:00",
        "updateTime": "2026-09-14 10:00:00"
      }
    ]
  }
}
```

### 4.2 影院详情

`GET /api/admin/cinemas/{id}`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| `id` | path | long | **是** | 影院 ID |

**响应 `data`**：单个影院对象（字段同 4.1 的 `records` 元素）

```json
{
  "code": 200,
  "message": "请求成功",
  "data": { "id": 1, "name": "CGV影城（成都SKP店）", "address": "四川省成都市高新区…", "status": 1, "createTime": "2026-09-14 10:00:00", "updateTime": "2026-09-14 10:00:00" }
}
```

**错误**：`404 不存在`

### 4.3 新增 / 编辑影院

`POST /api/admin/cinemas/save`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | long | 否 | — | 不传=新增；传=编辑 |
| `name` | string | **是** | ≤ 100 | 影院名称 |
| `address` | string | **是** | ≤ 255 | 详细地址 |
| `status` | int | **是** | 0 或 1 | 0-停业 1-营业 |

```json
{ "id": null, "name": "万达影城（金牛万达店）", "address": "四川省成都市金牛区…", "status": 1 }
```

**响应 `data`**：`null`

**错误**：`404 不存在`（编辑时 id 不存在）

---

## 5. 影厅与座位（HallController / SeatConfigService）

### 5.1 影院下的影厅列表

`GET /api/admin/cinemas/{cinemaId}/halls`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| `cinemaId` | path | long | **是** | 影院 ID |

**响应 `data`**：数组（不分页），按 ID 升序；元素结构：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影厅 ID |
| `cinemaId` | long | 所属影院 ID |
| `name` | string | 影厅名称 |
| `seatRows` | int | 座位排数 |
| `seatCols` | int | 每排座位数 |
| `status` | int | 0-禁用 1-启用 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": [
    { "id": 1, "cinemaId": 1, "name": "1号厅", "seatRows": 8, "seatCols": 10, "status": 1, "createTime": "2026-09-14 10:00:00", "updateTime": "2026-09-14 10:00:00" }
  ]
}
```

### 5.2 新增 / 编辑影厅

`POST /api/admin/halls/save`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | long | 否 | — | 不传=新增；传=编辑 |
| `cinemaId` | long | **是** | — | 所属影院 ID |
| `name` | string | **是** | ≤ 50 | 影厅名称，如 `1号厅` |
| `seatRows` | int | **是** | 1–26 | 座位排数 |
| `seatCols` | int | **是** | 1–26 | 每排座位数 |
| `status` | int | 否 | 0 或 1 | 0-禁用 1-启用；**新增不传默认 1** |

```json
{ "id": null, "cinemaId": 1, "name": "1号厅", "seatRows": 8, "seatCols": 10, "status": 1 }
```

**响应 `data`**：`null`

**错误**：`404 不存在`（编辑时 id 不存在）

> 改动 `seatRows`/`seatCols` 后座位模板规模随之变化：读取时按新尺寸补齐，未配置的座位默认启用。

### 5.3 查询座位模板

`GET /api/admin/halls/{id}/seats`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| `id` | path | long | **是** | 影厅 ID |

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `rows` | int | 排数（= 影厅 `seatRows`） |
| `cols` | int | 每排座位数（= 影厅 `seatCols`） |
| `seats` | array | **完整网格**（`rows × cols` 个元素） |

`seats` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `row` | int | 排（从 1 起） |
| `col` | int | 座（从 1 起） |
| `seatNo` | string | 展示座位号，如 `3排5座` |
| `status` | int | 0-禁用 1-启用；未单独配置的座位为 1 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "rows": 2,
    "cols": 3,
    "seats": [
      { "row": 1, "col": 1, "seatNo": "1排1座", "status": 1 },
      { "row": 1, "col": 2, "seatNo": "1排2座", "status": 0 }
    ]
  }
}
```

**错误**：`404 不存在`（影厅不存在）

### 5.4 保存座位模板（整表重写）

`POST /api/admin/halls/{id}/seats`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| `id` | path | long | **是** | 影厅 ID |

**请求体**：座位**数组**（不是对象），元素结构：

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `row` | int | **是** | 1 ≤ row ≤ 影厅排数 | 排 |
| `col` | int | **是** | 1 ≤ col ≤ 影厅每排座位数 | 座 |
| `status` | int | **是** | 0 或 1 | 0-禁用 1-启用 |

```json
[
  { "row": 1, "col": 1, "status": 1 },
  { "row": 1, "col": 2, "status": 0 }
]
```

**响应 `data`**：`null`

**错误**

| code | message | 触发条件 |
|---|---|---|
| `400` | `座位坐标超出影厅范围（8排10座）` | row/col 越界 |
| `400` | `座位状态非法` | status 不是 0/1 |
| `400` | `请求体缺失或格式错误` | 请求体不是数组 |
| `404` | `不存在` | 影厅不存在 |

> 语义为**整表覆盖**：未出现在数组中的坐标会被清除（读取时回落为启用）；传空数组等于清空模板。

---

## 6. 排场管理（ScreeningController）

### 6.1 排场分页

`GET /api/admin/cinemas/{cinemaId}/screenings`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 默认 | 约束 | 说明 |
|---|---|---|---|---|---|---|
| `cinemaId` | path | long | **是** | — | ≥ 1 | 影院 ID |
| `page` | query | int | 否 | 1 | ≥ 1 | 页码 |
| `size` | query | int | 否 | 10 | 1–100 | 每页条数 |
| `movieId` | query | long | 否 | — | ≥ 1 | 按影片筛选 |

排序：开场时间倒序。

**响应 `data`**：分页结构，`records` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 排场 ID |
| `movieId` | long | 影片 ID |
| `movieTitle` | string | 片名（联查 t_movie） |
| `hallId` | long | 影厅 ID |
| `hallName` | string | 影厅名（联查 t_hall） |
| `cinemaId` | long | 影院 ID |
| `startTime` | string | 开场时间 |
| `price` | number | 票价（元） |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "total": 6,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 6,
        "movieId": 4,
        "movieTitle": "深海寻秘",
        "hallId": 3,
        "hallName": "3号IMAX厅",
        "cinemaId": 1,
        "startTime": "2026-09-16 18:00:00",
        "price": 45.00,
        "createTime": "2026-09-14 06:03:32",
        "updateTime": "2026-09-14 06:03:32"
      }
    ]
  }
}
```

> 接口**不返回「是否已开场」**：该状态由前端按 `startTime` 与当前时间比对得出。

### 6.2 影片下拉选项

`GET /api/admin/screenings/movie-options`

**请求**：无参数

**响应 `data`**：数组，仅包含**上架**影片：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影片 ID |
| `title` | string | 片名 |

```json
{ "code": 200, "message": "请求成功", "data": [ { "id": 1, "title": "星际穿越" } ] }
```

### 6.3 新增 / 编辑排场

`POST /api/admin/screenings/save`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | long | 否 | — | 不传=新增；传=编辑 |
| `movieId` | long | **是** | — | 影片须存在且**上架** |
| `hallId` | long | **是** | — | 影厅须存在 |
| `startTime` | string | **是** | `yyyy-MM-dd HH:mm:ss`，晚于当前时间 | 开场时间 |
| `price` | number | **是** | ≥ 0.01 | 票价（元） |

```json
{ "id": null, "movieId": 1, "hallId": 1, "startTime": "2026-09-20 19:30:00", "price": 45.00 }
```

**响应 `data`**：`null`

**错误**

| code | message | 触发条件 |
|---|---|---|
| `400` | `电影不存在或已下架` | movieId 不存在或影片已下架 |
| `400` | `影厅不存在` | hallId 不存在 |
| `400` | `开场时间必须晚于当前时间` | startTime 不在未来 |
| `404` | `不存在` | 编辑时 id 不存在 |
| `409` | `排场已开场，禁止修改` | 编辑一个开场时间已过的排场 |
| `409` | `同影厅同一开场时间已有排场` | 撞唯一键 `uk_hall_start` |

> **不需要传 `cinemaId`**：服务端按影厅归属推导，保证排场与影厅同属一个影院。

---

## 7. 订单查询（只读）（OrderController）

### 7.1 影院订单分页

`GET /api/admin/cinemas/{cinemaId}/orders`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 默认 | 约束 | 说明 |
|---|---|---|---|---|---|---|
| `cinemaId` | path | long | **是** | — | ≥ 1 | 影院 ID |
| `page` | query | int | 否 | 1 | ≥ 1 | 页码 |
| `size` | query | int | 否 | 10 | 1–100 | 每页条数 |
| `orderNo` | query | long | 否 | — | — | 订单号**精确匹配** |
| `status` | query | int | 否 | — | 0/1/2 | 0-待支付 1-已支付 2-已取消 |

排序：下单时间倒序。

**响应 `data`**：分页结构，`records` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 订单 ID |
| `orderNo` | **string** | 订单号（雪花 ID，见下方说明） |
| `userId` | long | 用户 ID |
| `screeningId` | long | 排场 ID |
| `movieId` | long | 影片 ID |
| `movieTitle` | string | 片名（联查 t_movie） |
| `status` | int | 0-待支付 1-已支付 2-已取消 |
| `totalAmount` | number | 总金额 |
| `payExpireTime` | string | 支付截止时间 |
| `payTime` | string \| null | 支付时间；未支付为 `null` |
| `createTime` | string | 下单时间 |
| `updateTime` | string | 更新时间 |
| `userPhone` | string \| null | 下单用户手机号 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "total": 1,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "orderNo": "1912345678901234567",
        "userId": 3,
        "screeningId": 6,
        "movieId": 4,
        "movieTitle": "深海寻秘",
        "status": 1,
        "totalAmount": 90.00,
        "payExpireTime": "2026-09-14 06:18:32",
        "payTime": "2026-09-14 06:10:00",
        "createTime": "2026-09-14 06:03:32",
        "updateTime": "2026-09-14 06:10:00",
        "userPhone": "13800000001"
      }
    ]
  }
}
```

> **`orderNo` 是字符串不是数字**：底层是 BIGINT 雪花 ID（19 位），超出 JS `Number.MAX_SAFE_INTEGER`（约 16 位），后端强制序列化为字符串避免前端丢精度。查询参数可按数字或字符串传（服务端按 long 解析）。

---

## 8. 用户管理（UserController）

### 8.1 用户分页

`GET /api/admin/users`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 默认 | 约束 | 说明 |
|---|---|---|---|---|---|---|
| `page` | query | int | 否 | 1 | ≥ 1 | 页码 |
| `size` | query | int | 否 | 10 | 1–100 | 每页条数 |
| `phone` | query | string | 否 | — | ≤ 20 | 手机号模糊搜索 |
| `status` | query | int | 否 | — | 0 或 1 | 0-禁用 1-正常 |

排序：ID 倒序。

**响应 `data`**：分页结构，`records` 元素：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 用户 ID |
| `phone` | string | 手机号 |
| `nickname` | string | 昵称 |
| `status` | int | 0-禁用 1-正常 |
| `createTime` | string | 注册时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "total": 1,
    "page": 1,
    "size": 10,
    "records": [
      { "id": 1, "phone": "13800000001", "nickname": "影迷小王", "status": 1, "createTime": "2026-09-14 10:00:00", "updateTime": "2026-09-14 10:00:00" }
    ]
  }
}
```

### 8.2 启用 / 禁用用户

`POST /api/admin/users/{id}/status`

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|---|
| `id` | path | long | **是** | — | 用户 ID |
| `status` | query | int | **是** | 0 或 1 | 0-禁用 1-正常 |

示例：`POST /api/admin/users/1/status?status=0`

**响应 `data`**：`null`

**错误**：`404 不存在`

---

## 9. 轮播图管理（BannerController）

### 9.1 轮播图列表

`GET /api/admin/banners`

**请求**：无参数（不分页）

排序：`sort` 升序，同 `sort` 按 ID 倒序。

**响应 `data`**：数组，元素结构：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 轮播图 ID |
| `image` | string | 图片 URL |
| `linkUrl` | string | 跳转链接 |
| `sort` | int | 排序值，小者靠前 |
| `status` | int | 0-禁用 1-启用 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

```json
{
  "code": 200,
  "message": "请求成功",
  "data": [
    { "id": 1, "image": "http://localhost:9000/cd-ticket/20260914/xxx.jpg", "linkUrl": "/movies/1", "sort": 0, "status": 1, "createTime": "2026-09-14 10:00:00", "updateTime": "2026-09-14 10:00:00" }
  ]
}
```

### 9.2 新增 / 编辑轮播图

`POST /api/admin/banners/save`

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `id` | long | 否 | — | 不传=新增；传=编辑 |
| `image` | string | **是** | ≤ 255 | 图片 URL，先调上传接口（10.1） |
| `linkUrl` | string | **是** | ≤ 255 | 跳转链接，如 `/movies/1` |
| `sort` | int | **是** | ≥ 0 | 排序值，小者靠前 |
| `status` | int | **是** | 0 或 1 | 0-禁用 1-启用 |

```json
{ "id": null, "image": "http://localhost:9000/cd-ticket/20260914/xxx.jpg", "linkUrl": "/movies/1", "sort": 0, "status": 1 }
```

**响应 `data`**：`null`

**错误**：`404 不存在`（编辑时 id 不存在）

---

## 10. 文件上传（FileController）

### 10.1 上传图片

`POST /api/admin/files`

**请求**

| 项 | 值 |
|---|---|
| `Content-Type` | `multipart/form-data` |
| 表单字段 | `file`（**必填**，`MultipartFile`） |

**限制**

| 项 | 说明 |
|---|---|
| 大小 | ≤ 5MB（超出 → `400 图片不能超过 5MB`） |
| 格式 | jpg / jpeg / png / webp / gif，按**文件内容**识别（Apache Tika），扩展名与实际内容不符会被拒绝 |

**响应 `data`**：字符串（不是对象），即图片可访问 URL：

```json
{ "code": 200, "message": "请求成功", "data": "http://localhost:9000/cd-ticket/20260914/6f1c….jpg" }
```

**错误**

| code | message | 触发条件 |
|---|---|---|
| `400` | `请选择要上传的图片` | 未选择文件 / 空文件 |
| `400` | `仅支持 jpg/jpeg/png/webp/gif 图片` | 内容不是允许的图片格式 |
| `400` | `图片不能超过 5MB` | 超出大小限制 |
| `500` | `图片上传失败，请稍后重试` | MinIO 异常 |

> 上传成功后把返回的 URL 填入电影海报（3.2）或轮播图图片（9.2）字段后保存。

---

## 11. 接口速查

| # | 方法 | 路径 | 说明 |
|---|---|---|---|
| 1 | POST | `/api/admin/auth/login` | 登录（免鉴权） |
| 2 | POST | `/api/admin/auth/logout` | 退出登录 |
| 3 | GET | `/api/admin/admins/list` | 管理员列表 |
| 4 | POST | `/api/admin/admins/create` | 新增管理员 |
| 5 | POST | `/api/admin/admins/{id}/reset-password` | 重置管理员密码 |
| 6 | POST | `/api/admin/admins/{id}/status?status=` | 启用/禁用管理员 |
| 7 | GET | `/api/admin/movies` | 电影分页 |
| 8 | POST | `/api/admin/movies/save` | 新增/编辑电影 |
| 9 | GET | `/api/admin/cinemas` | 影院分页 |
| 10 | GET | `/api/admin/cinemas/{id}` | 影院详情 |
| 11 | POST | `/api/admin/cinemas/save` | 新增/编辑影院 |
| 12 | GET | `/api/admin/cinemas/{cinemaId}/halls` | 影厅列表 |
| 13 | POST | `/api/admin/halls/save` | 新增/编辑影厅 |
| 14 | GET | `/api/admin/halls/{id}/seats` | 查询座位模板 |
| 15 | POST | `/api/admin/halls/{id}/seats` | 保存座位模板（整表覆盖） |
| 16 | GET | `/api/admin/cinemas/{cinemaId}/screenings` | 排场分页 |
| 17 | GET | `/api/admin/screenings/movie-options` | 上架影片下拉选项 |
| 18 | POST | `/api/admin/screenings/save` | 新增/编辑排场 |
| 19 | GET | `/api/admin/cinemas/{cinemaId}/orders` | 订单分页（只读） |
| 20 | GET | `/api/admin/users` | 用户分页 |
| 21 | POST | `/api/admin/users/{id}/status?status=` | 启用/禁用用户 |
| 22 | GET | `/api/admin/banners` | 轮播图列表 |
| 23 | POST | `/api/admin/banners/save` | 新增/编辑轮播图 |
| 24 | POST | `/api/admin/files` | 上传图片 |
