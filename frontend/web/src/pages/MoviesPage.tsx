import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { pageMovies } from '@/api/movie'
import type { MovieVO, PageResult } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { PosterCard } from '@/components/PosterCard'
import { Pagination } from '@/components/Pagination'

type Tab = 'hot' | 'coming'

const TAB_LIST: { key: Tab; label: string }[] = [
  { key: 'hot', label: '热映电影' },
  { key: 'coming', label: '待映电影' },
]

const PAGE_SIZE = 10

/** 电影页：hot/coming 切换 + 分页；URL 同步 ?showStatus=&page= */
export default function MoviesPage() {
  const [params, setParams] = useSearchParams()
  const showStatus = (params.get('showStatus') as Tab) || 'hot'
  const page = Math.max(1, Number(params.get('page') ?? '1'))

  const [data, setData] = useState<PageResult<MovieVO> | null>(null)

  useEffect(() => {
    let cancelled = false
    pageMovies(showStatus, page, PAGE_SIZE)
      .then((res) => {
        if (!cancelled) setData(res)
      })
      .catch(() => {
        if (!cancelled) setData(null)
      })
    return () => {
      cancelled = true
    }
  }, [showStatus, page])

  function switchTab(next: Tab) {
    setParams({ showStatus: next, page: '1' })
  }

  const loading = data === null

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />
      <main className="mx-auto max-w-[1200px] px-6 pb-20">
        <div className="mt-7 mb-5 flex gap-1.5 border-b border-line">
          {TAB_LIST.map((t) => (
            <button
              key={t.key}
              type="button"
              onClick={() => switchTab(t.key)}
              className={`relative cursor-pointer px-5 py-3 text-[15px] transition-colors ${
                showStatus === t.key
                  ? 'font-bold text-ink after:absolute after:bottom-[-1px] after:left-3.5 after:right-3.5 after:h-[3px] after:rounded-[3px] after:bg-brand'
                  : 'text-ink-2 hover:text-ink'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {loading && <div className="py-10 text-center text-[13px] text-ink-2">加载中…</div>}

        {!loading && data && data.records.length === 0 && (
          <div className="py-16 text-center text-[13px] text-ink-2">该分类暂无影片</div>
        )}

        {!loading && data && data.records.length > 0 && (
          <div
            className="grid gap-x-5 gap-y-7"
            style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))' }}
          >
            {data.records.map((m) => (
              <PosterCard
                key={m.id}
                movie={m}
                badge={{ text: showStatus === 'hot' ? '热映' : '待映', tone: showStatus }}
              />
            ))}
          </div>
        )}

        {data && (
          <Pagination
            page={page}
            size={PAGE_SIZE}
            total={data.total}
            onChange={(p) => setParams({ showStatus, page: String(p) })}
          />
        )}
      </main>
    </div>
  )
}