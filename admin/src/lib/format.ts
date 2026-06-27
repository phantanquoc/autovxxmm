import { format, formatDistanceToNow, fromUnixTime, parseISO } from 'date-fns'
import { vi } from 'date-fns/locale'

export function formatXu(n: number): string {
  return n.toLocaleString('vi-VN')
}

function toDate(ms: number | string): Date {
  if (typeof ms === 'string') {
    return parseISO(ms)
  }
  // If ms looks like Unix seconds (< 1e12), convert to ms
  if (ms < 1e12) {
    return fromUnixTime(ms)
  }
  return new Date(ms)
}

export function formatDateTime(ms: number | string): string {
  if (!ms) return '—'
  try {
    const d = toDate(ms)
    return format(d, 'dd-MM-yyyy HH:mm:ss')
  } catch {
    return String(ms)
  }
}

export function formatRelative(ms: number | string): string {
  if (!ms) return '—'
  try {
    const d = toDate(ms)
    return formatDistanceToNow(d, { addSuffix: true, locale: vi })
  } catch {
    return String(ms)
  }
}
