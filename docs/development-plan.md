# 电影票务系统 — 全栈开发计划

> 技术栈：Spring Boot 3.5.16 · MyBatis-Plus 3.5.17 · MySQL 8 · Redis 7（Redisson 3.52.0）· Spring Security · Kafka · MinIO
> 前端：管理端 `frontend/admin`（Vue 3 + Element Plus + Pinia + ECharts），用户端 `frontend/web`（React 19 + Tailwind 4 + zustand + react-router）

---

## 1. 项目概述与目标

电影票务系统，包含 **用户端（C 端）** 与 **管理端（B 端）**：

- **用户端**：首页轮播图 / 热映 / 待映、电影列表与详情、影院列表与详情、按场次选座购票、手机号+验证码（或密码）登录、我的订单。
- **管理端**：支持**超级管理员**与**影院管理员**两种角色，多影院数据隔离。超管管理全部影院，影院管理员仅能管理本影院。

### 1.1 版本信息

- 后端：Spring Boot 单体 JAR，端口 **8600**，`/api` 前缀
- 管理端：Vue 3，端口 **5601**
- 用户端：React 19，端口 **5600**
- 数据库：`cd_ticket`（MySQL 8.0.45，docker-compose）
- 中间件：Redis 7（含 Redisson）、Kafka 4.1、MinIO（docker-compose 一键启动）

### 1.2 范围

| 包含 | 不包含（后续版本） |
|------|------------------|
| 电影/影院/影厅/场次管理、轮播图、管理员管理 | 优惠券、会员等级、退款、卖品 |
| 选座购票、订单、模拟支付、我的订单 | 真实支付渠道、短信服务商 |
| 手机号+验证码（静默注册）/手机号+密码登录、双 Token 无感刷新 | 用户注册页、找回密码 |
| 超管 + 影院管理员双角色、多影院隔离 | 多影院排片联动、售票权限细分 |
| 票房统计（按影院/影片/时间） | 实时票房、报表导出 |

---

## 2. 技术架构

### 2.1 后端模块（Maven 多模块）

`cd-ticket-bootstrap` 为可运行单体（`CdTicketApplication`），聚合所有业务模块。每个业务模块按 **DDD 四层** 组织：

```
domain/         领域实体、值对象、聚合、仓储接口（纯 POJO，无框架依赖）
application/    应用服务、DTO、用例编排
infrastructure/ MyBatis-Plus 实体与 Mapper、仓储实现、适配器（Redis/Kafka/MinIO）
interfaces/     REST Controller、请求/响应 DTO（/api 接口面）
```

依赖方向单向：`interfaces → application → domain ← infrastructure`。

```
common (无依赖)
 ├─ user, movie, cinema        （无模块间依赖）
 ├─ screening → movie, cinema
 ├─ order → user, screening
 ├─ payment → order
 └─ admin → user, movie, cinema, screening, order, payment
```

模块职责：

| 模块 | 职责 |
|------|------|
| `common` | 统一响应体、异常体系、常量、通用工具、配置类（Web/Security/Redis/Kafka/MinIO） |
| `user` | C 端用户：注册/登录（验证码/密码）、双 Token 签发与刷新、我的信息、修改密码 |
| `admin` | B 端账号：登录（单 Token + Redis）、管理员 CRUD、角色权限（超管/影院管理员） |
| `movie` | 电影 CRUD、电影列表（热映/待映）、电影详情、**轮播图管理** |
| `cinema` | 影院 CRUD、影厅 CRUD、影厅座位模板配置 |
| `screening` | 场次编排、按影院/电影查场次、座位锁定（Redis Bitmap + Lua） |
| `order` | 订单创建、状态机、超时关单（Redis ZSet + 轮询）、乐观锁、座位释放 |
| `payment` | 模拟支付、支付回调、退款补偿（Kafka 异步通知） |

### 2.2 依赖清单（根 POM 已配）

| 依赖 | 版本 | 用途 |
|------|------|------|
| `spring-boot-starter-parent` | 3.5.16 | 父 POM |
| `mybatis-plus-spring-boot3-starter` | 3.5.17 | ORM |
| `spring-boot-starter-web` | BOM | Web MVC |
| `spring-boot-starter-security` | BOM | 认证授权 |
| `spring-boot-starter-data-redis` | BOM | Redis |
| `redisson-spring-boot-starter` | 3.52.0 | 分布式锁 |
| `spring-kafka` | BOM (3.3.16) | Kafka |
| `mysql-connector-j` | BOM (9.7.0) | MySQL 驱动（runtime） |
| `mapstruct` | 1.6.3 | 实体↔DTO |
| `jjwt` | 0.12.6 | JWT |

> 注：Boot 3 BOM **不含** `spring-boot-starter-kafka`，使用底层 `spring-kafka`（功能等价）。

### 2.3 关键设计点（参考 docs/project-tech.md，仅作参考）

| 场景 | 方案 |
|------|------|
| 座位锁定 | Redis Bitmap，一场一 key，座位号 → bit；Lua 脚本原子「检查+锁定」，高并发一座一人 |
| 超时关单 | Redis ZSet 延迟队列 `[expireTs, orderId]` + 轮询；DB 乐观锁（version）防竞态 |
| C 端认证 | 双 Token：Access 15min + Refresh 7d，无感刷新 |
| B 端认证 | 单 Token + Redis，设备指纹限制单设备，支持即时吊销 |
| 缓存 | 逻辑过期+互斥锁（击穿）、布隆+空值（穿透）、TTL 抖动+预热（雪崩） |
| 限流 | Redis ZSet + Lua 滑动窗口 |
| 异步 | Kafka 短信/票房，本地消息表 + 定时任务保证最终一致 |

---

## 3. 功能规划

### 3.1 用户端（frontend/web）

| 页面 | 路由 | 功能 |
|------|------|------|
| 首页 | `/` | 轮播图、热映电影、待映电影（各栏「全部」跳电影页） |
| 电影页 | `/movies` | 热映/待映列表（tab 切换） |
| 电影详情页 | `/movies/:id` | 海报、简介、演职员、购票按钮 |
| 影院页 | `/cinemas` | 影院列表（支持按上映电影筛选，供购票跳转） |
| 影院详情页 | `/cinemas/:id` | 该影院有排场的电影横向海报列表；选某电影展示对应场次列表，每行场次有「选座购票」 |
| 登录页 | `/login` | 手机号+验证码 / 手机号+密码 两种方式；验证码登录自动静默注册 |
| 确认订单页 | `/orders/confirm` | 展示选座结果、场次、价格，确认后生成待支付订单 |
| 我的页面 | `/my` | 我的订单（待支付/已支付/已取消）、设置/修改密码 |

### 3.2 管理端（frontend/admin）

**超级管理员**（`role=0`）：

| 页面 | 功能 |
|------|------|
| 登录页 | 选择角色 → 用户名+密码 |
| 仪表盘 | 核心指标 + ECharts 图表（总票房、订单数、热映影片 Top） |
| 电影管理 | 影片 CRUD、上下架、海报上传（MinIO） |
| 影院管理 | 影院 CRUD |
| 用户管理 | C 端用户列表（手机号、注册时间等） |
| 管理员管理 | 管理员账号 CRUD、分配角色/关联影院、启用禁用 |
| 轮播图管理 | 轮播图 CRUD、排序、链接、上下线 |
| 订单管理 | 全局订单查看/管理 |

**影院管理员**（`role=1`，仅本影院数据）：

| 页面 | 功能 |
|------|------|
| 登录页 | 选择角色 → 用户名+密码 |
| 仪表盘 | 本影院票房统计 |
| 影厅管理 | 影厅 CRUD + 座位模板可视化设置 |
| 排场管理 | 为本影院影片/影厅排场 |
| 订单管理 | 本影院订单查看/管理 |
| 管理员管理 | 仅能操作本影院管理员账号 |

---

## 4. REST API 设计

> 统一前缀 `/api`。响应结构 `{ code, message, data }`。分页 `{ total, records, page, size }`。

### 4.1 用户端

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/user/auth/sms-code` | 发送验证码（Mock 固定值演示） | 公开 |
| POST | `/api/user/auth/login/sms` | 手机号+验证码登录（静默注册） | 公开 |
| POST | `/api/user/auth/login/password` | 手机号+密码登录 | 公开 |
| POST | `/api/user/auth/refresh` | 刷新 Token | Refresh Token |
| GET | `/api/user/me` | 当前用户信息 | Access Token |
| POST | `/api/user/me/password` | 设置/修改密码 | Access Token |

### 4.2 电影/影院/场次（公开浏览）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/movies` | 电影列表（`status=now/coming`，分页） |
| GET | `/api/movies/{id}` | 电影详情 |
| GET | `/api/banners` | 轮播图列表（仅上线） |
| GET | `/api/cinemas` | 影院列表（可选 `movieId` 筛选有该电影排场的影院） |
| GET | `/api/cinemas/{id}` | 影院详情 |
| GET | `/api/cinemas/{id}/hall` | 影院影厅列表 |
| GET | `/api/screenings` | 场次列表（`cinemaId`+`movieId`+`date`） |
| GET | `/api/screenings/{id}` | 场次详情（含座位占用） |
| GET | `/api/halls/{id}/seats` | 影厅座位模板 |

### 4.3 订单/支付

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/orders` | 创建订单（场次+座位，锁定座位） | Access |
| GET | `/api/orders/my` | 我的订单列表 | Access |
| GET | `/api/orders/{id}` | 订单详情 | Access |
| POST | `/api/orders/{id}/cancel` | 手动取消（释放座位） | Access |
| POST | `/api/orders/{id}/pay` | 模拟支付（生成支付单） | Access |
| POST | `/api/payments/callback` | 模拟支付回调（成功→已支付；若已取消→退款补偿） | 公开(签名) |

### 4.4 管理端（B 端，单 Token）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/admin/auth/login` | 管理员登录（返回角色信息） | 公开 |
| POST | `/api/admin/auth/logout` | 注销（吊销 Token） | 管理端 |
| GET | `/api/admin/dashboard` | 仪表盘统计 | 管理端 |
| — | **电影管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/movies`、`/api/admin/movies/{id}` | 影片 CRUD | 超管 |
| — | **影院管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/cinemas`、`/api/admin/cinemas/{id}` | 影院 CRUD | 超管 |
| — | **影厅管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/halls`、`/api/admin/halls/{id}` | 影厅 CRUD | 影院管理员（本影院） |
| PUT | `/api/admin/halls/{id}/seats` | 保存座位模板 | 影院管理员 |
| — | **排场管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/screenings`、`/api/admin/screenings/{id}` | 场次 CRUD | 影院管理员 |
| — | **用户管理** | | |
| GET | `/api/admin/users` | C 端用户列表 | 超管 |
| — | **管理员管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/admins`、`/api/admin/admins/{id}` | 管理员 CRUD | 超管 / 本影院管理员 |
| — | **轮播图管理** | | |
| GET/POST/PUT/DELETE | `/api/admin/banners`、`/api/admin/banners/{id}` | 轮播图 CRUD | 超管 |
| — | **订单管理** | | |
| GET | `/api/admin/orders` | 订单列表（超管全部 / 影院管理员本影院） | 管理端 |

---

## 5. 数据库设计

> 表前缀 `t_`（`t_admin`、`t_user`…）。逻辑删除 `deleted`，`create_time/update_time` 自动填充。关键表（部分）。

### 5.1 管理员 `t_admin`（沿用现有）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| username | VARCHAR(32) | 唯一 |
| password | VARCHAR(100) | BCrypt |
| role | TINYINT | 0 超管 / 1 影院管理员 |
| cinema_id | BIGINT | 关联影院（超管为 0） |
| status | TINYINT | 0 禁用 / 1 启用 |

### 5.2 C 端用户 `t_user`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| phone | VARCHAR(20) | 唯一 |
| password | VARCHAR(100) | 可空（未设置密码） |
| nickname | VARCHAR(50) | 默认「用户+手机尾号」 |
| status | TINYINT | 0 禁用 / 1 正常 |
| 审计字段 | | create/update/deleted |

### 5.3 电影 `t_movie`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| title | VARCHAR(100) | 片名 |
| poster | VARCHAR(255) | 海报 URL（MinIO） |
| description | TEXT | 简介 |
| release_date | DATE | 上映日期 |
| status | TINYINT | 0 待映 / 1 热映 / 2 下架 |
| 审计字段 | | |

### 5.4 轮播图 `t_banner`（并入 movie 模块）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| image | VARCHAR(255) | 图片 URL |
| link_url | VARCHAR(255) | 跳转链接 |
| sort | INT | 排序 |
| status | TINYINT | 0 下线 / 1 上线 |

### 5.5 影院 `t_cinema`（沿用现有）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| name | VARCHAR(100) | |
| address | VARCHAR(255) | |
| status | TINYINT | 0 停业 / 1 营业 |

### 5.6 影厅 `t_hall` + 座位 `t_seat_config`

```sql
CREATE TABLE t_hall (
  id BIGINT PK, cinema_id BIGINT, name VARCHAR(50), rows INT, cols INT, status TINYINT
);
CREATE TABLE t_seat_config (
  id BIGINT PK, hall_id BIGINT, seat_row INT, seat_col INT, seat_no VARCHAR(10), status TINYINT -- 0 正常 /1 不可售
);
```

> 座位布局与影厅分离；实际占用状态由 Redis Bitmap 运行时维护，不落 `t_seat_config` 的占用字段。

### 5.7 场次 `t_screening`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| movie_id | BIGINT | |
| hall_id | BIGINT | |
| cinema_id | BIGINT | |
| start_time | DATETIME | 开场时间 |
| price | DECIMAL(10,2) | 单价 |
| status | TINYINT | 0 未开始 / 1 已开场 |

### 5.8 订单 `t_order` + 明细 `t_order_item`

```sql
CREATE TABLE t_order (
  id BIGINT PK,
  order_no VARCHAR(32) UNIQUE,
  user_id BIGINT,
  screening_id BIGINT,
  cinema_id BIGINT,
  status TINYINT,           -- 0 待支付 / 1 已支付 / 2 已取消
  total_amount DECIMAL(10,2),
  version INT DEFAULT 0,    -- 乐观锁
  pay_expire_time DATETIME  -- 超时关单时间（创建+15min）
);
CREATE TABLE t_order_item (
  id BIGINT PK, order_id BIGINT, seat_no VARCHAR(10), price DECIMAL(10,2)
);
```

### 5.9 支付 `t_payment_record`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| order_no | VARCHAR(32) | |
| amount | DECIMAL(10,2) | |
| status | TINYINT | 0 待支付 / 1 成功 / 2 退款 |
| pay_time | DATETIME | |

### 5.10 本地消息表 `t_local_message`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| msg_type | VARCHAR(32) | sms / boxoffice |
| biz_id | VARCHAR(64) | 业务 ID |
| payload | JSON | 消息内容 |
| status | TINYINT | 0 待发送 / 1 已发送 |
| retry_count | INT | |

---

## 6. 关键流程设计

### 6.1 选座购票（座位锁定 → 下单 → 支付）

```
用户点「选座购票」
  ├─ 游客 → 跳转登录页
  ├─ 已登录 → 弹窗展示该场次座位图
  │    └─ 座位图 = 影厅模板(t_seat_config) ∩ Redis Bitmap 占用位
  │         └─ 选择座位 → 确认选座
  │              └─ POST /api/orders 创建订单
  │                   └─ Lua 脚本：校验座位未占用 → 锁定（bit=1）→ 返回
  │                        └─ 成功：DB 写订单(待支付) + 座位明细；Redis ZSet 写入延迟关单
  │                             └─ 失败（已占用）：提示重新选座
  └─ 确认订单页（待支付状态）→ 直接支付 / 退出稍后再付
```

### 6.2 支付与关单并发竞态

- **支付回调**（`POST /api/payments/callback`）：`UPDATE t_order SET status=已支付, version=version+1 WHERE id=? AND version=? AND status=待支付`
- **超时关单定时任务**（每分钟扫描 ZSet 到期项）：`UPDATE t_order SET status=已取消, version=version+1 WHERE id=? AND version=? AND status=待支付`
- 乐观锁保证两方只有一个成功；关单成功后 Lua 释放座位（bit=0）。
- **退款补偿**：支付回调发现订单已是 `已取消` → 写入 `t_local_message`（type=refund）→ 异步 Kafka 通知支付中心发起退款。

### 6.3 座位锁定（Redis Bitmap + Lua）

- Key：`seat:{screeningId}`，座位号 `(row-1)*cols + col` → bit 索引。
- Lua：`GETBIT` 逐一校验所有目标座位 → 全部空闲则 `SETBIT=1` → 返回成功；任一占用则返回失败（不锁定任何座位）。
- 释放：关单/取消时 Lua 回写 bit=0。

### 6.4 超时关单（Redis ZSet + 轮询）

- 创建订单时 `ZADD delay:cancel {expireTs} {orderId}`。
- 定时任务每分钟 `ZRANGEBYSCORE delay:cancel -inf now` 取出到期订单 → 乐观锁关单 → 释放座位。
- 已支付订单从 ZSet 移除。

### 6.5 认证

- **C 端双 Token**：登录/静默注册签发 Access(15min) + Refresh(7d)；Access 过期用 Refresh 换新（`/auth/refresh`）。
- **B 端单 Token**：登录后 Token 存 Redis `admin:token:{adminId}:{deviceFingerprint}`，单设备，注销/封号即时吊销。
- Spring Security `OncePerRequestFilter` 校验 JWT；`/api/user/auth/**`、`/api/admin/auth/login`、公开浏览接口放行。

### 6.6 缓存与限流（movie/cinema 热点）

- 击穿：逻辑过期 + 互斥锁异步重建。
- 穿透：布隆过滤器 + 缓存空值。
- 雪崩：TTL 抖动 + 启动预热。
- 限流：购票/下单接口 ZSet 滑动窗口（默认配置）。

### 6.7 异步解耦（Kafka + 本地消息表）

- 购票成功 → 异步：发短信、更新票房统计、订单推送。
- 可靠消息：业务 SQL + `t_local_message` 同事务；定时任务投递 Kafka 成功标记。

---

## 7. 开发里程碑

### 阶段 1：地基

- [ ] application.yml：MySQL/Redis/Kafka/MinIO 连接、JWT 密钥
- [ ] 全量 `script/sql/table.sql` + `seed.sql`（新增 t_user/t_movie/t_hall/t_seat_config/t_screening/t_order/t_order_item/t_payment_record/t_local_message/t_banner）
- [ ] common：统一响应体、异常处理、常量、MyBatis-Plus 配置
- [ ] Spring Security + JWT：C 端双 Token、B 端单 Token、登录接口
- **验收**：`./mvnw clean compile` 通过；curl 能登录拿 Token

### 阶段 2：影片·影院·影厅·场次（管理端 + 用户端）

- [ ] admin：影片/影院/影厅/轮播图 CRUD、座位模板配置
- [ ] web：首页（轮播图/热映/待映）、电影页、电影详情页、影院页、影院详情页
- [ ] screening：排场 CRUD、公开场次查询
- **验收**：管理端建影片/影院/影厅并排场，用户端可浏览电影与场次

### 阶段 3：选座·下单·支付·我的订单

- [ ] screening：座位锁定 Lua + 座位图查询
- [ ] order：创建订单、状态机、超时关单、乐观锁、座位释放
- [ ] payment：模拟支付 + 回调 + 退款补偿
- [ ] web：选座弹窗、确认订单、支付、我的订单
- **验收**：游客触发登录→选座→下单→支付全链路；两用户不能同时买同一座位

### 阶段 4：管理端完整 + 统计

- [ ] admin：订单管理（多影院隔离）、仪表盘 ECharts、用户管理、管理员管理（本影院）
- [ ] 限流、Kafka 短信/票房、缓存三防、本地消息表
- **验收**：双角色登录、多影院隔离正确、仪表盘数据正确

### 阶段 5：联调与收尾

- [ ] 前后端全流程联调、边界/异常（超时关单、退款补偿、并发选座）
- [ ] 性能压测（选座页并发）
- **验收**：全量回归通过，演示可用
