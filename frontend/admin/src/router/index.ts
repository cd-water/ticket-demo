import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ALL_MENUS } from '@/config/menu'

/** 菜单 key → 页面组件（dashboard 暂留占位） */
const VIEWS: Record<string, () => Promise<unknown>> = {
  dashboard: () => import('@/views/PlaceholderView.vue'),
  movies: () => import('@/views/MovieView.vue'),
  cinemas: () => import('@/views/CinemaView.vue'),
  users: () => import('@/views/UserView.vue'),
  banners: () => import('@/views/BannerView.vue'),
  orders: () => import('@/views/OrderView.vue'),
  admins: () => import('@/views/AdminView.vue'),
  halls: () => import('@/views/HallView.vue'),
  screenings: () => import('@/views/ScreeningView.vue'),
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layout/AdminLayout.vue'),
      redirect: '/dashboard',
      children: ALL_MENUS.map((m) => ({
        path: m.key,
        component: VIEWS[m.key] ?? (() => import('@/views/PlaceholderView.vue')),
        meta: { title: m.title },
      })),
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

/** 登录后的默认落地页；被拦截的是它时不带 redirect，保持登录页 URL 干净 */
const DEFAULT_PAGE = '/dashboard'

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.isLoggedIn) {
    return {
      path: '/login',
      query: to.fullPath === DEFAULT_PAGE ? undefined : { redirect: to.fullPath },
    }
  }
  return true
})

export default router
