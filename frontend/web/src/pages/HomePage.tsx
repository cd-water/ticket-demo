import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import { listBanners } from '@/api/banner'
import { listBoxOffice, listComingMovies, listHotMovies } from '@/api/movie'
import type { BannerVO, BoxOfficeVO, MovieVO } from '@/types/api'
import { TopBar } from '@/components/TopBar'
import { PosterCard } from '@/components/PosterCard'
import { posterFallback } from '@/lib/format'

const ROTATE_MS = 5000

/** 首页：上方轮播 + 左热映/待映 + 右票房榜 */
export default function HomePage() {
  const navigate = useNavigate()
  const [banners, setBanners] = useState<BannerVO[]>([])
  const [hot, setHot] = useState<MovieVO[]>([])
  const [coming, setComing] = useState<MovieVO[]>([])
  const [box, setBox] = useState<BoxOfficeVO[]>([])

  const [bannerIdx, setBannerIdx] = useState(0)
  const [imgErr, setImgErr] = useState<Record<number, boolean>>({})

  // 4 路并发拉取
  useEffect(() => {
    Promise.allSettled([
      listBanners().then(setBanners),
      listHotMovies().then(setHot),
      listComingMovies().then(setComing),
      listBoxOffice().then(setBox),
    ])
  }, [])

  // 轮播自动切换：默认 5s 下一张；手动切换（箭头/悬停圆点）后重新计时
  useEffect(() => {
    if (banners.length <= 1) return
    const t = setInterval(() => setBannerIdx((i) => (i + 1) % banners.length), ROTATE_MS)
    return () => clearInterval(t)
  }, [banners.length, bannerIdx])

  const prev = () => setBannerIdx((i) => (i - 1 + banners.length) % banners.length)
  const next = () => setBannerIdx((i) => (i + 1) % banners.length)

  function gotoBanner(b: BannerVO) {
    if (!b.linkUrl) return
    // 站外完整 URL（如 https://www.baidu.com）走 location.assign；站内路径走 SPA navigate
    if (/^https?:\/\//.test(b.linkUrl)) {
      window.location.assign(b.linkUrl)
    } else if (b.linkUrl.startsWith('/')) {
      navigate(b.linkUrl)
    }
  }

  const activeBanner = banners[bannerIdx]

  return (
    <div className="min-h-dvh bg-bg">
      <TopBar />

      {/* 轮播图 */}
      <main className="mx-auto max-w-[1200px] px-6 pb-20">
        <div
          className="relative mt-6 overflow-hidden rounded-2xl"
          style={{ aspectRatio: '1200 / 360' }}
        >
          {banners.length === 0 ? (
            <div
              className="absolute inset-0"
              style={{
                background: 'linear-gradient(120deg, #FFF3D6, #FFE9B8)',
              }}
            >
              <div className="absolute inset-0 flex items-center px-12">
                <div>
                  <h1 className="font-[family-name:var(--font-serif-cn)] text-[34px] font-extrabold leading-tight">
                    今晚，和谁一起
                    <br />
                    看一场好电影？
                  </h1>
                  <p className="mt-3 text-[15px] text-ink-2">
                    热门大片、黄金场次、最佳座位，一站选齐。
                  </p>
                  <button
                    type="button"
                    onClick={() => navigate('/movies')}
                    className="mt-6 cursor-pointer rounded-full bg-brand px-8 py-3 text-[15px] font-extrabold text-on-brand transition-colors hover:bg-brand-deep hover:text-white"
                  >
                    立即购票
                  </button>
                </div>
              </div>
            </div>
          ) : (
            banners.map((b, i) => {
              const active = i === bannerIdx
              const useFallback = !b.image || imgErr[b.id]
              return (
                <div
                  key={b.id}
                  onClick={() => gotoBanner(b)}
                  className={`absolute inset-0 cursor-pointer transition-opacity duration-700 ${
                    active ? 'opacity-100' : 'pointer-events-none opacity-0'
                  }`}
                  style={
                    useFallback
                      ? { background: posterFallback(b.id, true) }
                      : {
                          backgroundImage: `url(${b.image})`,
                          backgroundSize: 'cover',
                          backgroundPosition: 'center',
                        }
                  }
                >
                  {useFallback && (
                    <div className="absolute inset-0 flex items-center px-12">
                      <div className="text-white">
                        <h2 className="font-[family-name:var(--font-serif-cn)] text-[28px] font-extrabold">
                          精选推荐
                        </h2>
                        <p className="mt-2 text-sm opacity-90">点击查看影片详情</p>
                      </div>
                    </div>
                  )}
                </div>
              )
            })
          )}
          {/* 隐藏的 img 探测器：用于标记加载失败回退 */}
          {activeBanner?.image && !imgErr[activeBanner.id] && (
            <img
              src={activeBanner.image}
              alt=""
              className="hidden"
              onError={() => setImgErr((p) => ({ ...p, [activeBanner.id]: true }))}
            />
          )}
          {/* 手动切换按钮 */}
          {banners.length > 1 && (
            <>
              <button
                type="button"
                onClick={prev}
                aria-label="上一张"
                className="absolute top-1/2 left-3 flex size-9 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full bg-black/30 text-xl text-white transition-colors hover:bg-black/55"
              >
                ‹
              </button>
              <button
                type="button"
                onClick={next}
                aria-label="下一张"
                className="absolute top-1/2 right-3 flex size-9 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full bg-black/30 text-xl text-white transition-colors hover:bg-black/55"
              >
                ›
              </button>
            </>
          )}
          {/* 圆点指示器：悬停切换 */}
          {banners.length > 1 && (
            <div className="absolute bottom-3.5 left-1/2 flex -translate-x-1/2 gap-1.5">
              {banners.map((b, i) => (
                <button
                  key={b.id}
                  type="button"
                  onMouseEnter={() => setBannerIdx(i)}
                  onClick={() => setBannerIdx(i)}
                  className={`size-2 cursor-pointer rounded-full transition-all ${
                    i === bannerIdx ? 'w-6 bg-white' : 'bg-white/55'
                  }`}
                  aria-label={`轮播图 ${i + 1}`}
                />
              ))}
            </div>
          )}
        </div>

        {/* 热映 + 待映 + 票房榜 */}
        <div className="mt-7 grid grid-cols-[1fr_280px] gap-7 max-[900px]:grid-cols-1">
          <div>
            <SectionHeader title="热映电影" onMore={() => navigate('/movies?showStatus=hot&page=1')} />
            {hot.length === 0 ? (
              <EmptyRow />
            ) : (
              <PosterGrid movies={hot} badge={{ text: '热映', tone: 'hot' }} />
            )}

            <div className="mt-9" />
            <SectionHeader title="待映电影" onMore={() => navigate('/movies?showStatus=coming&page=1')} />
            {coming.length === 0 ? (
              <EmptyRow />
            ) : (
              <PosterGrid movies={coming} badge={{ text: '待映', tone: 'coming' }} />
            )}
          </div>

          {/* 票房榜 */}
          <aside className="space-y-3.5">
            <h3 className="font-[family-name:var(--font-serif-cn)] text-[18px] font-extrabold">
              今日票房榜
            </h3>
            <div className="rounded-2xl bg-card p-4 shadow-[var(--shadow-card)]">
              {box.length === 0 ? (
                <div className="py-6 text-center text-[13px] text-ink-2">暂无榜单</div>
              ) : (
                <ol className="space-y-3">
                  {box.map((b, i) => (
                    <li key={b.id}>
                      <button
                        type="button"
                        onClick={() => navigate(`/movies/${b.id}`)}
                        className="flex w-full cursor-pointer items-center gap-3 text-left hover:text-brand-deep"
                      >
                        <span
                          className={`flex size-7 shrink-0 items-center justify-center rounded-md font-mono text-[13px] font-bold ${
                            i < 3 ? 'bg-brand text-on-brand' : 'bg-bg-deep text-ink-2'
                          }`}
                        >
                          {i + 1}
                        </span>
                        <div className="min-w-0 flex-1">
                          <div className="truncate text-sm font-semibold">{b.title}</div>
                          <div className="text-[11px] text-ink-2">今日票房</div>
                        </div>
                        <div className="font-mono text-sm font-bold text-brand-deep">
                          ¥{b.boxOffice.toLocaleString('zh-CN', { minimumFractionDigits: 0 })}
                        </div>
                      </button>
                    </li>
                  ))}
                </ol>
              )}
            </div>
          </aside>
        </div>
      </main>
    </div>
  )
}

function SectionHeader({ title, onMore }: { title: string; onMore: () => void }) {
  return (
    <div className="mb-4 flex items-center gap-2.5">
      <h2 className="font-[family-name:var(--font-serif-cn)] text-[22px] font-extrabold">
        {title}
      </h2>
      <button
        type="button"
        onClick={onMore}
        className="ml-auto cursor-pointer text-sm text-ink-2 hover:text-brand-deep"
      >
        更多 ›
      </button>
    </div>
  )
}

function PosterGrid({
  movies,
  badge,
}: {
  movies: MovieVO[]
  badge: { text: string; tone: 'hot' | 'coming' }
}) {
  return (
    <div
      className="grid gap-x-5 gap-y-7"
      style={{ gridTemplateColumns: 'repeat(4, minmax(0, 1fr))' }}
    >
      {movies.map((m) => (
        <PosterCard key={m.id} movie={m} badge={badge} />
      ))}
    </div>
  )
}

function EmptyRow() {
  return <div className="py-10 text-center text-[13px] text-ink-2">暂无影片</div>
}