# CD-TICKET API 接口文档

> 版本：v0.4（认证模块 + 管理端业务接口已全部实现并通过真实环境冒烟；C 端公开浏览/下单接口待开发）。
> 服务：后端单体 JAR，端口 8600，统一前缀 `/api`。

## 全局约定

- **统一响应体**：`{ "code": "0000", "message": "ok", "data": { } }`
  - `code="0000"` 成功；非 0 失败，`message` 为可读提示。
  - `data` 为具体数据，无返回时为 `null`。
- **鉴权**：
  - C 端：请求头 `Authorization: Bearer {accessToken}`。
  - B 端：请求头 `Authorization: Bearer {adminToken}`。
  - 未携带/失效/过期 Token → `code="C002"`（HTTP 401）。
- **Content-Type**：`application/json`（除明确标注）。

## 错误码（阿里巴巴异常体系规范）

格式：**[类别字母] + 3 位数字**（SUCCESS 单独 `0000`）

| 字母 | 含义 |
|------|------|
| `O` | 成功（恒为 `0000`） |
| `C` | 客户端错误（4xx 等价） |
| `S` | 服务端错误（5xx 等价） |
| `T` | 第三方错误 |

数字位 `MSS` 中 `M` 为业务模块（0=公共, 1=用户, 2=管理员, 3=影片, 4=影院, 5=排场, 6=订单, 7=支付）。

| code | 场景 |
|------|------|
| `0000` | 成功 |
| `C001` | 参数错误（缺失/非法），含手机号格式、密码复杂度、Body 解析失败等 |
| `C002` | 未认证 / Token 无效或过期 |
| `C003` | 无权限（角色不符 / 跨影院越界） |
| `C004` | 用户名或密码错误（C 端登录/B 端登录**统一**，避免用户名枚举） |
| `C101` | 验证码错误或已过期 |
| `C102` | 用户已禁用 |
| `C103` | 新密码与旧密码相同 |
| `C104` | 两次输入密码不一致 |
| `C201` | 用户名已存在（管理员新增/修改撞 `uk_username`） |
| `C202` | 影院管理员必须绑定影院 |
| `C203` | 不能操作当前登录管理员（改/删/禁自己） |
| `C204` | 记录不存在 |
| `C501` | 同影厅同一开场时间已有排场 |
| `C502` | 该影厅已有排场，禁止删除 |
| `C503` | 排场已开场，禁止修改/删除 |
| `S001` | 系统异常 |

---

## 校验规则

| 字段 | 规则 | 正则 |
|------|------|------|
| `phone` | 1 开头 11 位 | `^1[3-9]\d{9}$` |
| `password` / `newPassword` / `confirmPassword` | 8-20 位，含字母 + 数字 | `^(?=.*[A-Za-z])(?=.*\d).{8,20}$` |
| 管理端 `title` / `name` | 非空，≤100 字（片名/影院名）；影厅名 ≤50 字 | — |
| 管理端 `seatRows` / `seatCols` | 整数 1~26 | — |
| 管理端 `price` | 正数（最小 0.01） | — |
| 管理端 `status` | 整数 0/1 | — |

校验失败 → `code="C001"`，`message` 为具体规则描述。

**异常构造约定（后端开发注意）**：`BizException(String message, String code)` —— 消息在前、错误码在后，携带自定义文案时写 `new BizException("文案", ResultCode.X.getCode())`。

---

## 一、C 端认证（`/api/user`）

### 1.1 发送验证码

- **方法/路径**：`POST /api/user/auth/sms-code`
- **鉴权**：公开

请求：

```json
{ "phone": "13800138000" }
```

响应（成功）：

```json
{ "code": "0000", "message": "ok", "data": null }
```

说明：
- 手机号格式校验失败 → `code="C001"`（消息「手机号格式非法」）。
- 验证码随机 6 位数字，存 Redis `sms:{phone}`，TTL 5 分钟（`sms.code-expire-seconds`，**当前硬编码 300s**）。
- 短信发送**不限流**（如需限流，统一在 API 网关 / 切面层做）。
- 短信下发走 Kafka topic `sms.send`（`KafkaSmsSender` → `SmsConsumer`），消费者模拟真实下发并打日志 `[SMS-SENT] phone=... code=...`。

错误码：`C001` 参数错误 / `S001` 系统异常

### 1.2 验证码登录（静默注册）

- **方法/路径**：`POST /api/user/auth/login/sms`
- **鉴权**：公开

请求：

```json
{ "phone": "13800138000", "code": "123456" }
```

响应（成功）：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "a1b2c3d4-e5f6-...",
    "user": { "id": 1, "phone": "13800138000", "nickname": "用户8000", "hasPassword": false }
  }
}
```

说明：
- 手机号不存在 → 静默注册（nickname 默认「用户+手机尾号」），随后签发 Token。
- 手机号格式校验失败 → `code="C001"`。
- 验证码错误/过期 → `code="C101"`。
- 用户被禁用（status≠1）→ `code="C102"`。
- `user.hasPassword`：是否已设置密码（前端据此决定「设置密码」入口文案）。

错误码：`C001` / `C101` / `C102`

### 1.3 密码登录

- **方法/路径**：`POST /api/user/auth/login/password`
- **鉴权**：公开

请求：

```json
{ "phone": "13800138000", "password": "Aa123456" }
```

响应（成功）：同 1.2，`data.user` 含 `id/phone/nickname/hasPassword`。

说明：
- 手机号/密码格式校验失败 → `code="C001"`。
- 手机号不存在 / 密码错误 / 用户被禁用 → 统一 `code="C004"`（不暴露账号是否存在，不暴露禁用状态）。

错误码：`C001` / `C004`

### 1.4 刷新 Token

- **方法/路径**：`POST /api/user/auth/refresh`
- **鉴权**：Refresh Token（请求体中携带，不走 Authorization 头）

请求：

```json
{ "refreshToken": "a1b2c3d4-e5f6-..." }
```

响应（成功）：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "新的-refreshToken-字符串"
  }
}
```

说明：
- 校验 refreshToken 存在于 Redis `refresh:token:{token}`（值为 userId），且未过期（轮换机制）。
- 校验通过：旧 Refresh 作废，签发**新 Access + 新 Refresh**（Refresh 单次使用）。
- 无效/已过期 → `code="C002"`（Redis 不存在该 token 即视同 Token 无效）。

### 1.5 退出登录

- **方法/路径**：`POST /api/user/auth/logout`
- **鉴权**：Access Token（Authorization 头）

请求：

```json
{ "refreshToken": "a1b2c3d4-e5f6-..." }
```

响应（成功）：

```json
{ "code": "0000", "message": "ok", "data": null }
```

说明：
- **请求体必填** `refreshToken`：按该 token 直接吊销（删除 Redis `refresh:token:{token}`）。
- Access Token 到期自然失效（A 方案，最多残留 15 分钟，见设计文档）。

### 1.6 当前用户信息

- **方法/路径**：`GET /api/user/me`
- **鉴权**：Access Token

响应（成功）：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "用户8000",
    "hasPassword": true
  }
}
```

说明：
- `hasPassword`：用户是否已设置密码（前端据此决定「设置密码」入口文案）。
- Token 无效/过期 → `code="C002"`；用户被禁用 → `code="C102"`。

### 1.7 修改密码

- **方法/路径**：`POST /api/user/me/password`
- **鉴权**：Access Token

请求：

```json
{ "newPassword": "NewPass123", "confirmPassword": "NewPass123" }
```

响应（成功）：

```json
{ "code": "0000", "message": "ok", "data": null }
```

说明：
- 登录态即身份证明，无需旧密码/验证码。
- 后端校验（由 Bean Validation 在 Controller 入口完成）：
  - `newPassword` / `confirmPassword` 非空 + 满足 8-20 位、含字母+数字 → 否则 `code="C001"`
  - `newPassword` == `confirmPassword`，否则 `code="C104"`
  - 新密码与原密码相同（已设置密码时）→ `code="C103"`
- 校验通过后写入 BCrypt 密码，`hasPassword` 变为 true。

错误码：`C001` / `C103` / `C104`

---

## 二、B 端认证（`/api/admin/auth`）

### 2.1 管理员登录

- **方法/路径**：`POST /api/admin/auth/login`
- **鉴权**：公开

请求：

```json
{ "username": "admin", "password": "Aa123456" }
```

响应（成功）：

```json
{
  "code": "0000",
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "admin": { "id": 1, "username": "admin", "role": 0, "cinemaId": 0 }
  }
}
```

说明：
- 用户名/密码错误 / 管理员被禁用（status≠1）/ 不存在 → **统一** `code="C004"`（避免用户名枚举）。
- 登录成功签发 JWT（`a{id}` 类型），并存 Redis `admin:token:{adminId}`，TTL = access 有效期。
- **单设备**：新登录覆盖旧 Token，旧 Token 立即失效（后续请求 401）。

错误码：`C004`

### 2.2 管理员退出登录

- **方法/路径**：`POST /api/admin/auth/logout`
- **鉴权**：Admin Token

请求：

```json
{}
```

响应（成功）：

```json
{ "code": "0000", "message": "ok", "data": null }
```

说明：删除 Redis `admin:token:{adminId}`，Token 即时吊销。

---

## 管理端通用约定

**权限矩阵**（所有 `/api/admin/**` 接口均需 Admin Token）：

| 接口组 | 超级管理员（role=0） | 影院管理员（role=1） |
|--------|:---:|:---:|
| `/api/admin/movies/**`、`/banners/**`、`/cinemas/**`、`/users/**`、`/files` | ✅ | ❌ `C003` |
| `/api/admin/halls/**`、`/screenings/**` | ❌ `C003` | ✅（强制本影院） |
| `/api/admin/orders` | ✅（全部） | ✅（强制本影院） |
| `/api/admin/admins` | 全量 CRUD | 仅本影院**列表 + 新增**（改/删 → `C003`） |

**分页约定**：请求 `?page=1&size=10`（`page≥1`、`size∈[1,100]`，越界 → `C001`）；响应 `data` 恒为：

```json
{ "total": 4, "records": [ ], "page": 1, "size": 10 }
```

**逻辑删除**：所有删除均为逻辑删除（`deleted=1`），列表与详情自动过滤。

---

## 三、管理端 - 电影管理（`/api/admin/movies`）

> 权限：超级管理员。

### 3.1 分页查询

- **方法/路径**：`GET /api/admin/movies?page=1&size=10&title=星际&status=1`（`title`/`status` 可空；title 模糊、status 精确；按 create_time 倒序）

响应 `data.records[]`：

```json
{
  "id": 1,
  "title": "星际穿越",
  "poster": "https://picsum.photos/seed/m1/300/420",
  "description": "人类接近灭亡……",
  "duration": 169,
  "releaseDate": "2026-09-07",
  "status": 1,
  "createTime": "2026-09-12T04:35:02"
}
```

### 3.2 新增

- **方法/路径**：`POST /api/admin/movies`

```json
{ "title": "流浪地球3", "poster": "http://…/p.jpg", "description": "简介",
  "duration": 173, "releaseDate": "2026-09-10", "status": 1 }
```

说明：`title` 非空；`duration` > 0；`status` 0 下架 / 1 上架；`poster`、`description`、`releaseDate` 可空。

### 3.3 修改

- **方法/路径**：`PUT /api/admin/movies/{id}`，请求体同新增。

说明：
- **显式 SET 全字段**：`description` 传 `null` 可清空；`poster` 传 `null` 会写入空串（列为 `NOT NULL DEFAULT ''`）。
- 目标不存在 → `C204`。

### 3.4 删除

- **方法/路径**：`DELETE /api/admin/movies/{id}`；不存在 → `C204`。

---

## 四、管理端 - 轮播图管理（`/api/admin/banners`）

> 权限：超级管理员。列表**不分页、不支持筛选**。

### 4.1 列表

- **方法/路径**：`GET /api/admin/banners`（按 `sort` asc、`id` desc）

```json
{ "id": 1, "image": "http://…/b1.jpg", "linkUrl": "/movies/1", "sort": 1, "status": 1, "createTime": "2026-09-12T04:35:02" }
```

### 4.2 新增 / 4.3 修改 / 4.4 删除

| 方法/路径 | 请求体 | 说明 |
|-----------|--------|------|
| `POST /api/admin/banners` | `{ "image": "http://…/b.jpg", "linkUrl": "/movies/1", "sort": 9, "status": 1 }` | `image` 非空（图片 URL，可先经 §11 上传获得）；`linkUrl` 为跳转链接，≤255 |
| `PUT /api/admin/banners/{id}` | 同上 | 不存在 → `C204` |
| `DELETE /api/admin/banners/{id}` | — | 不存在 → `C204` |

---

## 五、管理端 - 影院管理（`/api/admin/cinemas`）

> 权限：超级管理员。

| 方法/路径 | 说明 |
|-----------|------|
| `GET /api/admin/cinemas?page=1&size=10&name=万达` | 分页 + 名称模糊筛选；`records[]`：`{id, name, address, status, createTime}` |
| `POST /api/admin/cinemas` | `{ "name": "CGV影城", "address": "成都市…", "status": 1 }`（name/address 非空） |
| `PUT /api/admin/cinemas/{id}` | 请求体同上；不存在 → `C204` |
| `DELETE /api/admin/cinemas/{id}` | 不存在 → `C204` |

---

## 六、管理端 - 影厅与座位（`/api/admin/halls`）

> 权限：影院管理员（`requireCinemaAdmin`）。**所有操作强制本影院**，跨影院数据 → `C003`。

### 6.1 本影院影厅列表

- **方法/路径**：`GET /api/admin/halls`（不分页，只返回当前管理员所属影院的影厅，按 id asc）

```json
{ "id": 1, "cinemaId": 1, "name": "1号激光厅", "seatRows": 4, "seatCols": 5, "status": 1 }
```

### 6.2 新增 / 6.3 修改 / 6.4 删除

| 方法/路径 | 请求体 | 说明 |
|-----------|--------|------|
| `POST /api/admin/halls` | `{ "name": "1号厅", "seatRows": 8, "seatCols": 10 }` | `cinemaId` 由后端取当前管理员的影院，前端不传；行列 1~26 |
| `PUT /api/admin/halls/{id}` | 同上 | 不存在 → `C204`；改行列后座位网格按新规格重建 |
| `DELETE /api/admin/halls/{id}` | — | **已排场影厅禁止删除 → `C502`**；不存在 → `C204` |

### 6.5 读取座位模板

- **方法/路径**：`GET /api/admin/halls/{id}/seats`

响应（`rows×cols` 全量网格；未配置的座位默认 `status=0` 可售）：

```json
{
  "rows": 2, "cols": 2,
  "seats": [
    { "row": 1, "col": 1, "seatNo": "1排1座", "status": 0 },
    { "row": 1, "col": 2, "seatNo": "1排2座", "status": 1 }
  ]
}
```

### 6.6 保存座位模板

- **方法/路径**：`PUT /api/admin/halls/{id}/seats`

```json
{ "seats": [ { "row": 1, "col": 1, "status": 1 }, { "row": 1, "col": 2, "status": 0 } ] }
```

说明：
- **整体替换**（事务内删旧插新），坐标必须在当前 `rows×cols` 内，越界 → `C001`（消息含影厅规格）；`status` 仅 0（可售）/1（不可售）。
- `seatNo` 由后端生成（`{row}排{col}座`），前端不需要传。

---

## 七、管理端 - 排场管理（`/api/admin/screenings`）

> 权限：影院管理员。查询与写入均强制本影院。

### 7.1 分页查询

- **方法/路径**：`GET /api/admin/screenings?page=1&size=10&movieId=1`（`movieId` 可空；按 start_time 倒序）

```json
{
  "id": 1, "movieId": 1, "movieTitle": "星际穿越",
  "hallId": 1, "hallName": "1号激光厅", "cinemaId": 1,
  "startTime": "2026-09-13T10:00:00", "price": 39.90, "status": 0
}
```

### 7.2 电影下拉（筛选用）

- **方法/路径**：`GET /api/admin/screenings/movie-options`

响应：上架电影列表 `[{ "id": 1, "title": "星际穿越" }, …]`（转调 movie 模块，仅 `status=1`）。

### 7.3 新增

- **方法/路径**：`POST /api/admin/screenings`

```json
{ "movieId": 1, "hallId": 1, "startTime": "2026-10-01T19:00:00", "price": 45.00 }
```

说明：
- 校验：电影必须**上架**（否则 `C001 电影不存在或已下架`）；影厅必须**本影院**（越界 `C003`）；`startTime` 必须晚于当前（否则 `C001`）；`price` > 0。
- `cinemaId` 由后端取当前管理员影院。
- 撞 `uk_hall_start`（同影厅同一开场时间）→ `C501`。

### 7.4 修改 / 7.5 删除

| 方法/路径 | 说明 |
|-----------|------|
| `PUT /api/admin/screenings/{id}` | 请求体同新增；换影厅会同步 `cinemaId`；**已开场（status=1）禁止修改 → `C503`**；不存在 → `C204` |
| `DELETE /api/admin/screenings/{id}` | **已开场禁止删除 → `C503`**；不存在 → `C204` |

---

## 八、管理端 - 订单管理（`/api/admin/orders`）

> 权限：双角色（超管查全部；影院管理员**强制本影院**）。

### 8.1 分页查询

- **方法/路径**：`GET /api/admin/orders?page=1&size=10&orderNo=XXX&status=1`（`orderNo` 精确、`status` 精确，均可空；按 create_time 倒序）

`data.records[]`（JOIN `t_user` 带出下单人信息）：

```json
{
  "id": 1, "orderNo": "VERIFY20260912001",
  "userId": 1, "userPhone": "13900001111", "userNickname": "用户1111",
  "screeningId": 1, "movieId": 1, "movieTitle": "星际穿越", "cinemaId": 1,
  "status": 1, "totalAmount": 59.90,
  "payExpireTime": "2026-09-12T13:00:00", "payTime": "2026-09-12T12:41:00",
  "cancelType": null, "createTime": "2026-09-12T12:40:00"
}
```

说明：`status` 0 待支付 / 1 已支付 / 2 已取消；`cancelType` 1 手动 / 2 超时。

---

## 九、管理端 - 用户管理（`/api/admin/users`）

> 权限：超级管理员。

### 9.1 分页查询

- **方法/路径**：`GET /api/admin/users?page=1&size=10&phone=138`（`phone` 模糊，可空；按 id desc）

`data.records[]`：`{ "id": 1, "phone": "13800138000", "nickname": "用户8000", "status": 1, "createTime": "…" }`

### 9.2 禁用 / 启用

- **方法/路径**：`PUT /api/admin/users/{id}/status`

```json
{ "status": 0 }
```

说明：`status` 0 禁用 / 1 正常；不存在 → `C204`。禁用后该用户**验证码登录返回 `C102`**（密码登录统一 `C004`）。

---

## 十、管理端 - 管理员管理（`/api/admin/admins`）

> 权限：**列表/新增**双角色（影院管理员只能看本影院、只能建本影院 role=1）；**修改/删除**超管专属。

### 10.1 列表（不分页）

- **方法/路径**：`GET /api/admin/admins?role=1`（`role` 可空：0 超管 / 1 影院管理员）

```json
{ "id": 2, "username": "cinema01", "role": 1, "cinemaId": 1, "status": 1, "createTime": "2026-09-12T04:35:02" }
```

### 10.2 新增

- **方法/路径**：`POST /api/admin/admins`

```json
{ "username": "cinema03", "password": "Aa123456", "role": 1, "cinemaId": 1 }
```

说明：
- 超管：可建 role=0（`cinemaId` 须为 0/空）或 role=1（**必须绑存在的影院**，否则 `C202`）。
- 影院管理员：`role` 必须为 1 且 `cinemaId` 必须是本影院，否则 `C003`。
- 用户名重复 → `C201`；密码规则同登录（8-20 位含字母数字）。

### 10.3 修改

- **方法/路径**：`PUT /api/admin/admins/{id}`

```json
{ "username": "cinema03", "role": 1, "cinemaId": 1, "status": 0 }
```

说明：不改密码；用户名撞已有 → `C201`；role=1 未绑影院/影院不存在 → `C202`；**不能操作自己 → `C203`**；不存在 → `C204`。

### 10.4 删除

- **方法/路径**：`DELETE /api/admin/admins/{id}`

说明：逻辑删除，同时把用户名改写为 `{username}_del_{id}`（避开唯一索引，同名可重建）；**不能删除自己 → `C203`**。

---

## 十一、管理端 - 文件上传（`/api/admin/files`）

> 权限：超级管理员。

### 11.1 上传图片

- **方法/路径**：`POST /api/admin/files`
- **Content-Type**：`multipart/form-data`（字段名 `file`）

响应：

```json
{ "code": "0000", "message": "ok", "data": { "url": "http://localhost:9000/cd-ticket/20260912/5d3c8e7b-….png" } }
```

说明：
- 仅图片（jpg/jpeg/png/webp/gif）、≤5MB，否则 `C001`；上传失败 `S001`。
- 返回**公开可读 URL**，可直接作为电影海报（§3.2 `poster`）或轮播图（§4.2 `image`）提交。
- bucket 由后端启动时自动创建；公开读策略见 `docs/superpowers/specs/2026-09-12-admin-api-design.md` §7。