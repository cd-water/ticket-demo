# cd-ticket 管理端

电影票务系统 B 端后台。Vue 3（`<script setup lang="ts">`）+ Vue Router + Pinia + Element Plus + Vite，暗色单主题。

```bash
pnpm install
pnpm dev      # 5601，/api 代理到后端 8601
pnpm build    # vue-tsc -b && vite build
```

后端 `cd-ticket-admin` 需单独启动：`./mvnw -f cd-ticket-admin/pom.xml spring-boot:run`（仓库根目录）。接口契约见 `docs/api/admin-api.md`。

## 目录

| 目录 | 职责 |
|---|---|
| `api/` | 一个 controller 一个模块，只放请求函数；`http.ts` 是唯一 axios 实例 |
| `components/` | 跨视图展示组件 |
| `composables/` | 跨视图的有状态逻辑 |
| `config/menu.ts` | 侧边栏与路由表的唯一来源 |
| `types/api.ts` | 全部线格式类型 |
| `utils/` | 展示格式化、与后端一致的校验规则 |
| `views/` | 路由页面 |

## 约定

- 响应信封 `{ code, message, data }`，`code === 200` 为成功；业务失败也是 HTTP 200，只有未认证是 401。
- 成功拦截器已把 `data` 拆包，调用点写双泛型 `http.get<unknown, T>(...)`——第一个泛型故意是 `unknown`。
- 失败提示由 `http.ts` 统一弹出，业务代码里 `catch` 后留空即可。
- 写操作一律 POST；管理端没有删除接口。
