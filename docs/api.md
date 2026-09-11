# CD-TICKET API 接口文档

> 版本：v0.1（认证模块首发）。后续业务接口持续更新。
> 服务：后端单体 JAR，端口 8600，统一前缀 `/api`。

## 全局约定

- **统一响应体**：`{ "code": 0, "message": "ok", "data": { } }`
  - `code=0` 成功；非 0 失败，`message` 为可读提示。
  - `data` 为具体数据，无返回时为 `null`。
- **鉴权**：
  - C 端：请求头 `Authorization: Bearer {accessToken}`。
  - B 端：请求头 `Authorization: Bearer {adminToken}`。
  - 未携带/失效/过期 Token → `code=1002`（HTTP 401）。
- **Content-Type**：`application/json`（除明确标注）。

## 错误码表

| code | 场景 |
|------|------|
| 0 | 成功 |
| 1001 | 参数错误（缺失/非法） |
| 1002 | 未认证 / Token 无效或过期 |
| 1003 | 无权限 |
| 2001 | 验证码错误或已过期 |
| 2002 | 验证码发送过于频繁（冷却中） |
| 2003 | 手机号格式非法 |
| 2101 | 用户已禁用 |
| 2102 | 账号或密码错误 |
| 2103 | 新密码与旧密码相同 |
| 2104 | 两次输入密码不一致 |
| 2201 | 管理员不存在或已禁用 |
| 2202 | 密码错误 |

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
{ "code": 0, "message": "ok", "data": null }
```

说明：
- 验证码随机 6 位数字，存 Redis `sms:{phone}`，TTL 5 分钟（配置 `sms.code-expire-seconds`）。
- 发送冷却 60 秒（`sms.send-cooldown-seconds`）；冷却中返回 `code=2002`。
- 手机号格式校验失败返回 `code=2003`。
- 发送器为 Mock 实现，演示环境验证码通过服务端日志输出。

错误码：`1001` 参数错误 / `2002` 发送过于频繁 / `2003` 手机号格式非法

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
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "a1b2c3d4-e5f6-...",
    "user": { "id": 1, "phone": "13800138000", "nickname": "用户8000" }
  }
}
```

说明：
- 手机号不存在 → 静默注册（nickname 默认「用户+手机尾号」），随后签发 Token。
- 验证码错误/过期 → `code=2001`。
- 用户被禁用 → `code=2101`。

### 1.3 密码登录

- **方法/路径**：`POST /api/user/auth/login/password`
- **鉴权**：公开

请求：

```json
{ "phone": "13800138000", "password": "Aa123456" }
```

响应（成功）：同 1.2，`data.user` 含 `id/phone/nickname`。

说明：
- 手机号不存在或密码错误统一返回 `code=2102`（不暴露账号是否存在）。
- 用户被禁用 → `code=2101`。

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
  "code": 0,
  "message": "ok",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "新的-refreshToken-字符串"
  }
}
```

说明：
- 校验 refreshToken 存在于 Redis `refresh:token:{token}`（值为 userId），且未过期（轮换机制）。
- 校验通过：旧 Refresh 作废，签发**新 Access + 新 Refresh**（Refresh 单次使用）。
- 无效/已过期 → `code=1002`。

### 1.5 退出登录

- **方法/路径**：`POST /api/user/auth/logout`
- **鉴权**：Access Token（Authorization 头）

请求：

```json
{ "refreshToken": "a1b2c3d4-e5f6-..." }
```

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": null }
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
  "code": 0,
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
- Token 无效/过期 → `code=1002`；用户被禁用 → `code=2101`。

### 1.7 修改密码

- **方法/路径**：`POST /api/user/me/password`
- **鉴权**：Access Token

请求：

```json
{ "newPassword": "NewPass123", "confirmPassword": "NewPass123" }
```

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": null }
```

说明：
- 登录态即身份证明，无需旧密码/验证码。
- 后端校验：
  - `newPassword` 与 `confirmPassword` 非空、长度 6~20；
  - `newPassword` 必须等于 `confirmPassword`，否则 `code=2104`；
  - 新密码与原密码相同（已设置密码时）→ `code=2103`。
- 校验通过后写入 BCrypt 密码，`hasPassword` 变为 true。

错误码：`1001` 参数错误 / `2103` 新密码与旧密码相同 / `2104` 两次输入不一致

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
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "admin": { "id": 1, "username": "admin", "role": 0, "cinemaId": 0 }
  }
}
```

说明：
- 管理员不存在或禁用 → `code=2201`；密码错误 → `code=2202`。
- 登录成功签发 JWT（`a{id}` 类型），并存 Redis `admin:token:{adminId}`，TTL = access 有效期。
- **单设备**：新登录覆盖旧 Token，旧 Token 立即失效（后续请求 401）。

### 2.2 管理员退出登录

- **方法/路径**：`POST /api/admin/auth/logout`
- **鉴权**：Admin Token

请求：

```json
{}
```

响应（成功）：

```json
{ "code": 0, "message": "ok", "data": null }
```

说明：删除 Redis `admin:token:{adminId}`，Token 即时吊销。
