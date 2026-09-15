# CD-TICKET 电影票务系统

C 端涵盖电影/影院浏览、选座购票；B 端支持多影院后台管理。

## 技术栈

Spring Boot 3.5 (Java 21) · MyBatis-Plus · MySQL · Redis (Redisson) · Kafka · Spring Security · MinIO
前端：React 19 + Vite + Tailwind（C 端） / Vue 3 + Element Plus（B 端）

## 模块

| 模块 | 说明 | 端口 |
|---|---|---|
| `cd-ticket-app` | C 端后端（分层：interfaces / application / domain / infrastructure） | 8600 |
| `cd-ticket-admin` | B 端后台管理后端 | 8601 |
| `frontend/web` | C 端前端（React） | 5600 (dev) |
| `frontend/admin` | B 端前端（Vue） | 5601 (dev) |
| `script/sql` | `table.sql` 建表 + `seed.sql` 演示数据 | — |
| `docs/api.md` | C 端选座购票接口契约 | — |

## 快速开始

```bash
# 1. 基础设施（MySQL / Redis / Kafka / MinIO，凭据见 docker-compose.yaml）
docker compose up -d

# 2. 建表 + 初始化数据
mysql -uroot -pcdwatermysql cd_ticket < script/sql/table.sql
mysql -uroot -pcdwatermysql cd_ticket < script/sql/seed.sql

# 3. 后端（根目录 Maven wrapper）
./mvnw -pl cd-ticket-app spring-boot:run      # C 端 :8600
./mvnw -pl cd-ticket-admin spring-boot:run    # B 端 :8601

# 4. 前端（各自目录 pnpm）
cd frontend/web && pnpm dev                   # C 端 :5600
cd frontend/admin && pnpm dev                 # B 端 :5601
```

测试：`./mvnw test`（单模块单测：`./mvnw test -pl cd-ticket-admin -Dtest=MovieServiceTest`）

演示账号：C 端 `13800000001 / Aa123456`，B 端 `admin / Aa123456`

## 核心设计

- **座位锁定**：Redis Bitmap + Lua 原子检查与锁定，座位图整图一次 GET 解析，已售位图懒加载自 DB
- **超时关单**：Redis ZSet 延迟队列 + 轮询，`status` CAS（乐观锁）解决支付/关单并发，落败方自动退款补偿
- **认证**：C 端双 Token 无感刷新（access 15m + refresh 7d）；B 端单 Token + Redis（单设备、可即时吊销）
- **缓存**：逻辑过期 + Redisson 互斥锁异步重建（防击穿）、布隆过滤 + 空值缓存（防穿透）、TTL 抖动 + 启动预热（防雪崩）
- **限流**：Redis ZSet + Lua 滑动窗口（座位图按场次、下单按用户）
- **异步解耦**：短信与票房统计经本地消息表 + 定时任务投递 Kafka，失败指数退避重试
- **支付**：当前为服务端模拟（接口同步完成 0→1，生成 8 位取票码），网关接入时改由支付回调驱动
