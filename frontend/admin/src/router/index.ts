import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MENUS, type MenuKey } from '@/config/menu'

/** 菜单 key → 页面组件。漏登记会在 vue-tsc 阶段报错，而不是静默渲染成空页面。 */
const VIEWS: Record<MenuKey, () => Promise<unknown>> = {
  dashboard: () => import('@/views/DashboardView.vue'),
  movies: () => import('@/views/MovieView.vue'),
  cinemas: () => import('@/views/CinemaView.vue'),
  banners: () => import('@/views/BannerView.vue'),
  users: () => import('@/views/UserView.vue'),
  admins: () => import('@/views/AdminView.vue'),
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/layout/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        ...MENUS.map((m) => ({
          path: m.key,
          component: VIEWS[m.key],
          meta: { title: m.title },
        })),
        {
          path: 'cinemas/:cinemaId',
          component: () => import('@/views/CinemaDetailView.vue'),
          meta: { title: '影院详情' },
          children: [
            { path: '', redirect: 'halls' },
            {
              path: 'halls',
              component: () => import('@/views/CinemaHallsView.vue'),
              meta: { title: '影厅管理' },
            },
            {
              path: 'screenings',
              component: () => import('@/views/CinemaScreeningsView.vue'),
              meta: { title: '排场管理' },
            },
            {
              path: 'orders',
              component: () => import('@/views/CinemaOrdersView.vue'),
              meta: { title: '订单管理' },
            },
          ],
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

/** 登录后的默认落点 */
const DEFAULT_PAGE = '/dashboard'

/** 未登录时的跳转目标：记住原页面，默认页则不带 redirect */
export function loginRedirect(from: string) {
  return { path: '/login', query: from === DEFAULT_PAGE ? undefined : { redirect: from } }
}

router.beforeEach((to) => {
  if (to.meta.public) return true
  if (!useAuthStore().isLoggedIn) return loginRedirect(to.fullPath)
  return true
})

export default router
