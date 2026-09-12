import axios from 'axios'
import type { AxiosResponse } from 'axios'
import type { Result } from '@/types/api'
import { useAuthStore } from '@/stores/auth'
import { showToast } from '@/components/toast'

/**
 * axios 实例：统一拆包 Result<T>、携带 Bearer、401 时用 refreshToken 无感刷新并重放原请求。
 * 调用方拿到的是 data 本身；业务失败（HTTP 200 + code≠0000）走 reject。
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState()
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

/** 并发 401 共享同一次刷新 */
let refreshing: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const { refreshToken, setTokens, clear } = useAuthStore.getState()
  if (!refreshToken) {
    throw new Error('no refresh token')
  }
  // 裸实例：不走本文件拦截器，避免刷新失败递归
  const raw = axios.create()
  const resp = await raw.post<Result<{ accessToken: string; refreshToken: string }>>(
    '/api/user/auth/refresh',
    { refreshToken },
  )
  const body = resp.data
  if (body.code !== '0000') {
    clear()
    throw new Error(body.message)
  }
  setTokens(body.data.accessToken, body.data.refreshToken)
  return body.data.accessToken
}

function kickToLogin() {
  useAuthStore.getState().clear()
  const current = location.pathname + location.search
  if (!location.pathname.startsWith('/login')) {
    location.href = `/login?redirect=${encodeURIComponent(current)}`
  }
}

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as Result
    if (body.code === '0000') {
      // 约定：拦截器直接把业务 data 交给调用方，调用方用 http.post<unknown, T> 声明真实类型
      return body.data as unknown as AxiosResponse
    }
    showToast(body.message || '操作失败', 'error')
    return Promise.reject(new Error(body.message || '操作失败'))
  },
  async (error) => {
    const status = error.response?.status
    const body = error.response?.data as Result | undefined
    const original = error.config as (typeof error.config & { _retried?: boolean }) | undefined

    if (status === 401 && original && !original._retried && !original.url?.includes('/auth/')) {
      original._retried = true
      try {
        refreshing = refreshing ?? refreshAccessToken().finally(() => (refreshing = null))
        const token = await refreshing
        original.headers = { ...(original.headers ?? {}), Authorization: `Bearer ${token}` }
        return http(original)
      } catch {
        kickToLogin()
        showToast('登录已过期，请重新登录', 'error')
        return Promise.reject(error)
      }
    }

    if (status === 401) {
      kickToLogin()
      showToast(body?.message || '登录已过期，请重新登录', 'error')
    } else {
      showToast(body?.message || '网络异常，请稍后重试', 'error')
    }
    return Promise.reject(error)
  },
)

export default http
