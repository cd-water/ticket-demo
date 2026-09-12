/** "2026-09-13T10:00:00" → "2026-09-13 10:00"；空值返回 "—" */
export function formatDateTime(value?: string | null): string {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}

/** 金额：59.9 → "¥59.90"；空值返回 "—" */
export function formatAmount(value?: number | null): string {
  if (value === null || value === undefined) return '—'
  return `¥${value.toFixed(2)}`
}
