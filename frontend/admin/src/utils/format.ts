/** 后端日期时间统一输出 "yyyy-MM-dd HH:mm:ss"，这里截到分钟；空值返回 "—" */
export function formatDateTime(value?: string | null): string {
  if (!value) return '—'
  return value.slice(0, 16)
}

/** 金额：59.9 → "¥59.90"；空值返回 "—" */
export function formatAmount(value?: number | null): string {
  if (value === null || value === undefined) return '—'
  return `¥${value.toFixed(2)}`
}
