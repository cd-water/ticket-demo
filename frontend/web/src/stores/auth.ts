import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserInfo } from '@/types/api'

interface AuthState {
  accessToken: string
  refreshToken: string
  user: UserInfo | null
  setSession: (accessToken: string, refreshToken: string, user: UserInfo) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  setUser: (user: UserInfo) => void
  clear: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: '',
      refreshToken: '',
      user: null,
      setSession: (accessToken, refreshToken, user) => set({ accessToken, refreshToken, user }),
      setTokens: (accessToken, refreshToken) => set({ accessToken, refreshToken }),
      setUser: (user) => set({ user }),
      clear: () => set({ accessToken: '', refreshToken: '', user: null }),
    }),
    { name: 'auth' },
  ),
)
