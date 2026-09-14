import { defineStore } from 'pinia'
import type { AdminInfo } from '@/types/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    admin: null as AdminInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
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
