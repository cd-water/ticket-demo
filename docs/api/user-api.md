# 电影票务系统 · 用户端（C端）API

- **服务**：`cd-ticket-app`（端口 8600，独立 Spring Boot 应用）
- **Base URL**：`/api/user`
- **前端代理**：`frontend/web` 的 vite 把 `/api` 代理到 8600（dev 端口 5600）
- **实现状态**：第 2 章认证与账号已实现（`AuthController`/`UserController`）；第 3–7 章为设计稿（对应 `docs/project-tech.md` 中的座位锁定、超时关单、双 Token、缓存三防、限流、Kafka 异步方案）

## 目录

1. [通用约定](#1-通用约定)
2. [认证与账号（已实现）](#2-认证与账号已实现)
3. [首页与电影](#3-首页与电影)
4. [影院与排场](#4-影院与排场)
5. [选座](#5-选座)
6. [订单](#6-订单)
7. [支付](#7-支付)
8. [错误码](#8-错误码)
9. [接口速查](#9-接口速查)

---

## 1. 通用约定

### 1.1 公开接口与鉴权

**免鉴权接口**：浏览类（轮播图、电影、影院、排场、座位图）+ 认证类（验证码、登录、刷新）+ 支付回调。其余接口（`/me`、订单、发起支付）必须携带 access token：

```
Authorization: Bearer {accessToken}
```

### 1.2 双 Token 无感刷新

| 项 | 值 | 说明 |
|---|---|---|
| access token | JWT，有效期 15 分钟（`jwt.access-expire-seconds`） | 放 `Authorization: Bearer` 头 |
| refresh token | 随机串，有效期 7 天（`jwt.refresh-expire-seconds`），存 Redis | 放请求体；**每次刷新轮换**（旧的立即失效） |

**刷新流程**：access token 过期/缺失时受保护接口返回 **HTTP 401**（响应体仍是统一信封）→ 客户端带 refresh token 调 `POST /api/user/auth/refresh` 换新对 → 重试原请求一次。refresh 也失效（C002）则引导重新登录。

### 1.3 响应信封

所有接口（含失败）都返回同一结构。**注意：`code` 是字符串**（区别于管理端 `cd-ticket-admin` 的 int 错误码）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | string | `0000` 成功，其余为业务失败码（见第 8 章） |
| `message` | string | 提示文案，成功恒为 `ok` |
| `data` | 任意 | 业务数据；无数据时为 `null` |

**业务失败返回 HTTP 200**（仅未认证返回 HTTP 401），前端按 `code !== '0000'` 判定失败并提示 `message`。

```json
{ "code": "0000", "message": "ok", "data": { } }
```

### 1.4 分页约定

**请求**（订单列表等分页接口通用）：

| 参数 | 类型 | 必填 | 默认 | 约束 |
|---|---|---|---|---|
| `page` | int | 否 | `1` | ≥ 1 |
| `size` | int | 否 | `10` | 1–100 |

**响应 `data`**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | long | 总条数 |
| `records` | array | 当前页数据 |
| `page` | long | 当前页码 |
| `size` | long | 每页条数 |

### 1.5 字段格式

| 类型 | JSON 表示 | 说明 |
|---|---|---|
| 日期 | string | `yyyy-MM-dd` |
| 日期时间 | string | `yyyy-MM-dd HH:mm:ss` |
| 金额 | number | 两位小数（如 `45.00`） |
| 订单号 / 支付流水号 | **string** | 雪花 ID 超过 JS 安全整数，一律按字符串传输 |
| 状态位 | number | 0/1（订单为 0/1/2） |
| 座位 | object | `{seatRow, seatCol}`，排/座从 1 起 |

### 1.6 写操作约定

- 用户端写操作：创建订单、取消订单、发起支付，语义清晰，保留 POST；不提供任何删除接口
- 客户端重试：创建订单是**天然幂等**的——同一座位第二次锁座会失败返回 C501，不会产生重复订单；发起支付同订单重复调用复用已有待支付流水

---

## 2. 认证与账号（已实现）

### 2.1 发送验证码

`POST /api/user/auth/sms-code` —— 免鉴权

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `phone` | string | **是** | `^1[3-9]\d{9}$` | 手机号 |

```json
{ "phone": "13800001234" }
```

**响应 `data`**：`null`

> 验证码 6 位数字，有效期 300 秒（`sms.code-expire-seconds`），存 Redis；发送走 Kafka 异步（`KafkaSmsSender`，本地消息表保证可靠投递）。
>
> 设计说明：当前实现未限流，`SmsCodeService` 注释预留了在网关/切面层做**滑动窗口限流**（Redis ZSet + Lua，见 project-tech），建议同一手机号 60 秒 1 条、1 小时 5 条。

### 2.2 验证码登录（未注册自动注册）

`POST /api/user/auth/login/sms` —— 免鉴权

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `phone` | string | **是** | 手机号格式 | 未注册的手机号自动注册（昵称 `用户+尾4位`） |
| `code` | string | **是** | 6 位 | 验证码，一次性，验证后即失效 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `accessToken` | string | 放 `Authorization: Bearer` |
| `refreshToken` | string | 调 `/refresh` 用，7 天有效 |
| `user.id` | long | 用户 ID |
| `user.phone` | string | 手机号 |
| `user.nickname` | string | 昵称 |
| `user.hasPassword` | boolean | 是否已设置登录密码 |

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "8f3c1a4e-...",
    "user": { "id": 1, "phone": "13800001234", "nickname": "用户1234", "hasPassword": false }
  }
}
```

### 2.3 密码登录

`POST /api/user/auth/login/password` —— 免鉴权

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `phone` | string | **是** | 手机号格式 | 必须已注册 |
| `password` | string | **是** | 8–20 位，须同时含字母和数字 | 密码 |

**响应 `data`**：同 2.2。

> 手机号不存在、密码错误、未设置密码一律返回 `C101 用户名或密码错误`（不区分，避免账号枚举）；账号被禁用返回 `C103`。

### 2.4 刷新 Token（无感续期）

`POST /api/user/auth/refresh` —— 免鉴权

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `refreshToken` | string | **是** | 登录/上次刷新返回的 refresh token |

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `accessToken` | string | 新 access token |
| `refreshToken` | string | 新 refresh token（**旧 token 立即失效**，防止重放） |

> refresh token 无效或过期：`C002 未认证或登录已过期`。

### 2.5 退出登录

`POST /api/user/auth/logout`

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `refreshToken` | string | **是** | 吊销该 refresh token（服务端同时丢弃 access） |

**响应 `data`**：`null`

### 2.6 当前用户信息

`GET /api/user/me` —— 需鉴权

**响应 `data`**：同 2.2 的 `user` 结构（`id` / `phone` / `nickname` / `hasPassword`）。

### 2.7 设置 / 修改密码

`POST /api/user/me/password` —— 需鉴权

**请求体**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|---|---|---|---|---|
| `newPassword` | string | **是** | 8–20 位，须同时含字母和数字 | 新密码 |
| `confirmPassword` | string | **是** | 同上 | 确认密码 |

**响应 `data`**：`null`

> 两次不一致：`C105`；与旧密码相同：`C104`。

---

## 3. 首页与电影

### 3.1 轮播图

`GET /api/user/banners` —— 免鉴权

**请求**：无参数

**响应 `data`**：启用中的轮播图（`status=1`，按 `sort` 升序）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 轮播图 ID |
| `image` | string | 图片 URL |
| `linkUrl` | string | 跳转链接 |

### 3.2 热映电影

`GET /api/user/movies/now` —— 免鉴权

**响应 `data`**：上架且 `release_date <= 今天` 的电影（按上映日期倒序）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 电影 ID |
| `title` | string | 片名 |
| `poster` | string | 海报 URL |
| `releaseDate` | string | 上映日期 |
| `duration` | int | 时长（分钟） |
| `minPrice` | number | 该片最低票价（排场最低价；暂无排场为 `null`，展示「待定」） |

### 3.3 待映电影

`GET /api/user/movies/coming` —— 免鉴权

**请求**、**响应 `data`**：同 3.2，但为上架且 `release_date > 今天`（按上映日期升序）。

### 3.4 全部电影

`GET /api/user/movies` —— 免鉴权

**请求**：无参数（不分页；影片总量小，一次性返回）

**响应 `data`**：上架电影数组，每项为 3.2 的结构加 `status` 字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `status` | string | `now` 热映 / `coming` 待映（由 `release_date` 与当前时间比对得出，下同） |

### 3.5 电影详情

`GET /api/user/movies/{id}` —— 免鉴权

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` / `title` / `poster` / `duration` / `releaseDate` | — | 同 3.2 |
| `status` | string | `now` / `coming` |
| `description` | string | 简介 |
| `minPrice` | number | 最低票价，同 3.2 |

> 不存在或已下架：`C201`。
>
> 设计说明：3.2/3.3 是首页/电影页高频热点数据，按 project-tech 的缓存三防方案处理（逻辑过期 + 互斥锁重建防击穿、布隆 + 空值防穿透、TTL 抖动 + 预热防雪崩）。`minPrice` 需要关联排场表，命中缓存时可能滞后最多一个缓存周期，可接受。

---

## 4. 影院与排场

### 4.1 影院列表

`GET /api/user/cinemas` —— 免鉴权

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 影院 ID |
| `name` | string | 影院名称 |
| `address` | string | 详细地址 |
| `status` | number | 1 营业 / 0 停业（列表只返回营业中的影院） |
| `todayScreeningCount` | int | 今日排片数（用于「X 场排片」角标） |

### 4.2 影院详情（含正在排片）

`GET /api/user/cinemas/{id}` —— 免鉴权

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` / `name` / `address` / `status` | — | 影院信息 |
| `movies` | array | 该影院**正在排片**的电影（去重，每项同 3.2 结构，按最近开场时间倒序） |

> 不存在或已停业：`C301`。

### 4.3 影院排片列表

`GET /api/user/cinemas/{id}/screenings` —— 免鉴权

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `movieId` | long | 否 | 只看某部电影的场次（影院详情页默认选中第一部有排片的电影） |
| `date` | string | 否 | `yyyy-MM-dd`，默认今天 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 排场 ID |
| `movieId` / `movieTitle` / `moviePoster` | — | 电影信息 |
| `hallId` / `hallName` | — | 影厅信息 |
| `startTime` | string | 开场时间 |
| `endTime` | string | 散场时间（开场 + 影片时长） |
| `price` | number | 票价 |

> 排场需满足：电影上架、影厅启用、开场时间未过。

### 4.4 排场详情

`GET /api/user/screenings/{id}` —— 免鉴权

**响应 `data`**：同 4.3 单条 + `cinemaId` / `cinemaName`（选座页头部展示、分享/深链用）。

> 不存在：`C401`。

---

## 5. 选座

### 5.1 座位图

`GET /api/user/screenings/{id}/seats` —— 免鉴权

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `screeningId` | long | 排场 ID |
| `hallId` / `hallName` | — | 影厅信息 |
| `seatRows` | int | 排数 |
| `seatCols` | int | 每排座位数 |
| `seats` | array | 座位列表（见下） |

`seats[]`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `seatRow` | int | 排（1 起） |
| `seatCol` | int | 座（1 起） |
| `seatNo` | string | 展示座位号（如 `3排5座`，来自座位模板） |
| `status` | number | **0** 可用 / **1** 已售（已支付）/ **2** 锁定中（待支付，15 分钟内）/ **3** 不可售（模板禁用） |

```json
{
  "code": "0000", "message": "ok",
  "data": {
    "screeningId": 1001, "hallId": 6, "hallName": "3号激光厅",
    "seatRows": 8, "seatCols": 10,
    "seats": [
      { "seatRow": 1, "seatCol": 1, "seatNo": "1排1座", "status": 0 },
      { "seatRow": 3, "seatCol": 5, "seatNo": "3排5座", "status": 1 }
    ]
  }
}
```

> 座位状态 = 座位模板（`t_seat_config`）+ **已售**（`t_order` 已支付 + `t_order_item`）+ **锁定**（Redis Bitmap `seat:lock:{screeningId}`，位下标 `(seatRow-1)*seatCols + (seatCol-1)`）。已支付座位同时存在 DB 与 Bitmap，以 DB 为准（Bitmap 是支付截止 TTL 的临时锁，支付成功后由回调/关单任务释放）。
>
> 选座页高频轮询该接口，走缓存三防方案。

---

## 6. 订单

### 6.1 创建订单（锁座 + 下单，原子）

`POST /api/user/orders` —— 需鉴权

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `screeningId` | long | **是** | 排场 ID |
| `seats` | array | **是** | 座位列表（1–6 个） |
| `seats[].seatRow` | int | **是** | 排 |
| `seats[].seatCol` | int | **是** | 座 |

```json
{
  "screeningId": 1001,
  "seats": [ { "seatRow": 3, "seatCol": 5 }, { "seatRow": 3, "seatCol": 6 } ]
}
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | long | 订单 ID |
| `orderNo` | **string** | 订单号（雪花 ID，按字符串传输） |
| `status` | number | 恒为 0 待支付 |
| `totalAmount` | number | 总金额（票价 × 座位数） |
| `payExpireTime` | string | 支付截止时间（当前时间 + 15 分钟） |

```json
{
  "code": "0000", "message": "ok",
  "data": {
    "id": 10086, "orderNo": "1932418900070047744",
    "status": 0, "totalAmount": 116.00, "payExpireTime": "2026-09-14 20:15:00"
  }
}
```

**失败**：

| code | 场景 |
|---|---|
| `C402` | 场次已开场或已结束 |
| `C505` | 座位不在模板内 / 模板已禁用 / 数量超限 |
| `C501` | 座位已被占用（部分占用也整体失败，`data` 返回冲突座位列表 `[{seatRow, seatCol}]`，前端定位到具体座位） |

**锁座与关单设计**（对应 project-tech「座位锁定」「超时关单」）：

1. **原子锁座**：Redis Bitmap + Lua 脚本对请求的全部座位做「检查空闲 → 置位」的 all-or-nothing 操作，一步杜绝「一座多人」
2. **下单落库**：锁座成功 → 插入 `t_order`（待支付，`pay_expire_time = now + 15min`）+ `t_order_item`；锁 TTL 与支付截止对齐（15 分钟），即使关单任务丢失也能自动过期释放
3. **延迟关单**：以 `pay_expire_time` 为 score 写入 Redis ZSet 延迟队列，轮询任务取出到期订单 → **数据库乐观锁**（`version`）把待支付置为已取消 → 释放 Bitmap 座位
4. 已支付订单不可取消；支付回调与关单任务的竞态由乐观锁解决（见 7.3）

### 6.2 我的订单列表

`GET /api/user/orders?page=&size=&status=` —— 需鉴权

**请求参数**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `page` / `size` | int | 否 | 1 / 10 | 分页（见 1.4） |
| `status` | int | 否 | 全部 | 0 待支付 / 1 已支付 / 2 已取消（「我的订单」页签） |

**响应 `data`**：`PageResult`（`records` 元素）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` / `orderNo` | — | 订单 ID / 订单号（字符串） |
| `status` | number | 0/1/2 |
| `totalAmount` | number | 总金额 |
| `movieId` / `movieTitle` / `moviePoster` | — | 电影信息（列表卡图） |
| `cinemaName` / `hallName` / `startTime` | — | 场次信息 |
| `seats` | array | 座位号列表（`["3排5座", "3排6座"]`） |
| `payExpireTime` | string | 支付截止（待支付时展示倒计时） |
| `payTime` | string | 支付时间（已支付） |
| `createTime` | string | 下单时间 |

### 6.3 订单详情

`GET /api/user/orders/{id}` —— 需鉴权（仅本人订单）

**响应 `data`**：6.2 单条 + `screeningId` / `items`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `items[]` | array | 座位明细：`seatRow` / `seatCol` / `seatNo` / `price` |
| `payExpireTime` | string | 支付截止（**支付页倒计时用；前端到点自动关闭支付入口，服务端仍以关单任务为准**） |

> 不存在或非本人：`C502`。

### 6.4 取消订单

`POST /api/user/orders/{id}/cancel` —— 需鉴权（仅本人）

**请求**：无请求体

**响应 `data`**：`null`

> 仅待支付可取消：`C503`。取消成功即释放座位（从延迟队列移除 + 释放 Bitmap 位），其他用户立即可见。

---

## 7. 支付

### 7.1 发起支付

`POST /api/user/orders/{id}/pay` —— 需鉴权（仅本人）

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `channel` | number | **是** | 1 支付宝 / 2 微信 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|---|---|---|
| `paymentId` | long | 支付流水 ID |
| `paymentNo` | **string** | 支付流水号（雪花 ID，字符串） |
| `channel` | number | 1/2 |
| `amount` | number | 支付金额 |
| `payParams` | object | 渠道拉起参数（由具体 SDK 决定，如支付宝 `orderStr` / 微信 `prepay_id`；接入模拟支付网关时可为 `{mockUrl}`） |

> 同一订单重复发起：复用已有**待支付**流水（幂等，见 1.6）；订单已支付：`C603`；订单已超时关闭：`C504`（前端回到订单列表）。
>
> 支付截止后发起支付：服务端以 `pay_expire_time` 为准拒绝（`C504`），倒计时仅为前端体验。

### 7.2 支付回调（第三方 → 服务端）

`POST /api/pay/notify/{channel}` —— 免鉴权（`channel`：`alipay` / `wechat`）

**请求**：渠道原始报文（含签名，服务端验签后解析，`notify_raw` 原样落库）

**响应**：按渠道约定返回 `success` / `fail`（重试由渠道驱动）

**处理流程**：

1. 校验签名、幂等（`uk_channel_out_trade_no`）→ 更新 `t_payment`（成功，记 `trade_no` / `paid_time`）
2. 乐观锁更新 `t_order` 待支付 → 已支付（记 `pay_time`），释放 Bitmap 座位（已售状态由 DB 表达）
3. 写本地消息表（`t_local_message`），定时任务投递 Kafka：票房统计等非核心流程
4. 若订单已被关单任务取消（支付与关单竞态）→ **自动发起退款**（`t_refund` + 本地消息表可靠投递），用户无需操作

### 7.3 支付结果查询（轮询约定）

**不单独提供查询接口**：客户端在拉起支付后轮询 `GET /api/user/orders/{id}`（6.3），`status` 变为 1 即支付成功；轮询至 `payExpireTime` 为止，超时后页面回到订单列表。

---

## 8. 错误码

### 8.1 通用

| code | HTTP | 含义 | 典型 message |
|---|---|---|---|
| `0000` | 200 | 成功 | `ok` |
| `C001` | 200 | 参数错误 | 校验失败（`message` 直接来自校验注解，英文） |
| `C002` | **401**（未认证）/ 200（refresh 失效） | 未认证或登录已过期 | `Unauthorized` |
| `C003` | 200 | 无权限 | `Forbidden` |
| `C004` | 200 | 不存在 | `Not Found` |
| `C005` | 200 | 数据冲突 | `Conflict` |
| `S001` | 200 | 系统异常 | `Internal Server Error` |
| `T001` | 200 | 短信服务异常 | `短信服务异常` |

### 8.2 业务码（预留分组，实现时按此填充）

| code | 含义 | 典型 message |
|---|---|---|
| `C101` | 登录失败 | `用户名或密码错误` |
| `C102` | 验证码错误或已过期 | `验证码错误或已过期` |
| `C103` | 用户已禁用 | `用户已禁用` |
| `C104` | 新密码与旧密码相同 | `新密码与旧密码相同` |
| `C105` | 两次输入密码不一致 | `两次输入密码不一致` |
| `C201` | 电影不存在或已下架 | `电影不存在或已下架` |
| `C301` | 影院不存在或已停业 | `影院不存在或已停业` |
| `C401` | 排场不存在 | `排场不存在` |
| `C402` | 场次已开场或已结束 | `场次已开场或已结束` |
| `C501` | 座位已被占用 | `座位已被占用`（`data` 返回冲突座位） |
| `C502` | 订单不存在 | `订单不存在` |
| `C503` | 订单状态不允许该操作 | `当前订单状态不允许该操作` |
| `C504` | 订单已超时关闭 | `订单已超时关闭，请重新下单` |
| `C505` | 座位参数非法 | `座位参数非法` |
| `C601` | 支付渠道参数非法 | `支付渠道参数非法` |
| `C602` | 支付单不存在 | `支付单不存在` |
| `C603` | 订单状态不允许支付 | `当前订单状态不允许支付` |

---

## 9. 接口速查

| # | 方法 | 路径 | 鉴权 | 状态 |
|---|---|---|---|---|
| 1 | POST | `/api/user/auth/sms-code` | 免 | ✅ 已实现 |
| 2 | POST | `/api/user/auth/login/sms` | 免 | ✅ 已实现 |
| 3 | POST | `/api/user/auth/login/password` | 免 | ✅ 已实现 |
| 4 | POST | `/api/user/auth/refresh` | 免 | ✅ 已实现 |
| 5 | POST | `/api/user/auth/logout` | 免（需 refreshToken） | ✅ 已实现 |
| 6 | GET | `/api/user/me` | 需 | ✅ 已实现 |
| 7 | POST | `/api/user/me/password` | 需 | ✅ 已实现 |
| 8 | GET | `/api/user/banners` | 免 | 📝 设计稿 |
| 9 | GET | `/api/user/movies/now` | 免 | 📝 设计稿 |
| 10 | GET | `/api/user/movies/coming` | 免 | 📝 设计稿 |
| 11 | GET | `/api/user/movies` | 免 | 📝 设计稿 |
| 12 | GET | `/api/user/movies/{id}` | 免 | 📝 设计稿 |
| 13 | GET | `/api/user/cinemas` | 免 | 📝 设计稿 |
| 14 | GET | `/api/user/cinemas/{id}` | 免 | 📝 设计稿 |
| 15 | GET | `/api/user/cinemas/{id}/screenings` | 免 | 📝 设计稿 |
| 16 | GET | `/api/user/screenings/{id}` | 免 | 📝 设计稿 |
| 17 | GET | `/api/user/screenings/{id}/seats` | 免 | 📝 设计稿 |
| 18 | POST | `/api/user/orders` | 需 | 📝 设计稿 |
| 19 | GET | `/api/user/orders` | 需 | 📝 设计稿 |
| 20 | GET | `/api/user/orders/{id}` | 需 | 📝 设计稿 |
| 21 | POST | `/api/user/orders/{id}/cancel` | 需 | 📝 设计稿 |
| 22 | POST | `/api/user/orders/{id}/pay` | 需 | 📝 设计稿 |
| 23 | POST | `/api/pay/notify/{channel}` | 免 | 📝 设计稿 |

**设计取舍备注**：

- 不做「预锁座 + 确认下单」两步流程（淘宝式购物车场景才需要）；用户端选座即下单，锁座与下单原子完成，`稍后支付` 就是「不调支付接口直接离开」，与 demo 交互一致
- 支付结果查询复用订单详情，不单独设计轮询接口
- 电影/影院列表不分页（总量小），订单列表分页
