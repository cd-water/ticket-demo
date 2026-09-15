import { useNavigate } from 'react-router'
import type { MovieVO } from '@/types/api'
import { posterFallback } from '@/lib/format'

interface PosterCardProps {
  movie: Pick<MovieVO, 'id' | 'title'> & { poster?: string }
  badge?: { text: string; tone?: 'hot' | 'coming' }
  subline?: string
  price?: number
}

/** 海报卡片：点击跳转 /movies/{id}；图片缺失时回退到米黄渐变占位 */
export function PosterCard({ movie, badge, subline, price }: PosterCardProps) {
  const navigate = useNavigate()
  const fallback = posterFallback(movie.id)

  return (
    <button
      type="button"
      onClick={() => navigate(`/movies/${movie.id}`)}
      className="group block w-full cursor-pointer text-left"
    >
      <div
        className="relative aspect-[2/3] overflow-hidden rounded-xl shadow-[var(--shadow-card)] transition-transform group-hover:-translate-y-1"
        style={{
          backgroundImage: movie.poster ? `url(${movie.poster})` : fallback,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      >
        {badge && (
          <span
            className={`absolute left-2 top-2 rounded-md px-2 py-0.5 text-[11px] font-bold text-white ${
              badge.tone === 'coming' ? 'bg-ink-2' : 'bg-black/45'
            }`}
          >
            {badge.text}
          </span>
        )}
      </div>
      <div className="mt-2.5 font-[family-name:var(--font-serif-cn)] text-[15px] font-bold leading-tight">
        {movie.title}
      </div>
      {subline && <div className="mt-0.5 text-xs text-ink-2">{subline}</div>}
      {price !== undefined && (
        <div className="mt-1 text-sm font-bold text-brand-deep">
          ¥{price}
          <span className="text-[11px] font-normal text-ink-2"> 起</span>
        </div>
      )}
    </button>
  )
}