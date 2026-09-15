import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { pageCinemas } from '@/api/cinema'
import type { CinemaVO, PageResult } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { CinemaCard } from '@/components/CinemaCard'
import { Pagination } from '@/components/Pagination'

const PAGE_SIZE = 10

/** 影院列表：分页；URL ?page= 同步 */
export default function CinemasPage() {
  const [params, setParams] = useSearchParams()
  const page = Math.max(1, Number(params.get('page') ?? '1'))

  const [data, setData] = useState<PageResult<CinemaVO> | null>(null)

  useEffect(() => {
    let cancelled = false
    pageCinemas(page, PAGE_SIZE)
      .then((res) => {
        if (!cancelled) setData(res)
      })
      .catch(() => {
        if (!cancelled) setData(null)
      })
    return () => {
      cancelled = true
    }
  }, [page])

  const loading = data === null

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[1200px] px-6 pb-20">
        <h2 className="mt-7 mb-5 font-[family-name:var(--font-serif-cn)] text-[22px] font-extrabold">
          选择影院
        </h2>

        {loading && <div className="py-10 text-center text-[13px] text-ink-2">加载中…</div>}

        {!loading && data && data.records.length === 0 && (
          <div className="py-16 text-center text-[13px] text-ink-2">暂无影院</div>
        )}

        {!loading && data && data.records.length > 0 && (
          <div className="flex flex-col gap-4">
            {data.records.map((c) => (
              <CinemaCard key={c.id} cinema={c} />
            ))}
          </div>
        )}

        {data && (
          <Pagination
            page={page}
            size={PAGE_SIZE}
            total={data.total}
            onChange={(p) => setParams({ page: String(p) })}
          />
        )}
      </main>
    </div>
  )
}