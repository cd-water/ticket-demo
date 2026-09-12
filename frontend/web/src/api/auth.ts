import http from '@/api/http'
import type { LoginResponse } from '@/types/api'

export function sendSmsCode(phone: string) {
  return http.post<unknown, void>('/user/auth/sms-code', { phone })
}

export function loginBySms(phone: string, code: string) {
  return http.post<unknown, LoginResponse>('/user/auth/login/sms', { phone, code })
}

export function loginByPassword(phone: string, password: string) {
  return http.post<unknown, LoginResponse>('/user/auth/login/password', { phone, password })
}

export function changePassword(newPassword: string, confirmPassword: string) {
  return http.post<unknown, void>('/user/me/password', { newPassword, confirmPassword })
}

export function logout(refreshToken: string) {
  return http.post<unknown, void>('/user/auth/logout', { refreshToken })
}
