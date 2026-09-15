/** 千分位 + 两位小数（票房榜 / 票价） */
export function formatMoney(value: number): string {
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 仅整数（票价输入框友好） */
export function formatMoneyShort(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

/** 后端返回 "yyyy-MM-dd HH:mm:ss"；前端展示 "MM-dd HH:mm" */
export function formatScreeningTime(value: string): string {
  const d = new Date(value.replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return value
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${hh}:${mm}`
}

/** 后端 yyyy-MM-dd；前端中文 yyyy年MM月dd日 */
export function formatDate(value: string): string {
  const [y, m, d] = value.split('-')
  return y && m && d ? `${y}年${m}月${d}日` : value
}

/** 时长分钟 → "X 小时 Y 分钟"（173 → "2 小时 53 分钟"） */
export function formatDuration(minutes: number): string {
  if (!minutes) return ''
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m} 分钟`
  if (m === 0) return `${h} 小时`
  return `${h} 小时 ${m} 分钟`
}

/** 票房榜前 N 名拿一个稳定的色块，避免每张海报都长一样 */
export function posterFallback(seed: number | string, dark = false): string {
  const palettes = dark
    ? ['#3F3422', '#4A3A18', '#523D14', '#5C4310']
    : ['#F3E7C8', '#EAD9A8', '#F0DCAA', '#E8D08C']
  const idx =
    typeof seed === 'number'
      ? seed % palettes.length
      : [...seed].reduce((s, c) => (s * 31 + c.charCodeAt(0)) >>> 0, 0) % palettes.length
  return `linear-gradient(160deg, ${palettes[idx]}, ${palettes[(idx + 1) % palettes.length]})`
}