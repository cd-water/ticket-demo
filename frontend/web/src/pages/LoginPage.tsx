import { useEffect, useRef, useState } from 'react'
import type { SubmitEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { loginByPassword, loginBySms, sendSmsCode } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { showToast } from '@/components/toast'

const PHONE_PATTERN = /^1[3-9]\d{9}$/

type Tab = 'sms' | 'pwd'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const setSession = useAuthStore((s) => s.setSession)

  const [tab, setTab] = useState<Tab>('sms')
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [countdown, setCountdown] = useState(0)
  const [loading, setLoading] = useState(false)
  const timerRef = useRef<ReturnType<typeof setInterval>>(null)

  useEffect(() => () => clearInterval(timerRef.current ?? undefined), [])

  const redirect = (location.search.match(/redirect=([^&]+)/)?.[1] && decodeURIComponent(location.search.match(/redirect=([^&]+)/)![1])) || '/'

  function validatePhone(): boolean {
    if (!PHONE_PATTERN.test(phone)) {
      showToast('请输入正确的手机号', 'error')
      return false
    }
    return true
  }

  async function onSendCode() {
    if (!validatePhone()) return
    if (countdown > 0) return
    try {
      await sendSmsCode(phone)
      showToast('验证码已发送', 'success')
      setCountdown(60)
      clearInterval(timerRef.current ?? undefined)
      timerRef.current = setInterval(() => {
        setCountdown((n) => {
          if (n <= 1) {
            clearInterval(timerRef.current ?? undefined)
            return 0
          }
          return n - 1
        })
      }, 1000)
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    }
  }

  async function onSubmit(e: SubmitEvent) {
    e.preventDefault()
    if (!validatePhone()) return
    if (tab === 'sms' && !code.trim()) {
      showToast('请输入验证码', 'error')
      return
    }
    if (tab === 'pwd' && !password.trim()) {
      showToast('请输入密码', 'error')
      return
    }
    setLoading(true)
    try {
      const data = tab === 'sms'
        ? await loginBySms(phone, code.trim())
        : await loginByPassword(phone, password)
      setSession(data.accessToken, data.refreshToken, data.user)
      showToast(`欢迎，${data.user.nickname}`, 'success')
      navigate(redirect, { replace: true })
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-gradient-to-br from-[#FFF9ED] to-[#FFEEC7] p-6">
      <div className="relative w-[420px] max-w-full rounded-[20px] bg-card p-9 shadow-[var(--shadow-card)]">
        <div className="mb-2 flex items-center justify-center gap-2">
          <span className="size-3 rounded-[3px] bg-brand shadow-[0_0_0_4px_rgba(255,195,0,0.25)]" />
          <span className="font-[family-name:var(--font-serif-cn)] text-[22px] font-extrabold">电影票务系统</span>
        </div>
        {/* 登录方式切换 */}
        <div className="my-6 flex gap-2 rounded-full bg-bg-deep p-1.5">
          {(['sms', 'pwd'] as const).map((t) => (
            <button
              key={t}
              type="button"
              className={`flex-1 cursor-pointer rounded-full py-2 text-sm font-semibold transition-colors ${
                tab === t ? 'bg-card text-ink shadow-[0_2px_8px_rgba(0,0,0,0.06)]' : 'text-ink-2'
              }`}
              onClick={() => setTab(t)}
            >
              {t === 'sms' ? '验证码登录' : '密码登录'}
            </button>
          ))}
        </div>

        <form onSubmit={onSubmit}>
          <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="phone">
            手机号
          </label>
          <input
            id="phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="请输入手机号"
            maxLength={11}
            autoComplete="tel"
            className="mb-4 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
          />

          {tab === 'sms' ? (
            <>
              <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="code">
                验证码
              </label>
              <div className="mb-4 flex gap-2.5">
                <input
                  id="code"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  placeholder="请输入验证码"
                  maxLength={6}
                  autoComplete="one-time-code"
                  className="w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
                />
                <button
                  type="button"
                  disabled={countdown > 0}
                  className="shrink-0 cursor-pointer rounded-full border border-line bg-card px-4 py-2 text-[13px] font-semibold text-ink-2 hover:text-brand-deep disabled:cursor-not-allowed disabled:opacity-60"
                  onClick={onSendCode}
                >
                  {countdown > 0 ? `${countdown}s` : '获取验证码'}
                </button>
              </div>
            </>
          ) : (
            <>
              <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="password">
                密码
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码"
                autoComplete="current-password"
                className="mb-4 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
              />
            </>
          )}

          <button
            type="submit"
            disabled={loading}
            className="mt-3 w-full cursor-pointer rounded-full bg-brand py-3 text-base font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
          >
            {loading ? '登录中…' : '登录'}
          </button>
        </form>

        <p className="mt-2.5 text-center text-[13px] text-ink-2">未注册的手机号验证后自动创建账号</p>

        <button
          type="button"
          className="mt-5 w-full cursor-pointer rounded-full border border-line bg-card py-2.5 text-[13px] font-semibold text-ink-2 transition-colors hover:border-brand-deep hover:text-brand-deep"
          onClick={() => navigate('/')}
        >
          游客身份进入
        </button>
      </div>
    </div>
  )
}
