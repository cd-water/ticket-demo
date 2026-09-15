import { useEffect, useState } from 'react'
import { Navigate, useParams, useSearchParams } from 'react-router'
import { cancelOrder, orderDetail, payOrder } from '@/api/order'
import type { OrderVO } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { useAuthStore } from '@/stores/auth'
import { showToast } from '@/components/toast'
import { formatMoneyShort, formatScreeningTime } from '@/lib/format'

const ORDER_STATUS_LABEL: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已取消',
}

/** 订单详情 / 确认订单：from=checkout 显示"立即支付/稍后支付"按钮与 15 分钟倒计时 */
export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [params] = useSearchParams()
  const fromCheckout = params.get('from') === 'checkout'
  const isAuthed = useAuthStore((s) => !!s.accessToken)

  const [order, setP] = useState<OrderVO | null>(null)
  const [loading, setLoading] = useState(true)
  const [acting, setActing] = useState(false)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    let cancelled = false
    orderDetail(Number(id))
      .then((o) => {
        if (!cancelled) setP(o)
      })
      .catch(() => {
        if (!cancelled) showToast('订单不存在或非本人', 'error')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  async function pay() {
    if (!order || acting) return
    setActing(true)
    try {
      const updated = await payOrder(order.id)
      setP(updated)
      showToast('支付成功，座位已锁定', 'success')
    } catch {
      /* 错误已由 http.ts 弹错 */
    } finally {
      setActing(false)
    }
  }

  async function cancel() {
    if (!order || acting) return
    setActing(true)
    try {
      await cancelOrder(order.id)
      const fresh = await orderDetail(order.id)
      setP(fresh)
      showToast('订单已取消，座位已释放', 'success')
    } catch {
      /* http.ts 弹错 */
    } finally {
      setActing(false)
    }
  }

  function deferPay() {
    showToast('已保存，可在「我的订单」继续支付', 'success')
  }

  if (!isAuthed) {
    return (
      <Navigate
        to={`/login?redirect=${encodeURIComponent(`/orders/${id}`)}`}
        replace
      />
    )
  }

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[560px] px-6 pb-20">
        {fromCheckout && !loading && order && order.status === 0 && (
          <h1 className="mt-7 mb-5 font-[family-name:var(--font-serif-cn)] text-[24px] font-extrabold">
            确认订单
          </h1>
        )}

        {loading && (
          <div className="mt-10 py-10 text-center text-[13px] text-ink-2">加载中…</div>
        )}

        {!loading && order && (
          <>
            <div className="flex items-center justify-between rounded-2xl bg-card p-5 shadow-[var(--shadow-card)]">
              <div>
                <div className="text-[12px] text-ink-2">订单号</div>
                <div className="mt-0.5 font-mono text-sm">{order.orderNo}</div>
              </div>
              <StatusPill status={order.status} />
            </div>

            <section className="mt-4 rounded-2xl bg-card p-5 shadow-[var(--shadow-card)]">
              <div className="flex gap-4">
                {order.moviePoster && (
                  <div
                    className="h-[120px] w-[80px] shrink-0 rounded-[10px] bg-cover bg-center"
                    style={{ backgroundImage: `url(${order.moviePoster})` }}
                  />
                )}
                <div className="min-w-0 flex-1">
                  <div className="truncate font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">
                    {order.movieTitle ?? '电影'}
                  </div>
                  <div className="mt-1 text-[13px] text-ink-2">{order.cinemaName}</div>
                  <div className="mt-1 text-[13px] text-ink-2">
                    {order.hallName}
                    {order.startTime && (
                      <>
                        {' · '}
                        {formatScreeningTime(order.startTime)}
                      </>
                    )}
                  </div>
                </div>
              </div>

              <div className="mt-4 border-t border-line pt-4">
                <div className="text-[12px] text-ink-2">座位</div>
                <div className="mt-2 flex flex-wrap gap-2">
                  {(order.items ?? []).map((it, i) => (
                    <span
                      key={i}
                      className="rounded-md bg-brand px-3 py-1 text-[13px] font-bold text-[#5c3d00]"
                    >
                      {it.seatNo}
                    </span>
                  ))}
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between border-t border-line pt-4 text-[13px]">
                <span className="text-ink-2">总价</span>
                <span className="font-mono text-xl font-bold text-brand-deep">
                  ¥{formatMoneyShort(order.totalAmount)}
                </span>
              </div>
            </section>

            {/* 取票码（已支付） */}
            {order.status === 1 && order.ticketCode && (
              <section className="mt-4 rounded-2xl bg-card p-5 shadow-[var(--shadow-card)]">
                <div className="text-[12px] text-ink-2">取票码</div>
                <div className="mt-2 flex items-baseline gap-2">
                  <span className="font-mono text-3xl font-bold tracking-wider">
                    {order.ticketCode.slice(0, 4)}
                  </span>
                  <span className="font-mono text-3xl font-bold tracking-wider">
                    {order.ticketCode.slice(4)}
                  </span>
                </div>
                <p className="mt-2 text-[12px] text-ink-2">请于影院自助取票机输入取票码</p>
              </section>
            )}

            {/* 倒计时（待支付） */}
            {order.status === 0 && order.payExpireTime && (
              <CountdownHint expireAt={order.payExpireTime} />
            )}

            {/* 取消原因（已取消） */}
            {order.status === 2 && order.cancelReason && (
              <section className="mt-4 rounded-2xl bg-card p-5 text-[13px] text-ink-2 shadow-[var(--shadow-card)]">
                取消原因：{order.cancelReason}
              </section>
            )}

            {/* 操作区 */}
            <div className="mt-6 flex flex-col gap-3">
              {order.status === 0 && (
                <>
                  {fromCheckout ? (
                    <>
                      <button
                        type="button"
                        onClick={deferPay}
                        className="w-full cursor-pointer rounded-full border border-line bg-card py-3 text-[15px] font-bold transition-colors hover:border-brand-deep hover:text-brand-deep"
                      >
                        稍后支付
                      </button>
                      <button
                        type="button"
                        onClick={pay}
                        disabled={acting}
                        className="w-full cursor-pointer rounded-full bg-brand py-3 text-[16px] font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
                      >
                        {acting ? '处理中…' : '立即支付'}
                      </button>
                    </>
                  ) : (
                    <>
                      <button
                        type="button"
                        onClick={cancel}
                        disabled={acting}
                        className="w-full cursor-pointer rounded-full border border-line bg-card py-3 text-[15px] font-bold transition-colors hover:border-red-400 hover:text-red-500 disabled:cursor-wait disabled:opacity-70"
                      >
                        {acting ? '处理中…' : '取消订单'}
                      </button>
                      <button
                        type="button"
                        onClick={pay}
                        disabled={acting}
                        className="w-full cursor-pointer rounded-full bg-brand py-3 text-[16px] font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-wait disabled:opacity-70"
                      >
                        {acting ? '处理中…' : '去支付'}
                      </button>
                    </>
                  )}
                  <p className="text-center text-[12px] text-ink-2">
                    待支付订单 <CountdownInline expireAt={order.payExpireTime!} /> 后自动取消
                  </p>
                </>
              )}
            </div>
          </>
        )}
      </main>
    </div>
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
    <span
      className={`rounded-full px-3 py-1 text-[12px] font-bold ${cls}`}
    >
      {ORDER_STATUS_LABEL[status] ?? '未知'}
    </span>
  )
}

function CountdownHint({ expireAt }: { expireAt: string }) {
  const [now, setNow] = useState(Date.now())
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(t)
  }, [])
  const remaining = Math.max(0, new Date(expireAt).getTime() - now)
  if (remaining === 0) {
    return (
      <section className="mt-4 rounded-2xl bg-card p-5 text-center text-[13px] text-red-500 shadow-[var(--shadow-card)]">
        订单已过期，请刷新
      </section>
    )
  }
  return (
    <section className="mt-4 rounded-2xl bg-card p-5 shadow-[var(--shadow-card)]">
      <div className="flex items-center justify-between">
        <span className="text-[13px] text-ink-2">支付剩余时间</span>
        <span className="font-mono text-2xl font-bold text-brand-deep">
          {formatMmSs(Math.floor(remaining / 1000))}
        </span>
      </div>
    </section>
  )
}

function CountdownInline({ expireAt }: { expireAt: string }) {
  const [now, setNow] = useState(Date.now())
  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(t)
  }, [])
  const remaining = Math.max(0, Math.floor((new Date(expireAt).getTime() - now) / 1000))
  return <span className="font-mono font-bold text-brand-deep">{formatMmSs(remaining)}</span>
}

function formatMmSs(sec: number) {
  const mm = String(Math.floor(sec / 60)).padStart(2, '0')
  const ss = String(sec % 60).padStart(2, '0')
  return `${mm}:${ss}`
}