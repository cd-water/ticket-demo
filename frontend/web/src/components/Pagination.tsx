interface PaginationProps {
  page: number
  size: number
  total: number
  onChange: (page: number) => void
}

function pageRange(page: number, totalPages: number): (number | '…')[] {
  if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i + 1)
  const list: (number | '…')[] = [1]
  const left = Math.max(2, page - 1)
  const right = Math.min(totalPages - 1, page + 1)
  if (left > 2) list.push('…')
  for (let i = left; i <= right; i++) list.push(i)
  if (right < totalPages - 1) list.push('…')
  list.push(totalPages)
  return list
}

/** 简单数字分页；>= 8 条记录才显示，否则隐藏（mock 数据大多用不到） */
export function Pagination({ page, size, total, onChange }: PaginationProps) {
  if (total <= size) return null
  const totalPages = Math.max(1, Math.ceil(total / size))
  const safePage = Math.min(Math.max(page, 1), totalPages)
  const items = pageRange(safePage, totalPages)

  return (
    <div className="mt-6 flex items-center justify-center gap-2">
      <button
        type="button"
        disabled={safePage === 1}
        onClick={() => onChange(safePage - 1)}
        className="cursor-pointer rounded-full border border-line bg-card px-3 py-1.5 text-sm text-ink-2 transition-colors hover:border-brand-deep hover:text-brand-deep disabled:cursor-not-allowed disabled:opacity-40"
      >
        上一页
      </button>
      {items.map((it, i) =>
        it === '…' ? (
          <span key={`g${i}`} className="px-1 text-sm text-ink-2">
            …
          </span>
        ) : (
          <button
            key={it}
            type="button"
            onClick={() => onChange(it)}
            className={`size-9 cursor-pointer rounded-full text-sm font-semibold transition-colors ${
              it === safePage
                ? 'bg-brand text-on-brand'
                : 'border border-line bg-card text-ink-2 hover:border-brand-deep hover:text-brand-deep'
            }`}
          >
            {it}
          </button>
        ),
      )}
      <button
        type="button"
        disabled={safePage === totalPages}
        onClick={() => onChange(safePage + 1)}
        className="cursor-pointer rounded-full border border-line bg-card px-3 py-1.5 text-sm text-ink-2 transition-colors hover:border-brand-deep hover:text-brand-deep disabled:cursor-not-allowed disabled:opacity-40"
      >
        下一页
      </button>
    </div>
  )
}