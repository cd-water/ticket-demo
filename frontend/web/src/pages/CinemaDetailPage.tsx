import { useEffect, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router'
import { getCinema, listScreenings } from '@/api/cinema'
import type { CinemaDetailVO, ScreeningVO } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { SeatModal } from '@/components/SeatModal'
import { showToast } from '@/components/toast'
import { formatMoneyShort, formatScreeningTime, posterFallback } from '@/lib/format'

/** 影院详情：影院头 + 排片电影海报(横向) + 排片列表 */
export default function CinemaDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const cinemaId = Number(id)

  const [cinema, setCinema] = useState<CinemaDetailVO | null>(null)
  const [pickedMovieId, setPickedMovieId] = useState<number | undefined>(() => {
    const v = params.get('movieId')
    return v ? Number(v) : undefined
  })
  const [screenings, setScreenings] = useState<ScreeningVO[] | null>(null)
  const [seatFor, setSeatFor] = useState<ScreeningVO | null>(null)

  // 拉影院详情
  useEffect(() => {
    if (!cinemaId) return
    let cancelled = false
    getCinema(cinemaId)
      .then((c) => {
        if (!cancelled) setCinema(c)
      })
      .catch(() => {
        if (!cancelled) {
          showToast('影院不存在', 'error')
          navigate('/cinemas', { replace: true })
        }
      })
    return () => {
      cancelled = true
    }
  }, [cinemaId, navigate])

  // 拉排片（按 activeMovieId 过滤；undefined = 全部）
  const activeMovieId = pickedMovieId ?? cinema?.movies[0]?.id
  useEffect(() => {
    if (!cinemaId || activeMovieId === undefined) return
    let cancelled = false
    listScreenings(cinemaId, activeMovieId)
      .then((list) => {
        if (!cancelled) setScreenings(list)
      })
      .catch(() => {
        if (!cancelled) setScreenings([])
      })
    return () => {
      cancelled = true
    }
  }, [cinemaId, activeMovieId])

  const activeMovie = cinema?.movies.find((m) => m.id === activeMovieId)

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[1200px] px-6 pb-20">
        <button
          type="button"
          onClick={() => navigate('/cinemas')}
          className="mt-5 inline-flex cursor-pointer items-center gap-1.5 text-sm text-ink-2 hover:text-brand-deep"
        >
          ‹ 返回影院
        </button>

        {cinema && (
          <>
            <h2 className="mt-3 font-[family-name:var(--font-serif-cn)] text-[22px] font-extrabold">
              {cinema.name}
            </h2>
            <p className="-mt-2 mb-6 text-sm text-ink-2">{cinema.address}</p>

            {cinema.movies.length === 0 ? (
              <div className="py-16 text-center text-[13px] text-ink-2">该影院暂无排片</div>
            ) : (
              <>
                <h3 className="mb-3.5 font-[family-name:var(--font-serif-cn)] text-[18px] font-extrabold">
                  正在排片
                </h3>
                <div className="flex gap-4 overflow-x-auto pb-4">
                  {cinema.movies.map((m) => {
                    const on = m.id === activeMovieId
                    return (
                      <button
                        key={m.id}
                        type="button"
                        onClick={() => setPickedMovieId(m.id)}
                        className="group w-[110px] shrink-0 cursor-pointer text-center"
                      >
                        <div
                          className={`aspect-[2/3] rounded-[10px] transition-shadow ${
                            on ? 'shadow-[0_0_0_3px_var(--color-brand)]' : ''
                          }`}
                          style={{
                            backgroundImage: m.poster
                              ? `url(${m.poster})`
                              : posterFallback(m.id),
                            backgroundSize: 'cover',
                            backgroundPosition: 'center',
                          }}
                        />
                        <div
                          className={`mt-1.5 truncate text-[13px] font-semibold font-[family-name:var(--font-serif-cn)] ${
                            on ? 'text-ink' : 'text-ink-2'
                          }`}
                        >
                          {m.title}
                        </div>
                      </button>
                    )
                  })}
                </div>

                <h3 className="mb-3.5 font-[family-name:var(--font-serif-cn)] text-[18px] font-extrabold">
                  {activeMovie ? `${activeMovie.title} · 今日场次` : '今日场次'}
                </h3>

                {screenings === null && (
                  <div className="py-10 text-center text-[13px] text-ink-2">加载中…</div>
                )}

                {screenings && screenings.length === 0 && (
                  <div className="py-10 text-center text-[13px] text-ink-2">
                    该影片今日暂无场次
                  </div>
                )}

                {screenings && screenings.length > 0 && (
                  <div className="space-y-3">
                    {screenings.map((s) => (
                      <div
                        key={s.id}
                        className="flex items-center gap-4.5 rounded-xl bg-card p-4 shadow-[var(--shadow-card)]"
                      >
                        <div className="w-[74px]">
                          <div className="font-mono text-[22px] font-bold leading-none">
                            {formatScreeningTime(s.startTime).slice(-5)}
                          </div>
                          <div className="mt-1 text-[11px] text-ink-2">
                            {formatScreeningTime(s.startTime).slice(0, 5)}
                          </div>
                        </div>
                        <div className="w-[100px] text-[13px] text-ink-2">{s.hallName}</div>
                        <div className="w-[100px] font-mono text-[18px] font-bold text-brand-deep">
                          ¥{formatMoneyShort(s.price)}
                          <span className="ml-1 text-xs font-normal text-ink-2">起</span>
                        </div>
                        <button
                          type="button"
                          onClick={() => setSeatFor(s)}
                          className="ml-auto cursor-pointer rounded-full bg-brand px-5 py-2 text-sm font-bold text-on-brand transition-colors hover:bg-brand-deep hover:text-white"
                        >
                          选座购票
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </>
        )}

        {seatFor && (
          <SeatModal
            screeningId={seatFor.id}
            hallName={seatFor.hallName}
            startTime={formatScreeningTime(seatFor.startTime)}
            price={seatFor.price}
            onClose={() => setSeatFor(null)}
          />
        )}
      </main>
    </div>
  )
}