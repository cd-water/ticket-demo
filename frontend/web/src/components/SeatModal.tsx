import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router'
import { getSeatMap } from '@/api/seat'
import { createOrder } from '@/api/order'
import type { SeatMapVO, SeatVO } from '@/types/api'
import { showToast } from '@/components/toast'
import { useAuthStore } from '@/stores/auth'
import { formatMoneyShort } from '@/lib/format'

interface SeatModalProps {
  screeningId: number
  hallName: string
  startTime: string
  price: number
  onClose: () => void
}

const MAX_SEATS = 6
const MIN_SEATS = 1

/** 选座弹窗：渲染座位图 + 选座 + 确认下单；选中座位超出 limit 时拒绝 */
export function SeatModal({ screeningId, hallName, startTime, price, onClose }: SeatModalProps) {
  const navigate = useNavigate()
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [seatMap, setSeatMap] = useState<SeatMapVO | null>(null)
  const [picked, setPicked] = useState<{ seatRow: number; seatCol: number }[]>([])
  const [submitting, setSubmitting] = useState(false)
  const isAuthed = useAuthStore((s) => !!s.accessToken)

  useEffect(() => {
    dialogRef.current?.showModal()
    let cancelled = false
    getSeatMap(screeningId)
      .then((m) => {
        if (!cancelled) setSeatMap(m)
      })
      .catch(() => {
        if (!cancelled) showToast('座位图加载失败', 'error')
      })
    return () => {
      cancelled = true
    }
  }, [screeningId])

  /** 按 (row, col) 索引座位，快速查找状态 */
  const seatByPos = useMemo(() => {
    const map = new Map<string, SeatVO>()
    seatMap?.seats.forEach((s) => map.set(`${s.seatRow}:${s.seatCol}`, s))
    return map
  }, [seatMap])

  function pick(seat: SeatVO) {
    if (seat.status !== 0) return
    setPicked((prev) => {
      const exists = prev.find((p) => p.seatRow === seat.seatRow && p.seatCol === seat.seatCol)
      if (exists) return prev.filter((p) => !(p.seatRow === seat.seatRow && p.seatCol === seat.seatCol))
      if (prev.length >= MAX_SEATS) {
        showToast(`最多可选 ${MAX_SEATS} 个座位`, 'info')
        return prev
      }
      return [...prev, { seatRow: seat.seatRow, seatCol: seat.seatCol }]
    })
  }

  const totalAmount = picked.length * price
  const canSubmit = picked.length >= MIN_SEATS && picked.length <= MAX_SEATS && !submitting

  async function confirm() {
    if (!isAuthed) {
      onClose()
      navigate(`/login?redirect=${encodeURIComponent(`/orders/new?screeningId=${screeningId}`)}`)
      return
    }
    if (picked.length < MIN_SEATS) {
      showToast('请选择座位', 'info')
      return
    }
    setSubmitting(true)
    try {
      const order = await createOrder({ screeningId, seats: picked })
      onClose()
      navigate(`/orders/${order.id}?from=checkout`, { replace: true })
    } catch {
      // http.ts 已弹错；保留弹窗以便用户重新选
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <dialog
      ref={dialogRef}
      className="m-auto w-[min(720px,96vw)] max-h-[92vh] overflow-auto rounded-2xl border-0 bg-white p-0 shadow-[0_20px_60px_rgba(0,0,0,0.18)] backdrop:bg-black/40"
      onClose={onClose}
      onClick={(e) => {
        if (e.target === dialogRef.current) onClose()
      }}
    >
      <div className="flex items-center justify-between border-b border-line px-6 py-4">
        <div>
          <h3 className="font-[family-name:var(--font-serif-cn)] text-xl font-extrabold">选座购票</h3>
          <p className="mt-0.5 text-[13px] text-ink-2">
            {hallName} · {startTime} · ¥{formatMoneyShort(price)}/座
          </p>
        </div>
        <button
          type="button"
          onClick={onClose}
          aria-label="关闭"
          className="cursor-pointer size-8 rounded-full text-ink-2 transition-colors hover:bg-bg-deep hover:text-ink"
        >
          ✕
        </button>
      </div>

      {!seatMap && (
        <div className="px-6 py-10 text-center text-[13px] text-ink-2">座位图加载中…</div>
      )}

      {seatMap && (
        <div className="px-6 pb-6 pt-5">
          {/* 屏幕 */}
          <div className="mx-auto h-7 max-w-[480px] rounded-lg bg-gradient-to-b from-[#F2E2B0] to-[#FFF9ED] text-center text-[11px] leading-7 text-ink-2">
            屏幕
          </div>

          {/* 座位网格 */}
          <div className="mt-5 flex flex-col items-center gap-1.5">
            {Array.from({ length: seatMap.seatRows }, (_, rowIdx) => {
              const row = rowIdx + 1
              return (
                <div key={row} className="flex items-center gap-2">
                  <span className="w-5 text-right text-[11px] text-ink-2">{row}</span>
                  <div className="flex gap-1.5">
                    {Array.from({ length: seatMap.seatCols }, (_, colIdx) => {
                      const col = colIdx + 1
                      const seat = seatByPos.get(`${row}:${col}`)
                      if (!seat) return <div key={col} className="size-8" />
                      const pickedHere = picked.some(
                        (p) => p.seatRow === row && p.seatCol === col,
                      )
                      const cls = seatClass(seat.status, pickedHere)
                      return (
                        <button
                          key={col}
                          type="button"
                          disabled={seat.status !== 0}
                          onClick={() => pick(seat)}
                          aria-label={seat.seatNo}
                          className={`flex size-8 cursor-pointer items-center justify-center rounded-md text-[11px] transition-colors ${cls}`}
                        >
                          {col}
                        </button>
                      )
                    })}
                  </div>
                </div>
              )
            })}
          </div>

          {/* 图例 */}
          <div className="mt-6 flex flex-wrap items-center justify-center gap-5 text-[12px] text-ink-2">
            <span className="flex items-center gap-1.5">
              <i className="inline-block size-3.5 rounded bg-[#F7E8BE]" />
              可选
            </span>
            <span className="flex items-center gap-1.5">
              <i className="inline-block size-3.5 rounded bg-brand" />
              已选
            </span>
            <span className="flex items-center gap-1.5">
              <i className="inline-block size-3.5 rounded bg-[#EDE7D8]" />
              已售/锁定
            </span>
            <span className="flex items-center gap-1.5">
              <i
                className="inline-block size-3.5 rounded"
                style={{
                  background:
                    'repeating-linear-gradient(45deg,#F5EFE0,#F5EFE0 3px,#EDE5D0 3px,#EDE5D0 6px)',
                }}
              />
              不可售
            </span>
          </div>
        </div>
      )}

      <div className="flex items-center gap-4 border-t border-line bg-bg-deep/40 px-6 py-4">
        <span className="text-[13px] text-ink-2">
          已选 <b className="font-mono text-xl text-brand-deep">{picked.length}</b> 座
        </span>
        <span className="text-[13px] text-ink-2">
          合计 <b className="font-mono text-xl text-brand-deep">¥{formatMoneyShort(totalAmount)}</b>
        </span>
        <button
          type="button"
          onClick={confirm}
          disabled={!canSubmit}
          className="ml-auto cursor-pointer rounded-full bg-brand px-7 py-2 text-[15px] font-bold text-on-brand transition-colors hover:bg-brand-deep hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? '下单中…' : `确认选座${picked.length > 0 ? ` · ${picked.length}座` : ''}`}
        </button>
      </div>
    </dialog>
  )
}

function seatClass(status: number, picked: boolean): string {
  if (picked) return 'bg-brand text-[#5c3d00] font-bold cursor-pointer'
  if (status === 1 || status === 2) return 'bg-[#EDE7D8] text-[#C9C2AE] cursor-not-allowed'
  if (status === 3) return 'bg-[repeating-linear-gradient(45deg,#F5EFE0,#F5EFE0_3px,#EDE5D0_3px,#EDE5D0_6px)] text-[#C9C2AE] cursor-not-allowed'
  return 'bg-[#F7E8BE] text-[#8a6d1f] hover:bg-brand cursor-pointer'
}