import { useNavigate } from 'react-router'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { showToast } from '@/components/toast'

interface NavItem {
  to: string
  label: string
  match: (path: string) => boolean
}

const NAV: NavItem[] = [
  { to: '/', label: '首页', match: (p) => p === '/' },
  { to: '/movies', label: '电影', match: (p) => p.startsWith('/movies') },
  { to: '/cinemas', label: '影院', match: (p) => p.startsWith('/cinemas') },
  { to: '/me', label: '我的', match: (p) => p.startsWith('/me') },
]

/** 顶部导航 + 品牌 logo + 用户入口（登录后显示昵称 + 退出按钮，无下拉） */
export function TopBar() {
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const clear = useAuthStore((s) => s.clear)
  const pathname = typeof window !== 'undefined' ? window.location.pathname : '/'

  async function onLogout() {
    const { refreshToken } = useAuthStore.getState()
    try {
      if (refreshToken) await logout(refreshToken)
    } catch {
      /* 即使后端吊销失败也本地退出 */
    }
    clear()
    showToast('已登出', 'success')
    if (pathname.startsWith('/me')) navigate('/', { replace: true })
  }

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-bg">
      <div className="mx-auto flex max-w-[1200px] items-center gap-7 px-6 py-3.5">
        <button
          type="button"
          onClick={() => navigate('/')}
          className="flex cursor-pointer items-center gap-2 font-[family-name:var(--font-serif-cn)] text-xl font-extrabold"
        >
          <span className="size-3 rounded-[3px] bg-brand shadow-[0_0_0_4px_rgba(255,195,0,0.25)]" />
          CD-TICKET 电影票务
        </button>
        <nav className="flex gap-5.5">
          {NAV.map((n) => (
            <button
              key={n.to}
              type="button"
              onClick={() => navigate(n.to)}
              className={`cursor-pointer py-1 text-[15px] transition-colors ${
                n.match(pathname)
                  ? 'border-b-2 border-brand font-bold text-ink'
                  : 'text-ink-2 hover:text-ink'
              }`}
            >
              {n.label}
            </button>
          ))}
        </nav>
        <div className="ml-auto flex items-center gap-3">
          {user ? (
            <>
              <button
                type="button"
                onClick={() => navigate('/me')}
                className="cursor-pointer text-sm font-semibold"
              >
                {user.nickname}
              </button>
              <button
                type="button"
                onClick={onLogout}
                className="cursor-pointer rounded-full border border-line bg-card px-4 py-1.5 text-[13px] font-semibold text-ink-2 transition-colors hover:border-[#E5484D] hover:text-[#E5484D]"
              >
                登出
              </button>
            </>
          ) : (
            <button
              type="button"
              className="cursor-pointer rounded-full bg-brand px-5 py-2 text-sm font-bold text-on-brand transition-colors hover:bg-brand-deep hover:text-white"
              onClick={() => navigate('/login')}
            >
              登录
            </button>
          )}
        </div>
      </div>
    </header>
  )
}