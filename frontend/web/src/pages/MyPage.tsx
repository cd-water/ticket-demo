import { useEffect, useState, type SubmitEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { changePassword } from '@/api/auth'
import { updateProfile } from '@/api/user'
import { cancelOrder, pageOrders, payOrder } from '@/api/order'
import type { OrderVO } from '@/types/api'
import { useAuthStore } from '@/stores/auth'
import { TopBar } from '@/components/TopBar'
import { showToast } from '@/components/toast'
import { formatMoneyShort, formatScreeningTime } from '@/lib/format'

const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,20}$/
const PASSWORD_HINT = '密码需 8-20 位，且包含字母与数字'

const STATUS_LABEL: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已取消',
}

/** 我的页面：顶 tab「我的订单 / 账号设置」；订单 tab 内三个状态子 tab */
export default function MyPage() {
  const user = useAuthStore((s) => s.user)
  const [tab, setTab] = useState<'orders' | 'profile'>('orders')
  const [status, setStatus] = useState<number>(0)

  if (!user) {
    return <Navigate to={`/login?redirect=${encodeURIComponent('/me')}`} replace />
  }

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[560px] px-6 pb-20">
        <h1 className="mt-7 mb-5 font-[family-name:var(--font-serif-cn)] text-[26px] font-extrabold">
          我的
        </h1>

        <div className="mb-5 flex gap-6 border-b border-line text-sm">
          <TabBtn on={tab === 'orders'} onClick={() => setTab('orders')}>
            我的订单
          </TabBtn>
          <TabBtn on={tab === 'profile'} onClick={() => setTab('profile')}>
            账号设置
          </TabBtn>
        </div>

        {tab === 'orders' && <OrdersPanel status={status} setStatus={setStatus} />}
        {tab === 'profile' && <ProfilePanel />}
      </main>
    </div>
  )
}

function TabBtn({
  on,
  onClick,
  children,
}: {
  on: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`cursor-pointer border-b-2 pb-2 transition-colors ${
        on
          ? 'border-brand text-ink'
          : 'border-transparent text-ink-2 hover:text-ink'
      }`}
    >
      {children}
    </button>
  )
}

function OrdersPanel({
  status,
  setStatus,
}: {
  status: number
  setStatus: (s: number) => void
}) {
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const [orders, setOrders] = useState<OrderVO[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [actingId, setActingId] = useState<number | null>(null)

  useEffect(() => {
    if (!user) return
    let cancelled = false
    setLoading(true)
    pageOrders(1, 50, status)
      .then((r) => {
        if (!cancelled) setOrders(r.records)
      })
      .catch(() => {
        if (!cancelled) setOrders([])
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [user, status])

  async function pay(o: OrderVO) {
    setActingId(o.id)
    try {
      await payOrder(o.id)
      showToast('支付成功', 'success')
      refresh()
    } finally {
      setActingId(null)
    }
  }

  async function cancel(o: OrderVO) {
    setActingId(o.id)
    try {
      await cancelOrder(o.id)
      showToast('订单已取消', 'success')
      refresh()
    } finally {
      setActingId(null)
    }
  }

  function refresh() {
    pageOrders(1, 50, status).then((r) => setOrders(r.records)).catch(() => {})
  }

  return (
    <div>
      <div className="mb-3 flex gap-5 border-b border-line text-[13px]">
        <SubTabBtn on={status === 0} onClick={() => setStatus(0)}>
          待支付
        </SubTabBtn>
        <SubTabBtn on={status === 1} onClick={() => setStatus(1)}>
          已支付
        </SubTabBtn>
        <SubTabBtn on={status === 2} onClick={() => setStatus(2)}>
          已取消
        </SubTabBtn>
      </div>

      {loading && (
        <div className="py-10 text-center text-[13px] text-ink-2">加载中…</div>
      )}

      {!loading && orders && orders.length === 0 && (
        <div className="mt-10 py-16 text-center text-[13px] text-ink-2">
          该分类暂无订单
        </div>
      )}

      {!loading && orders && orders.length > 0 && (
        <ul className="space-y-3">
          {orders.map((o) => (
            <OrderCard
              key={o.id}
              order={o}
              acting={actingId === o.id}
              onClick={() => navigate(`/orders/${o.id}`)}
              onPay={() => pay(o)}
              onCancel={() => cancel(o)}
            />
          ))}
        </ul>
      )}
    </div>
  )
}

function OrderCard({
  order,
  acting,
  onClick,
  onPay,
  onCancel,
}: {
  order: OrderVO
  acting: boolean
  onClick: () => void
  onPay: () => void
  onCancel: () => void
}) {
  return (
    <li
      className={`rounded-2xl bg-card p-5 shadow-[var(--shadow-card)] ${
        order.status === 0 ? 'cursor-pointer' : ''
      }`}
      onClick={onClick}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="truncate font-[family-name:var(--font-serif-cn)] text-base font-extrabold">
            {order.movieTitle ?? '电影'}
          </div>
          <div className="mt-0.5 text-[12px] text-ink-2">
            {order.cinemaName} · {order.hallName}
            {order.startTime && (
              <>
                {' · '}
                {formatScreeningTime(order.startTime)}
              </>
            )}
          </div>
        </div>
        <StatusPill status={order.status} />
      </div>

      <div className="mt-3 flex flex-wrap gap-1.5 text-[12px] text-ink-2">
        {(order.seats ?? []).map((s, i) => (
          <span key={i} className="rounded bg-bg-deep px-2 py-0.5">
            {s}
          </span>
        ))}
        <span className="ml-auto font-mono font-bold text-ink">
          ¥{formatMoneyShort(order.totalAmount)}
        </span>
      </div>

      {order.status === 1 && order.ticketCode && (
        <div className="mt-2 text-[12px] text-ink-2">
          取票码{' '}
          <span className="font-mono font-bold text-ink">
            {order.ticketCode.slice(0, 4)} {order.ticketCode.slice(4)}
          </span>
        </div>
      )}

      {order.status === 2 && order.cancelReason && (
        <div className="mt-2 text-[12px] text-ink-2">{order.cancelReason}</div>
      )}

      {order.status === 0 && (
        <div
          className="mt-3 flex justify-end gap-2"
          onClick={(e) => e.stopPropagation()}
        >
          <button
            type="button"
            onClick={onCancel}
            disabled={acting}
            className="cursor-pointer rounded-full border border-line bg-card px-4 py-1.5 text-[13px] transition-colors hover:border-red-400 hover:text-red-500 disabled:opacity-60"
          >
            取消订单
          </button>
          <button
            type="button"
            onClick={onPay}
            disabled={acting}
            className="cursor-pointer rounded-full bg-brand px-5 py-1.5 text-[13px] font-bold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:opacity-60"
          >
            {acting ? '处理中…' : '去支付'}
          </button>
        </div>
      )}
    </li>
  )
}

function StatusPill({ status }: { status: number }) {
  const cls =
    status === 1
      ? 'bg-[#E5F4E9] text-[#2FA84F]'
      : status === 2
        ? 'bg-bg-deep text-ink-2'
        : 'bg-[#FFF5DC] text-[#B57000]'
  return (
    <span className={`shrink-0 rounded-full px-2.5 py-0.5 text-[11px] font-bold ${cls}`}>
      {STATUS_LABEL[status] ?? '未知'}
    </span>
  )
}

function SubTabBtn({
  on,
  onClick,
  children,
}: {
  on: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`cursor-pointer border-b-2 pb-2 transition-colors ${
        on
          ? 'border-brand text-ink font-bold'
          : 'border-transparent text-ink-2 hover:text-ink'
      }`}
    >
      {children}
    </button>
  )
}

function ProfilePanel() {
  const user = useAuthStore((s) => s.user)!
  const setUser = useAuthStore((s) => s.setUser)

  const [draftNickname, setDraftNickname] = useState(() => user.nickname)
  const [savingNick, setSavingNick] = useState(false)

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [savingPwd, setSavingPwd] = useState(false)

  async function onSaveNickname(e: SubmitEvent) {
    e.preventDefault()
    const trimmed = draftNickname.trim()
    if (trimmed.length < 1 || trimmed.length > 50) {
      showToast('昵称需 1-50 个字符', 'error')
      return
    }
    if (trimmed === user.nickname) {
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
      /* http.ts 弹错 */
    } finally {
      setSavingPwd(false)
    }
  }

  return (
    <div>
      <section className="mb-4 rounded-2xl bg-card p-6 shadow-[var(--shadow-card)]">
        <div className="font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">
          {user.nickname}
        </div>
        <div className="text-[13px] text-ink-2">{user.phone}</div>
      </section>

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
    </div>
  )
}