import { useNavigate } from 'react-router'
import type { CinemaVO } from '@/types/api'

interface CinemaCardProps {
  cinema: CinemaVO
  screeningCount?: number
}

/** 影院卡片：右上角状态灯 + 排片数；点击进详情 */
export function CinemaCard({ cinema, screeningCount }: CinemaCardProps) {
  const navigate = useNavigate()
  const open = cinema.status === 1

  return (
    <button
      type="button"
      onClick={() => navigate(`/cinemas/${cinema.id}`)}
      className="group block w-full cursor-pointer rounded-2xl bg-card p-5 text-left shadow-[var(--shadow-card)] transition-transform hover:-translate-y-0.5"
    >
      <h3 className="font-[family-name:var(--font-serif-cn)] text-lg font-extrabold">
        {cinema.name}
      </h3>
      <p className="mt-1.5 text-[13px] text-ink-2">{cinema.address}</p>
      <div className="mt-3.5 flex items-center gap-2.5">
        <span
          className={`inline-block size-2 rounded-full ${
            open ? 'bg-[#2FA84F]' : 'bg-[#C9C2AE]'
          }`}
        />
        <span className="text-[13px]">{open ? '营业中' : '今日停业'}</span>
        {screeningCount !== undefined && (
          <span className="ml-auto text-xs text-ink-2">{screeningCount} 场排片</span>
        )}
      </div>
    </button>
  )
}