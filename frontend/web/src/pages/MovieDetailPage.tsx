import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { getMovie } from '@/api/movie'
import type { MovieDetailVO } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { showToast } from '@/components/toast'
import { formatDate, formatDuration, posterFallback } from '@/lib/format'

/** 电影详情：海报 + 元信息 + 简介 + 购票按钮（购票跳转影院列表） */
export default function MovieDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [movie, setMovie] = useState<MovieDetailVO | null>(null)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    getMovie(Number(id))
      .then((m) => {
        if (!cancelled) setMovie(m)
      })
      .catch(() => {
        if (!cancelled) {
          setMovie(null)
          showToast('影片不存在或已下架', 'error')
          navigate('/', { replace: true })
        }
      })
    return () => {
      cancelled = true
    }
  }, [id, navigate])

  const loading = movie === null

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[1200px] px-6 pb-20">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="mt-5 inline-flex cursor-pointer items-center gap-1.5 text-sm text-ink-2 hover:text-brand-deep"
        >
          ‹ 返回
        </button>

        {loading && <div className="py-16 text-center text-[13px] text-ink-2">加载中…</div>}

        {movie && (
          <div className="mt-3 grid grid-cols-[280px_1fr] items-start gap-10 max-[760px]:grid-cols-1">
            <div
              className="aspect-[2/3] rounded-2xl shadow-[var(--shadow-card)]"
              style={{
                backgroundImage: movie.poster ? `url(${movie.poster})` : posterFallback(movie.id),
                backgroundSize: 'cover',
                backgroundPosition: 'center',
              }}
            />
            <div>
              <h1 className="font-[family-name:var(--font-serif-cn)] text-[30px] font-extrabold leading-tight">
                {movie.title}
              </h1>
              <div className="mt-3 text-sm leading-relaxed text-ink-2">
                类型 · {movie.showStatus === 'hot' ? '热映' : '待映'}
                <br />
                时长 · {formatDuration(movie.duration)}
                <br />
                上映 · {formatDate(movie.releaseDate)}
              </div>
              <p className="mt-4.5 text-sm leading-relaxed">{movie.description}</p>
              <div className="mt-6.5 flex items-center gap-4">
                {movie.showStatus === 'hot' ? (
                  <button
                    type="button"
                    onClick={() => navigate('/cinemas')}
                    className="cursor-pointer rounded-full bg-brand px-11 py-3.5 text-[17px] font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white"
                  >
                    立即购票
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => showToast('影片尚未上映，敬请期待', 'info')}
                    className="cursor-pointer rounded-full border border-line bg-card px-11 py-3.5 text-[17px] font-bold text-ink-2 hover:border-brand-deep hover:text-brand-deep"
                  >
                    想看
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}