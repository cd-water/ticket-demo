import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { Navigate } from 'react-router'
import { changePassword } from '@/api/auth'
import { updateProfile } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { TopBar } from '@/components/TopBar'
import { showToast } from '@/components/toast'

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/
const PASSWORD_HINT = '密码需 8-20 位，且包含字母与数字'

/** 我的页面：展示资料 + 内联昵称修改 + 内联改密 */
export default function MyPage() {
  const user = useAuthStore((s) => s.user)
  const setUser = useAuthStore((s) => s.setUser)

  const [draftNickname, setDraftNickname] = useState(() => user?.nickname ?? '')
  const [savingNick, setSavingNick] = useState(false)

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [savingPwd, setSavingPwd] = useState(false)

  if (!user) {
    return <Navigate to={`/login?redirect=${encodeURIComponent('/me')}`} replace />
  }

  async function onSaveNickname(e: SubmitEvent) {
    e.preventDefault()
    const trimmed = draftNickname.trim()
    if (trimmed.length < 1 || trimmed.length > 50) {
      showToast('昵称需 1-50 个字符', 'error')
      return
    }
    if (trimmed === user!.nickname) {
      showToast('昵称未变化', 'info')
      return
    }
    setSavingNick(true)
    try {
      const updated = await updateProfile({ nickname: trimmed })
      setUser(updated)
      showToast('资料已更新', 'success')
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      setSavingNick(false)
    }
  }

  async function onChangePwd(e: SubmitEvent) {
    e.preventDefault()
    if (!PASSWORD_PATTERN.test(newPassword) || !PASSWORD_PATTERN.test(confirmPassword)) {
      showToast(PASSWORD_HINT, 'error')
      return
    }
    if (newPassword !== confirmPassword) {
      showToast('两次输入密码不一致', 'error')
      return
    }
    setSavingPwd(true)
    try {
      await changePassword(newPassword, confirmPassword)
      showToast('密码修改成功', 'success')
      setNewPassword('')
      setConfirmPassword('')
    } catch {
      /* 错误提示已由 http.ts 统一弹出 */
    } finally {
      setSavingPwd(false)
    }
  }

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[560px] px-6 pb-20">
        <h1 className="mt-7 mb-5 font-[family-name:var(--font-serif-cn)] text-[26px] font-extrabold">
          我的
        </h1>

        {/* 资料卡 */}
        <section className="mb-4 rounded-2xl bg-card p-6 shadow-[var(--shadow-card)]">
          <div className="flex items-center gap-3.5">
            <span className="flex size-12 items-center justify-center rounded-full bg-brand text-base font-bold text-on-brand">
              {user.nickname.slice(-2)}
            </span>
            <div>
              <div className="font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">
                {user.nickname}
              </div>
              <div className="text-[13px] text-ink-2">{user.phone}</div>
            </div>
          </div>
        </section>

        {/* 昵称内联编辑 */}
        <section className="mb-4 rounded-2xl bg-card p-6 shadow-[var(--shadow-card)]">
          <h2 className="mb-3 font-[family-name:var(--font-serif-cn)] text-base font-extrabold">
            昵称
          </h2>
          <form onSubmit={onSaveNickname}>
            <input
              value={draftNickname}
              onChange={(e) => setDraftNickname(e.target.value)}
              placeholder="1-50 个字符"
              maxLength={50}
              className="mb-4 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
            />
            <button
              type="submit"
              disabled={savingNick}
              className="w-full cursor-pointer rounded-full bg-brand py-2.5 text-sm font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
            >
              {savingNick ? '保存中…' : '保存昵称'}
            </button>
          </form>
        </section>

        {/* 修改密码（内联表单） */}
        <section className="mb-4 rounded-2xl bg-card p-6 shadow-[var(--shadow-card)]">
          <h2 className="mb-3 font-[family-name:var(--font-serif-cn)] text-base font-extrabold">
            修改密码
          </h2>
          <form onSubmit={onChangePwd}>
            <label className="mb-1.5 block text-[13px] text-ink-2" htmlFor="new-password">
              新密码
            </label>
            <input
              id="new-password"
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="8-20 位，含字母与数字"
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
              className="mb-4 w-full rounded-[10px] border border-line bg-card px-3.5 py-3 text-[15px] outline-none focus:border-brand"
            />
            <button
              type="submit"
              disabled={savingPwd}
              className="w-full cursor-pointer rounded-full bg-brand py-2.5 text-sm font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
            >
              {savingPwd ? '保存中…' : '保存新密码'}
            </button>
          </form>
        </section>
      </main>
    </div>
  )
}