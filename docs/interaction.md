# 交互文档

> 对应页面路由、跳转、涉及 API、权限。页面交互细节据此实现。

---

## 一、用户端（frontend/web）

### 1.1 页面地图

```
首页 /                      → 电影详情 /movies/:id  → 影院页 /cinemas?movieId=
                           → 电影页 /movies (tab 热映|待映)
电影页 /movies              → 电影详情
电影详情 /movies/:id        → 影院页 /cinemas?movieId=:id  （购票按钮）
影院页 /cinemas             → 影院详情 /cinemas/:id
影院详情 /cinemas/:id       → 选座弹窗 → 登录页(游客) / 确认订单(已登录)
登录页 /login               → 回跳原页面
确认订单 /orders/confirm    → 支付成功 → 我的订单 /my
我的 /my                    → 我的订单、设置/修改密码
```

### 1.2 公共元素

- **顶栏**（首页/电影/影院/详情页）：Logo、导航（电影 / 影院）、右上角登录入口。
  - 未登录：显示「登录」按钮 → `/login`
  - 已登录：显示用户昵称/手机号 → 点击进入 `/my`
- **Token 生命周期**：axios 请求拦截器带 `Authorization: Bearer {accessToken}`；401 时尝试 Refresh 无感刷新，失败跳 `/login?redirect=原路径`。

### 1.3 首页 `/`

| 元素 | 内容 / 行为 |
|------|------------|
| 轮播图 | 取 `/api/banners`（仅上线），点击跳 `link_url` |
| 热映电影栏 | 横向海报列表，取 `/api/movies?status=now`；「全部」→ `/movies?tab=now`；点海报 → `/movies/:id` |
| 待映电影栏 | 同上，`status=coming`；「全部」→ `/movies?tab=coming` |

### 1.4 电影页 `/movies`

- URL 参数 `tab=now | coming` 控制默认高亮 tab。
- 两个 tab：热映列表 / 待映列表（分页）。
- 点海报 → `/movies/:id`。

### 1.5 电影详情页 `/movies/:id`

- 海报、片名、上映日期、简介等，取 `/api/movies/:id`。
- **购票按钮**：跳 `/cinemas?movieId=:id`（展示有该电影排场的影院列表）。

### 1.6 影院页 `/cinemas`

- 影院列表，取 `/api/cinemas`（带 `movieId` 时后端筛选有该电影排场的影院）。
- 点影院卡片 → `/cinemas/:id`。

### 1.7 影院详情页 `/cinemas/:id`

- 影厅/影院信息，取 `/api/cinemas/:id`。
- **该影院有排场的电影**：横向海报列表，取 `/api/screenings?cinemaId=:id` 去重后的 movie 列表。
- 点某电影海报 → 下方**场次列表**更新为 `GET /api/screenings?cinemaId=:id&movieId=:movieId&date=当前日期`。
- 每行场次：时间 + 影厅 + 价格 + **选座购票按钮**。
  - 点「选座购票」：
    - **游客** → 跳 `/login?redirect=/cinemas/:id?movieId=:movieId`（登录后回跳）
    - **已登录** → 弹出**选座弹窗**

### 1.8 选座弹窗

- 数据：`GET /api/screenings/:id` 返回场次信息 + 影厅座位模板（`GET /api/halls/:id/seats`）+ 已占用座位。
- 座位图按 `rows × cols` 渲染，不可售座位（模板 status=1）置灰、已占用置红/禁选、空闲可选。
- 点座位 → 选中（可多选）；再点取消。
- **确认选座** → `POST /api/orders {screeningId, seatNos[]}` → 跳 `/orders/confirm?orderId=:id`。
  - 座位已被占：后端返回错误，提示「座位已被购买，请重新选择」并刷新座位图。

### 1.9 登录页 `/login`

| 方式 | 说明 |
|------|------|
| 手机号 + 验证码 | 输入手机号 → 点「获取验证码」调 `POST /api/user/auth/sms-code`；演示环境验证码固定 `123456`。提交 `POST /api/user/auth/login/sms`。**首次验证码登录自动静默注册** |
| 手机号 + 密码 | `POST /api/user/auth/login/password`；未设置密码用户可切到验证码方式先登录 |

- 登录成功 → 存 Token（access/refresh）→ 跳 `redirect` 参数指向的原页面（默认 `/`）。
- 登录后未设置过密码 → 提示前往「我的-设置密码」。
- 密码设置/修改：`POST /api/user/me/password`（设置需验证码登录态，修改需旧密码校验）。

### 1.10 确认订单页 `/orders/confirm?orderId=:id`

- 展示场次、座位、单价、总价（取 `GET /api/orders/:id`）。
- **立即支付** → `POST /api/orders/:id/pay` → 触发 `POST /api/payments/callback`（模拟）→ 支付成功 → 跳 `/my?tab=paid`。
- **退出不支付**：关闭页面即可，订单保持待支付（15min 超时自动取消释放座位）；可在「我的-待支付」继续支付或取消。

### 1.11 我的页面 `/my`

- Tab：待支付 / 已支付 / 已取消（取 `/api/orders/my?status=`）。
- 待支付：显示剩余支付时间；操作「去支付」/「取消订单」（`POST /api/orders/:id/cancel`，释放座位）。
- 已支付：展示取票码/座位信息。
- 已取消：展示取消原因（超时/手动）。
- **设置密码 / 修改密码**入口（未设置 → 设置；已设置 → 修改，需旧密码）。

---

## 二、管理端（frontend/admin）

### 2.1 页面地图

```
登录页 /login（选择角色）
 ├─ 超级管理员 → 仪表盘 + 电影/影院/用户/管理员/轮播图/订单
 └─ 影院管理员 → 仪表盘 + 影厅/排场/订单/管理员(本影院)
```

### 2.2 登录页 `/login`

- **选择角色**（超级管理员 / 影院管理员）→ 输入用户名+密码 → `POST /api/admin/auth/login`。
- 后端校验账号 `role` 与所选角色一致，不一致返回错误。
- 成功 → 存 Token + 角色 + `cinemaId`（影院管理员）→ 跳仪表盘。
- 单 Token + Redis，单设备登录（新登录踢下线），注销即时吊销。
- 路由守卫：无 Token 访问后台页 → 跳登录；影院管理员访问超管菜单 → 拒绝。

### 2.3 布局与公共元素

- 左侧菜单：按角色渲染（见下）；顶部：影院名（影院管理员）/「退出登录」。
- 退出 → `POST /api/admin/auth/logout`（吊销 Token）→ 回登录页。

### 2.4 超级管理员菜单

| 菜单 | 页面行为 |
|------|----------|
| 仪表盘 | `GET /api/admin/dashboard`：总票房、总订单、热映影片 Top、近 N 日票房趋势（ECharts） |
| 电影管理 | 影片表格（分页/搜索）→ 新增/编辑弹窗（含海报上传 MinIO）→ 删除/上下架。CRUD `/api/admin/movies` |
| 影院管理 | 影院表格 → 新增/编辑/删除。CRUD `/api/admin/cinemas` |
| 用户管理 | C 端用户表格（手机号/昵称/注册时间），只读，可禁用。`GET /api/admin/users` |
| 管理员管理 | 管理员表格 → 新增（选角色/关联影院）/编辑/禁用/删除。CRUD `/api/admin/admins` |
| 轮播图管理 | 轮播图表格（排序/上线/下线/链接）→ 新增/编辑/删除。CRUD `/api/admin/banners` |
| 订单管理 | 全局订单表格（订单号/用户/影院/状态/金额），可按状态筛选。`GET /api/admin/orders` |

### 2.5 影院管理员菜单

| 菜单 | 页面行为 |
|------|----------|
| 仪表盘 | 本影院票房统计（`GET /api/admin/dashboard`，后端按 `cinemaId` 过滤） |
| 影厅管理 | 本影院影厅列表 → 新增/编辑影厅（行列数）→ **座位模板设置**：可视化点选座位可售/不可售，保存 `PUT /api/admin/halls/:id/seats`。CRUD `/api/admin/halls` |
| 排场管理 | 为本影院影厅选择影片（本影院上映中影片）→ 设定时间/价格 → 新增/编辑/删除。CRUD `/api/admin/screenings` |
| 订单管理 | 本影院订单表格，只读或可标记退款。`GET /api/admin/orders?cinemaId=:本影院` |
| 管理员管理 | 仅操作本影院管理员账号（角色固定影院管理员），CRUD `/api/admin/admins`（后端校验 `cinemaId`） |

---

## 三、关键交互约束

| 场景 | 约束 |
|------|------|
| 座位并发 | 选座弹窗数据为快照；提交订单由后端 Lua 原子校验，冲突返回错误并刷新座位图 |
| 游客购票 | 任何「选座购票」动作对游客先跳登录，登录后带 `redirect` 回跳继续 |
| 多影院隔离 | 影院管理员所有列表接口后端强制拼 `cinema_id`，前端仅传入；超管传空取全部 |
| Token 刷新 | 用户端 401 触发 refresh 一次，仍失败回登录页保留原路径 |
| 超时订单 | 前端按 `pay_expire_time` 倒计时展示「去支付」可用性；超时后仅展示「已取消」 |
