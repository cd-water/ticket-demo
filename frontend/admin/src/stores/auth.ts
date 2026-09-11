import { defineStore } from 'pinia'
import type { AdminInfo } from '@/types/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    admin: null as AdminInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    /** 由后端返回的 role 决定显示内容 */
    roleLabel: (state) => {
      if (state.admin?.role === 0) return '超级管理员'
      if (state.admin?.role === 1) return '影院管理员'
      return '未知角色'
    },
    /** 超管管辖全部影院；影院管理员限定本影院 */
    scopeLabel: (state) => {
      if (!state.admin) return ''
      if (state.admin.role === 0) return '全部影院'
      return `本影院（ID: ${state.admin.cinemaId}）`
    },
  },
  actions: {
    setSession(token: string, admin: AdminInfo) {
      this.token = token
      this.admin = admin
    },
    clear() {
      this.token = ''
      this.admin = null
    },
  },
  persist: true,
})
