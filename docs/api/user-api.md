# 电影票务系统 · 用户端（C端）API

- **服务**：`cd-ticket-app`（端口 8600）
- **Base URL**：`/api/user`
- **响应信封**：所有接口返回 `{ code, message, data }`；`code = "0000"` 成功，否则失败；业务失败 HTTP 200，未认证 HTTP 401；前端按 `code !== '0000'` 提示 `message`

## 通用分页

- **请求参数**：`page`（默认 1，≥1）、`size`（默认 10，最大 50）
- **响应 `data`**：`{ total, records, page, size }`

## 接口

### 轮播图（首页）

`GET /api/user/banners` —— 免鉴权，无参数

**响应 `data`**：启用中的轮播图（`status=1`，按 `sort` 升序）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 轮播图 ID |
| `image` | string | 图片 URL |
| `linkUrl` | string | 跳转链接 |

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": [
    { "id": 1, "image": "https://cdn.example.com/banner1.jpg", "linkUrl": "/movies/1" },
    { "id": 2, "image": "https://cdn.example.com/banner2.jpg", "linkUrl": "/movies/2" }
  ]
}
```

### 热映电影（首页）

`GET /api/user/movies/hot` —— 免鉴权，无参数

**响应 `data`**：`[{ id, title, poster }]`（已上映，**最多 8 部**，上映日期倒序）

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": [
    { "id": 1, "title": "流浪地球 3", "poster": "https://cdn.example.com/poster1.jpg" },
    { "id": 2, "title": "深海传说", "poster": "https://cdn.example.com/poster2.jpg" }
  ]
}
```

### 待映电影（首页）

`GET /api/user/movies/coming` —— 免鉴权，无参数

**响应 `data`**：`[{ id, title, poster }]`（未上映，**最多 8 部**，上映日期升序）

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": [
    { "id": 5, "title": "千里江山图", "poster": "https://cdn.example.com/poster5.jpg" },
    { "id": 6, "title": "星际拓荒者", "poster": "https://cdn.example.com/poster6.jpg" }
  ]
}
```

### 电影票房榜（首页）

`GET /api/user/movies/box-office` —— 免鉴权，无参数

**响应 `data`**：`[{ id, title, boxOffice }]`（**今日**票房 top 10，数组顺序即排名；`boxOffice` 单位元，两位小数）

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": [
    { "id": 1, "title": "流浪地球 3", "boxOffice": 1284000.00 },
    { "id": 2, "title": "深海传说", "boxOffice": 960000.00 }
  ]
}
```

### 电影列表（电影页，分页，热映/待映切换）

`GET /api/user/movies?status=&page=&size=` —— 免鉴权

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `status` | string | **是** | `hot` 热映 / `coming` 待映 |
| `page` / `size` | int | 否 | 分页（见通用分页） |

**响应 `data`**：分页结构，`records` 为 `[{ id, title, poster }]`；`hot` 上映日期倒序、`coming` 升序

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "total": 42,
    "records": [
      { "id": 1, "title": "流浪地球 3", "poster": "https://cdn.example.com/poster1.jpg" },
      { "id": 2, "title": "深海传说", "poster": "https://cdn.example.com/poster2.jpg" }
    ],
    "page": 1,
    "size": 10
  }
}
```

### 电影详情

`GET /api/user/movies/{id}` —— 免鉴权

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | long | **是** | 电影 ID（路径参数） |

**响应 `data`**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 电影 ID |
| `title` | string | 片名 |
| `poster` | string | 海报 URL |
| `description` | string | 简介 |
| `duration` | int | 时长（分钟） |
| `releaseDate` | string | 上映日期（`yyyy-MM-dd`） |
| `showStatus` | string | `hot` 热映 / `coming` 待映（前端据此显示「购票」/「想看」） |

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "id": 1,
    "title": "流浪地球 3",
    "poster": "https://cdn.example.com/poster1.jpg",
    "description": "太阳危机后的第 37 年，地球踏上了新的航程……",
    "duration": 173,
    "releaseDate": "2026-05-01",
    "showStatus": "hot"
  }
}
```

> 电影不存在或已下架：`code = C004`（`NOT_FOUND`），`data = null`。

### 影院列表（影院页，分页）

`GET /api/user/cinemas?page=&size=` —— 免鉴权

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` / `size` | int | 否 | 分页（见通用分页） |

**响应 `data`**：分页结构，`records` 为 `[{ id, name, address, status }]`（停业/营业都返回，按 `id` 升序）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影院 ID |
| `name` | string | 影院名称 |
| `address` | string | 详细地址 |
| `status` | number | 0 停业 / 1 营业（前端显示「营业中/今日停业」角标） |

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "total": 12,
    "records": [
      { "id": 1, "name": "CD-TICKET·天府广场店", "address": "成都市锦江区人民东路10号", "status": 1 },
      { "id": 3, "name": "CD-TICKET·宽窄巷子店", "address": "成都市青羊区长顺上街127号", "status": 0 }
    ],
    "page": 1,
    "size": 10
  }
}
```

### 影院详情

`GET /api/user/cinemas/{id}` —— 免鉴权

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | long | **是** | 影院 ID（路径参数） |

**响应 `data`**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影院 ID |
| `name` | string | 影院名称 |
| `address` | string | 详细地址 |
| `status` | number | 0 停业 / 1 营业（停业影院也正常返回，前端展示停业状态） |
| `movies[]` | array | 正在排片的电影（去重，页面头部横向海报行，点击切换）：`{ id, title, poster }` |

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "id": 1,
    "name": "CD-TICKET·天府广场店",
    "address": "成都市锦江区人民东路10号",
    "status": 1,
    "movies": [
      { "id": 1, "title": "流浪地球 3", "poster": "https://cdn.example.com/poster1.jpg" },
      { "id": 2, "title": "深海传说", "poster": "https://cdn.example.com/poster2.jpg" }
    ]
  }
}
```

> 影院不存在：`code = C004`（`NOT_FOUND`），`data = null`。

### 影院排片列表

`GET /api/user/cinemas/{id}/screenings?movieId=` —— 免鉴权

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | long | **是** | 影院 ID（路径参数） |
| `movieId` | long | 否 | 只看某部电影的场次（默认返回全部） |

**响应 `data`**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 排场 ID（选座购票用） |
| `movieId` | long | 电影 ID |
| `hallId` / `hallName` | — | 影厅 |
| `startTime` | string | 开场时间（`yyyy-MM-dd HH:mm:ss`） |
| `endTime` | string | 散场时间（开场 + 影片时长） |
| `price` | number | 票价 |

**响应示例**

```json
{
  "code": "0000",
  "message": "ok",
  "data": [
    { "id": 1001, "movieId": 1, "hallId": 6, "hallName": "3号激光厅", "startTime": "2026-09-15 14:20:00", "endTime": "2026-09-15 17:13:00", "price": 48.00 },
    { "id": 1002, "movieId": 1, "hallId": 1, "hallName": "1号巨幕厅", "startTime": "2026-09-15 19:30:00", "endTime": "2026-09-15 22:23:00", "price": 58.00 }
  ]
}
```

## 通用模型

### `UserInfo`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 用户 ID |
| `phone` | string | 手机号 |
| `nickname` | string | 昵称 |

### `LoginResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `accessToken` | string | JWT，900s 过期，鉴权头 `Authorization: Bearer <token>` |
| `refreshToken` | string | UUID，7d 过期，存 Redis；用于刷新与登出 |
| `user` | object | `UserInfo` |

### `RefreshResponse`

| 字段 | 类型 | 说明 |
|---|---|---|
| `accessToken` | string | 新的 access token |
| `refreshToken` | string | 新的 refresh token（旧 token 已轮换） |

## 认证（`/api/user/auth`）

### 发送短信验证码

`POST /api/user/auth/sms-code` —— 免鉴权

**请求体**：

```json
{ "phone": "13800138000" }
```

> 短信验证码 300s 过期。

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": null
}
```

### 短信验证码登录

`POST /api/user/auth/login/sms` —— 免鉴权

**请求体**：

```json
{ "phone": "13800138000", "code": "123456" }
```

**响应 `data`**：`LoginResponse`

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzE1MTI0MDAwLCJleHAiOjE3MTUxMjQ5MDB9.signature",
    "refreshToken": "8e3a1b40-2c9a-4d5e-bb1e-9a2c3f4d5e6a",
    "user": { "id": 1, "phone": "13800138000", "nickname": "user_8000" }
  }
}
```

> - 验证码错误或过期：`code = C102`
> - 手机号未注册时自动注册并登录,昵称 `user_<手机后4位>`

### 密码登录

`POST /api/user/auth/login/password` —— 免鉴权

**请求体**：

```json
{ "phone": "13800138000", "password": "abcd1234" }
```

**响应 `data`**：`LoginResponse`

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzE1MTI0MDAwLCJleHAiOjE3MTUxMjQ5MDB9.signature",
    "refreshToken": "8e3a1b40-2c9a-4d5e-bb1e-9a2c3f4d5e6a",
    "user": { "id": 1, "phone": "13800138000", "nickname": "user_8000" }
  }
}
```

> 用户名/密码错误、用户已禁用统一返回:`code = C101`(不区分原因,避免泄露账号状态)。

### 刷新 Token

`POST /api/user/auth/refresh` —— 免鉴权(凭 refreshToken)

**请求体**：

```json
{ "refreshToken": "<uuid>" }
```

**响应 `data`**：`RefreshResponse`

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzE1MTI0MDAwLCJleHAiOjE3MTUxMjQ5MDB9.signature",
    "refreshToken": "new-uuid-3c1d-4e5f-a678-9b0c1d2e3f4a"
  }
}
```

> - refreshToken 无效或过期：`code = C002`
> - 旧的 refreshToken 在响应返回后失效(轮换),下次刷新需用新的

### 登出

`POST /api/user/auth/logout` —— **需鉴权**

**请求体**：

```json
{ "refreshToken": "<uuid>" }
```

> 仅销毁服务端 refreshToken,前端需自行丢弃 accessToken(短期有效,自然过期)。

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": null
}
```

## 用户（`/api/user`）

### 当前用户信息

`GET /api/user/me` —— **需鉴权**,无参数

**响应 `data`**：`UserInfo`

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": { "id": 1, "phone": "13800138000", "nickname": "user_8000" }
}
```

### 更新个人资料

`POST /api/user/me` —— **需鉴权**

**请求体**：

```json
{ "nickname": "新昵称" }
```

> 字段说明：
> - `nickname`：1-50 字符（与 DB `t_user.nickname VARCHAR(50)` 对齐）
>
> 当前仅支持昵称；后续要加字段（头像、性别、生日等）直接扩请求体和服务层 `AuthService.updateProfile`。

**响应 `data`**：`UserInfo`

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": { "id": 1, "phone": "13800138000", "nickname": "新昵称" }
}
```

### 修改密码

`POST /api/user/me/password` —— **需鉴权**

**请求体**：

```json
{
  "newPassword": "newpass1",
  "confirmPassword": "newpass1"
}
```

> - 两次密码不一致：`code = C105`
> - 与旧密码相同：`code = C104`
> - 密码规则：8-20 位,必须含字母和数字(`@Pattern` 校验)

**响应示例**：

```json
{
  "code": "0000",
  "message": "ok",
  "data": null
}
```

