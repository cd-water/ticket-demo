import axios from 'axios'
import type { AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

/**
 * axios 实例：统一拆包 Result<T>、401 踢出、错误提示。
 * 调用方拿到的是 data 本身；业务失败（HTTP 200 + code≠200）走 reject。
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as Result
    if (body.code === 200) {
      // 约定：拦截器直接把业务 data 交给调用方，调用方用 http.post<unknown, T> 声明真实类型
      return body.data as unknown as AxiosResponse
    }
    ElMessage.error(body.message || '操作失败')
    return Promise.reject(new Error(body.message || '操作失败'))
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data as Result | undefined
    if (status === 401) {
      // 单设备踢线 / Token 过期：清会话回登录页
      useAuthStore().clear()
      if (!location.pathname.startsWith('/login')) {
        location.href = '/login'
      }
      ElMessage.error(body?.message || '登录已过期，请重新登录')
    } else {
      ElMessage.error(body?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default http
