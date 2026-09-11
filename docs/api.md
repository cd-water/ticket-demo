# CD-TICKET API 接口文档

> 版本：v0.2（认证模块；后续业务接口持续更新）。
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

数字位 `MSS` 中 `M` 为业务模块（0=公共, 1=用户, 2=管理员, 3-7 业务模块预留）。

| code | 场景 |
|------|------|
| `0000` | 成功 |
| `C001` | 参数错误（缺失/非法），含手机号格式、密码复杂度、Body 解析失败等 |
| `C002` | 未认证 / Token 无效或过期 |
| `C003` | 无权限 |
| `C004` | 用户名或密码错误（C 端登录/B 端登录**统一**，避免用户名枚举） |
| `C101` | 验证码错误或已过期 |
| `C102` | 用户已禁用 |
| `C103` | 新密码与旧密码相同 |
| `C104` | 两次输入密码不一致 |
| `S001` | 系统异常 |

---

## 校验规则

| 字段 | 规则 | 正则 |
|------|------|------|
| `phone` | 1 开头 11 位 | `^1[3-9]\d{9}$` |
| `password` / `newPassword` / `confirmPassword` | 8-20 位，含字母 + 数字 | `^(?=.*[A-Za-z])(?=.*\d).{8,20}$` |

校验失败 → `code="C001"`，`message` 为具体规则描述。

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

## 二、B 端认证（`/api/admin`）

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