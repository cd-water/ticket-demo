# cd-ticket 电影票务系统B端

Vue 3 + Vite + Element Plus + Pinia 单页管理后台。

## 技术栈

- **框架**：Vue 3.5 + TypeScript + Vite 8
- **UI**：Element Plus 2.14（zh-CN，强制暗黑主题）
- **状态**：Pinia 4 + pinia-plugin-persistedstate
- **HTTP**：axios（统一实例在 `src/api/http.ts`）
- **图表**：echarts 6
- **路由**：vue-router 4（菜单驱动 + 登录守卫）
- **包管理**：pnpm

## 命令

```bash
pnpm install
pnpm dev      # http://localhost:5601，/api 代理到 8601
pnpm build    # vue-tsc 类型检查 + vite build
pnpm preview
```

## 项目结构

```
src/
├── api/         每个领域一个文件：admins / auth / banners / cinemas / dashboard /
│                files / halls / http / movies / orders / screenings / users
├── components/  跨视图复用：ImageUpload / ListPager / SeatCanvas / StatusFilter / StatusPill
├── composables/ useList / usePagedList / useFormDialog / useCinemaId
├── config/menu.ts       侧边栏菜单的唯一来源（与 router 的 VIEWS 一一对应）
├── layout/AdminLayout.vue
├── router/index.ts      菜单驱动 + 路由守卫；未登录跳 /login?redirect=...
├── stores/auth.ts       token + 当前管理员，pinia 持久化
├── styles/              tokens.css（设计变量）+ list.css（列表样式）
├── types/api.ts         与后端 DTO 一一对应的线格式类型 + Result / PageResult
├── utils/               format / rules（el-form 校验规则）
├── views/               各业务页面；CinemaDetailView 下含 halls / screenings / orders 三 tab
└── main.ts              装配 Pinia + Router + Element Plus
```

Vite 配置：`@` → `src/`，dev 端口 5601，`/api` 代理到 8601。