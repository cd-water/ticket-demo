import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { useNavigate } from 'react-router'
import { changePassword, logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { showToast } from '@/components/toast'

/** 与后端 ChangePasswordRequest 的 @Pattern 一致 */
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/
const PASSWORD_HINT = '密码需8-20位，且包含字母与数字'

/** 首页占位：游客可访问；右上角登录入口（已登录显示昵称，hover 出下拉：修改密码 / 退出登录）。 */
export default function HomePage() {
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const clear = useAuthStore((s) => s.clear)

  const [pwdOpen, setPwdOpen] = useState(false)
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [saving, setSaving] = useState(false)

  async function onLogout() {
    const { refreshToken } = useAuthStore.getState()
    try {
      if (refreshToken) await logout(refreshToken)
    } catch {
      /* 即使后端吊销失败也本地退出 */
    }
    clear()
    showToast('已退出登录', 'success')
    navigate('/', { replace: true })
  }

  function openPwdModal() {
    setNewPassword('')
    setConfirmPassword('')
    setPwdOpen(true)
  }

  async function onSubmitPwd(e: SubmitEvent) {
    e.preventDefault()
    if (!PASSWORD_PATTERN.test(newPassword) || !PASSWORD_PATTERN.test(confirmPassword)) {
      showToast(PASSWORD_HINT, 'error')
      return
    }
    if (newPassword !== confirmPassword) {
      showToast('两次输入密码不一致', 'error')
      return
    }
    setSaving(true)
    try {
      await changePassword(newPassword, confirmPassword)
      showToast('密码修改成功', 'success')
      setPwdOpen(false)
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="min-h-dvh bg-bg">
      <header className="sticky top-0 z-40 border-b border-line bg-bg">
        <div className="mx-auto flex max-w-[1200px] items-center gap-7 px-6 py-3.5">
          <div className="flex items-center gap-2 font-[family-name:var(--font-serif-cn)] text-xl font-extrabold">
            <span className="size-3 rounded-[3px] bg-brand shadow-[0_0_0_4px_rgba(255,195,0,0.25)]" />
            电影票务系统
          </div>
          <div className="ml-auto flex items-center gap-3.5">
            {user ? (
              <div className="group relative">
                <button
                  type="button"
                  className="flex cursor-pointer items-center gap-2 text-sm"
                  onClick={() => showToast('「我的」页面建设中', 'info')}
                >
                  <span className="flex size-[30px] items-center justify-center rounded-full bg-brand text-[13px] font-bold text-on-brand">
                    {user.nickname.slice(-2)}
                  </span>
                  {user.nickname}
                </button>
                <div className="invisible absolute right-0 top-full z-50 pt-2 opacity-0 transition-opacity group-hover:visible group-hover:opacity-100">
                  <div className="w-40 rounded-xl border border-line bg-card py-1.5 shadow-[var(--shadow-card)]">
                    <button
                      type="button"
                      className="block w-full cursor-pointer px-4 py-2 text-left text-sm hover:bg-bg-deep"
                      onClick={openPwdModal}
                    >
                      修改密码
                    </button>
                    <button
                      type="button"
                      className="block w-full cursor-pointer px-4 py-2 text-left text-sm text-[#E5484D] hover:bg-bg-deep"
                      onClick={onLogout}
                    >
                      退出登录
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <button
                type="button"
                className="cursor-pointer rounded-full bg-brand px-5 py-2 text-sm font-bold text-on-brand transition-colors hover:bg-brand-deep hover:text-white"
                onClick={() => navigate('/login')}
              >
                登录
              </button>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-[1200px] px-6">
        <div className="mt-6 rounded-2xl bg-gradient-to-br from-bg-deep to-[#FFE9B8]" style={{ minHeight: '420px' }} />
      </main>

      {pwdOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 p-6"
          onClick={() => setPwdOpen(false)}
        >
          <div
            className="w-[400px] max-w-full rounded-[20px] bg-card p-7 shadow-[var(--shadow-card)]"
            onClick={(e) => e.stopPropagation()}
          >
            <h2 className="mb-4 font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">修改密码</h2>
            <form onSubmit={onSubmitPwd}>
              <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="new-password">
                新密码
              </label>
              <input
                id="new-password"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="8-20位，含字母与数字"
                maxLength={20}
                autoComplete="new-password"
                className="mb-4 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
              />
              <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="confirm-password">
                确认密码
              </label>
              <input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="请再次输入新密码"
                maxLength={20}
                autoComplete="new-password"
                className="mb-5 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
              />
              <div className="flex gap-3">
                <button
                  type="button"
                  className="flex-1 cursor-pointer rounded-full border border-line py-2.5 text-sm font-semibold text-ink-2 hover:text-brand-deep"
                  onClick={() => setPwdOpen(false)}
                >
                  取消
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 cursor-pointer rounded-full bg-brand py-2.5 text-sm font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
                >
                  {saving ? '保存中…' : '确认修改'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
