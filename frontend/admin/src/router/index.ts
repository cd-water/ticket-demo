import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MENUS } from '@/config/menu'

const VIEWS: Record<string, () => Promise<unknown>> = {
  dashboard: () => import('@/views/PlaceholderView.vue'),
  movies: () => import('@/views/MovieView.vue'),
  cinemas: () => import('@/views/CinemaView.vue'),
  users: () => import('@/views/UserView.vue'),
  banners: () => import('@/views/BannerView.vue'),
  admins: () => import('@/views/AdminView.vue'),
  cinemaHalls: () => import('@/views/CinemaHallsView.vue'),
  cinemaScreenings: () => import('@/views/CinemaScreeningsView.vue'),
  cinemaOrders: () => import('@/views/CinemaOrdersView.vue'),
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
          component: VIEWS[m.key] ?? (() => import('@/views/PlaceholderView.vue')),
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
              component: VIEWS.cinemaHalls,
              meta: { title: '影厅管理' },
            },
            {
              path: 'screenings',
              component: VIEWS.cinemaScreenings,
              meta: { title: '排场管理' },
            },
            {
              path: 'orders',
              component: VIEWS.cinemaOrders,
              meta: { title: '订单管理' },
            },
          ],
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

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
